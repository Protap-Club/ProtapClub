//
//  NFCUtils.swift
//  ProtapClub
//
//  Builds and parses NDEF payloads. Port of NfcUtils.kt / NfcBaseActivity.kt.
//

@preconcurrency import CoreNFC
import Foundation

nonisolated struct WriteResult {
    let success: Bool
    let message: String
}

nonisolated enum SocialMediaType: String, CaseIterable, Identifiable {
    case facebook, instagram, twitter, linkedin, github, tiktok, youtube

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .facebook: "Facebook"
        case .instagram: "Instagram"
        case .twitter: "Twitter / X"
        case .linkedin: "LinkedIn"
        case .github: "GitHub"
        case .tiktok: "TikTok"
        case .youtube: "YouTube"
        }
    }

    var placeholder: String {
        switch self {
        case .twitter: "e.g. elonmusk (without @)"
        case .instagram: "e.g. instagram"
        case .facebook: "e.g. zuck"
        case .linkedin: "e.g. billgates"
        case .github: "e.g. torvalds"
        case .tiktok: "e.g. khaby.lame"
        case .youtube: "e.g. @MrBeast"
        }
    }

    func profileURL(for username: String) -> String {
        switch self {
        case .facebook: "https://facebook.com/\(username)"
        case .instagram: "https://instagram.com/\(username)"
        case .twitter: "https://twitter.com/\(username)"
        case .linkedin: "https://linkedin.com/in/\(username)"
        case .github: "https://github.com/\(username)"
        case .tiktok: "https://tiktok.com/@\(username)"
        case .youtube: "https://youtube.com/\(username)"
        }
    }
}

nonisolated enum CallToAction: String, CaseIterable, Identifiable {
    case emergency, message, facetime, facetimeAudio, address, location

    var id: String { rawValue }

    var menuTitle: String {
        switch self {
        case .emergency: "Add Emergency Phone"
        case .message: "Add a Message"
        case .facetime: "FaceTime Number"
        case .facetimeAudio: "FaceTime Audio Only"
        case .address: "Address"
        case .location: "Location (Lat, Lon)"
        }
    }

    var dialogTitle: String {
        switch self {
        case .emergency: "Enter Emergency Phone"
        case .message: "Send SMS"
        case .facetime: "FaceTime Number or Email"
        case .facetimeAudio: "FaceTime Audio Number or Email"
        case .address: "Enter Address"
        case .location: "Enter Location"
        }
    }
}

nonisolated struct ContactInfo {
    var name = ""
    var phone = ""
    var email = ""
    var company = ""
    var title = ""
    var address = ""
}

nonisolated enum NFCUtils {
    // MARK: - Write payloads

    static func textMessage(_ text: String) -> NFCNDEFMessage? {
        guard let record = NFCNDEFPayload.wellKnownTypeTextPayload(string: text, locale: Locale(identifier: "en")) else {
            return nil
        }
        return NFCNDEFMessage(records: [record])
    }

    static func uriMessage(_ uri: String) -> NFCNDEFMessage? {
        guard let record = NFCNDEFPayload.wellKnownTypeURIPayload(string: uri) else { return nil }
        return NFCNDEFMessage(records: [record])
    }

    /// Prepends https:// when no http(s) scheme was typed.
    static func formatUrl(_ url: String) -> String {
        if !url.hasPrefix("http://") && !url.hasPrefix("https://") {
            return "https://\(url)"
        }
        return url
    }

    /// Same field order and format as the Android vCard builder.
    static func contactMessage(_ info: ContactInfo) -> NFCNDEFMessage {
        var vCard = "BEGIN:VCARD\n"
        vCard += "VERSION:3.0\n"
        if !info.name.isEmpty { vCard += "FN:\(info.name)\n" }
        if !info.phone.isEmpty { vCard += "TEL:\(info.phone)\n" }
        if !info.email.isEmpty { vCard += "EMAIL:\(info.email)\n" }
        if !info.company.isEmpty { vCard += "ORG:\(info.company)\n" }
        if !info.title.isEmpty { vCard += "TITLE:\(info.title)\n" }
        if !info.address.isEmpty { vCard += "ADR:;;\(info.address)\n" }
        vCard += "END:VCARD"

        let record = NFCNDEFPayload(
            format: .media,
            type: Data("text/vcard".utf8),
            identifier: Data(),
            payload: Data(vCard.utf8)
        )
        return NFCNDEFMessage(records: [record])
    }

    /// Text written when a tag is reset (date, timestamp, device), as on Android.
    static func resetInfo(now: Date = Date()) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let timestamp = Int64(now.timeIntervalSince1970 * 1000)
        return """
        TAG RESET
        Date: \(formatter.string(from: now))
        Timestamp: \(timestamp)
        Device: Apple \(deviceModelIdentifier())
        """
    }

    /// Builds the NDEF message for a call-to-action. `nil` when the action can't be expressed as a URI.
    static func ctaMessage(action: CallToAction, data: String, body: String?) -> NFCNDEFMessage? {
        let uri: String
        switch action {
        case .emergency:
            uri = "tel:\(data)"
        case .message:
            if let body, !body.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                uri = "sms:\(data)?body=" + uriEncode(body)
            } else {
                uri = "sms:\(data)"
            }
        case .facetime:
            uri = "facetime:\(data)"
        case .facetimeAudio:
            uri = "facetime-audio:\(data)"
        case .address:
            uri = "https://maps.google.com/?q=" + uriEncode(data)
        case .location:
            uri = "geo:\(data)"
        }
        return uriMessage(uri)
    }

    /// Percent-encoding with the same safe set as Android's `Uri.encode`.
    static func uriEncode(_ value: String) -> String {
        var allowed = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
        allowed.insert(charactersIn: "_-!.~'()*")
        return value.addingPercentEncoding(withAllowedCharacters: allowed) ?? value
    }

    // MARK: - Read parsing

    /// One line per record: URI / Contact / MIME / Text / Unknown (same labels as Android).
    static func describe(_ message: NFCNDEFMessage) -> String {
        message.records.map(describe).joined(separator: "\n")
    }

    static func describe(_ record: NFCNDEFPayload) -> String {
        switch record.typeNameFormat {
        case .nfcWellKnown:
            if let url = record.wellKnownTypeURIPayload() {
                return "URI: \(url.absoluteString)"
            }
            if let text = record.wellKnownTypeTextPayload().0 {
                return "Text: \(text)"
            }
            return "Unknown Data Format"
        case .absoluteURI:
            return "URI: \(String(decoding: record.payload, as: UTF8.self))"
        case .media:
            let mimeType = String(decoding: record.type, as: UTF8.self)
            let payload = String(decoding: record.payload, as: UTF8.self)
            if mimeType == "text/vcard" || mimeType == "text/x-vcard" {
                return "Contact: \(payload)"
            }
            return "MIME: \(payload)"
        default:
            return "Unknown Data Format"
        }
    }

    /// Text of every well-known text record; used for the SILENT / EMERGENCY / MESSAGE automations.
    static func textRecords(in message: NFCNDEFMessage) -> [String] {
        message.records.compactMap { record in
            guard record.typeNameFormat == .nfcWellKnown else { return nil }
            return record.wellKnownTypeTextPayload().0
        }
    }

    private static func deviceModelIdentifier() -> String {
        var info = utsname()
        uname(&info)
        let machine = withUnsafePointer(to: &info.machine) {
            $0.withMemoryRebound(to: CChar.self, capacity: Int(_SYS_NAMELEN)) { String(cString: $0) }
        }
        return machine
    }
}
