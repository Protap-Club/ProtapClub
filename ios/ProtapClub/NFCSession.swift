//
//  NFCSession.swift
//  ProtapClub
//
//  One-shot CoreNFC NDEF session used for reading and writing tags.
//  Port of NfcBaseActivity.kt: Android keeps foreground dispatch on while a screen is open,
//  iOS needs an explicit scan session that shows the system "Ready to Scan" sheet.
//

@preconcurrency import CoreNFC

nonisolated enum NFCReadResult {
    case message(NFCNDEFMessage)
    case noData
    case notNDEF
    case failure(String)
    case cancelled
}

nonisolated enum NFCWriteResult {
    case finished(WriteResult)
    case cancelled
}

final class NFCSession: NSObject, NFCNDEFReaderSessionDelegate {
    private nonisolated enum Plan {
        case read
        case write(message: NFCNDEFMessage, successMessage: String, errorPrefix: String)
    }

    private var session: NFCNDEFReaderSession?
    private var plan: Plan?
    private var readCompletion: ((NFCReadResult) -> Void)?
    private var writeCompletion: ((NFCWriteResult) -> Void)?

    static var isAvailable: Bool { NFCNDEFReaderSession.readingAvailable }

    var isScanning: Bool { session != nil }

    // MARK: - Public API

    func read(completion: @escaping (NFCReadResult) -> Void) {
        guard session == nil else { return }
        guard begin(plan: .read, prompt: "Hold your iPhone near an NFC tag to read it.") else {
            completion(.failure("Your device doesn't support NFC."))
            return
        }
        readCompletion = completion
    }

    func write(
        _ message: NFCNDEFMessage,
        successMessage: String,
        errorPrefix: String,
        prompt: String = "Hold your iPhone near an NFC tag to write.",
        completion: @escaping (NFCWriteResult) -> Void
    ) {
        guard session == nil else { return }
        let plan = Plan.write(message: message, successMessage: successMessage, errorPrefix: errorPrefix)
        guard begin(plan: plan, prompt: prompt) else {
            completion(.finished(WriteResult(success: false, message: "Your device doesn't support NFC.")))
            return
        }
        writeCompletion = completion
    }

    // MARK: - Session lifecycle

    private func begin(plan: Plan, prompt: String) -> Bool {
        guard NFCNDEFReaderSession.readingAvailable else { return false }
        let newSession = NFCNDEFReaderSession(delegate: self, queue: .main, invalidateAfterFirstRead: false)
        newSession.alertMessage = prompt
        self.plan = plan
        session = newSession
        newSession.begin()
        return true
    }

    private func cleanUp() {
        session = nil
        plan = nil
        readCompletion = nil
        writeCompletion = nil
    }

    private func deliverRead(_ result: NFCReadResult) {
        let completion = readCompletion
        cleanUp()
        completion?(result)
    }

    private func deliverWrite(_ result: NFCWriteResult) {
        let completion = writeCompletion
        cleanUp()
        completion?(result)
    }

    private func sessionEnded(_ ended: NFCNDEFReaderSession, code: NFCReaderError.Code?, message: String) {
        // Ignore late callbacks from an older session once a new one is running.
        if let current = session, current !== ended { return }

        let reason: String?
        switch code {
        case .readerSessionInvalidationErrorUserCanceled: reason = nil
        case .readerSessionInvalidationErrorSessionTimeout: reason = "Scan timed out. Please try again."
        default: reason = message
        }

        if readCompletion != nil {
            deliverRead(reason.map { .failure("Error reading tag: \($0)") } ?? .cancelled)
        } else if writeCompletion != nil {
            let prefix: String
            if case .write(_, _, let errorPrefix)? = plan { prefix = errorPrefix } else { prefix = "Error writing to tag" }
            deliverWrite(reason.map { .finished(WriteResult(success: false, message: "\(prefix): \($0)")) } ?? .cancelled)
        } else {
            cleanUp()
        }
    }

    // MARK: - NFCNDEFReaderSessionDelegate

    nonisolated func readerSessionDidBecomeActive(_ session: NFCNDEFReaderSession) {}

    // Required by the protocol; not called because the tag-based callback below is implemented.
    nonisolated func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {}

    nonisolated func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        let code = (error as? NFCReaderError)?.code
        let message = error.localizedDescription
        DispatchQueue.main.async {
            MainActor.assumeIsolated { self.sessionEnded(session, code: code, message: message) }
        }
    }

    nonisolated func readerSession(_ session: NFCNDEFReaderSession, didDetect tags: [NFCNDEFTag]) {
        if tags.count > 1 {
            session.alertMessage = "More than one tag detected. Please present only one tag."
            DispatchQueue.global().asyncAfter(deadline: .now() + 0.5) { session.restartPolling() }
            return
        }
        guard let tag = tags.first,
              let plan = MainActor.assumeIsolated({ self.plan }) else {
            session.invalidate(errorMessage: "No tag found.")
            return
        }
        process(tag, in: session, plan: plan)
    }

    // MARK: - Tag handling

    private nonisolated func process(_ tag: NFCNDEFTag, in session: NFCNDEFReaderSession, plan: Plan) {
        session.connect(to: tag) { error in
            if let error {
                self.systemFailure(session, plan, error)
                return
            }
            tag.queryNDEFStatus { status, capacity, error in
                if let error {
                    self.systemFailure(session, plan, error)
                    return
                }
                switch plan {
                case .read:
                    self.read(tag, status: status, in: session)
                case .write(let message, let successMessage, _):
                    self.write(message, to: tag, status: status, capacity: capacity,
                               successMessage: successMessage, in: session, plan: plan)
                }
            }
        }
    }

    private nonisolated func read(_ tag: NFCNDEFTag, status: NFCNDEFStatus, in session: NFCNDEFReaderSession) {
        guard status != .notSupported else {
            finishRead(session, .notNDEF, sheetMessage: "Tag is not NDEF formatted.")
            return
        }
        tag.readNDEF { message, error in
            if let error {
                if (error as? NFCReaderError)?.code == .ndefReaderSessionErrorZeroLengthMessage {
                    self.finishRead(session, .noData, sheetMessage: "No NDEF data found on this tag.")
                } else {
                    self.systemFailure(session, .read, error)
                }
                return
            }
            guard let message, !message.records.isEmpty else {
                self.finishRead(session, .noData, sheetMessage: "No NDEF data found on this tag.")
                return
            }
            self.finishRead(session, .message(message), sheetMessage: nil)
        }
    }

    private nonisolated func write(
        _ message: NFCNDEFMessage,
        to tag: NFCNDEFTag,
        status: NFCNDEFStatus,
        capacity: Int,
        successMessage: String,
        in session: NFCNDEFReaderSession,
        plan: Plan
    ) {
        switch status {
        case .notSupported:
            finishWrite(session, WriteResult(success: false, message: "Tag is not NDEF formatted."), sheetMessage: "Tag is not NDEF formatted.")
            return
        case .readOnly:
            finishWrite(session, WriteResult(success: false, message: "Tag is read-only and can't be written."), sheetMessage: "Tag is read-only.")
            return
        default:
            break
        }

        let size = message.length
        guard size <= capacity else {
            let text = "Not enough space on this tag: the data needs \(size) bytes but the tag holds \(capacity)."
            finishWrite(session, WriteResult(success: false, message: text), sheetMessage: "Not enough space on tag.")
            return
        }

        tag.writeNDEF(message) { error in
            if let error {
                self.systemFailure(session, plan, error)
            } else {
                self.finishWrite(session, WriteResult(success: true, message: successMessage), sheetMessage: nil)
            }
        }
    }

    // MARK: - Finishing

    private nonisolated func systemFailure(_ session: NFCNDEFReaderSession, _ plan: Plan, _ error: Error) {
        let detail = error.localizedDescription
        switch plan {
        case .read:
            finishRead(session, .failure("Error reading tag: \(detail)"), sheetMessage: "Could not read tag.")
        case .write(_, _, let prefix):
            finishWrite(session, WriteResult(success: false, message: "\(prefix): \(detail)"), sheetMessage: "Could not write tag.")
        }
    }

    /// `sheetMessage` is shown in the system sheet as an error; `nil` means success.
    private nonisolated func finishRead(_ session: NFCNDEFReaderSession, _ result: NFCReadResult, sheetMessage: String?) {
        DispatchQueue.main.async {
            MainActor.assumeIsolated { self.deliverRead(result) }
        }
        close(session, errorMessage: sheetMessage, successMessage: "Tag read successfully.")
    }

    private nonisolated func finishWrite(_ session: NFCNDEFReaderSession, _ result: WriteResult, sheetMessage: String?) {
        DispatchQueue.main.async {
            MainActor.assumeIsolated { self.deliverWrite(.finished(result)) }
        }
        close(session, errorMessage: sheetMessage, successMessage: "Tag written successfully.")
    }

    private nonisolated func close(_ session: NFCNDEFReaderSession, errorMessage: String?, successMessage: String) {
        if let errorMessage {
            session.invalidate(errorMessage: errorMessage)
        } else {
            session.alertMessage = successMessage
            session.invalidate()
        }
    }
}
