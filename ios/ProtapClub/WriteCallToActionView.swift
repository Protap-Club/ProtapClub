//
//  WriteCallToActionView.swift
//  ProtapClub
//
//  Port of WriteCallToActionActivity.kt.
//

import SwiftUI

struct WriteCallToActionView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var toast: String?

    // Action picker and per-action input dialogs
    @State private var showPicker = false
    @State private var showInput = false
    @State private var inputAction: CallToAction?

    @State private var emergencyPhone = ""
    @State private var smsNumber = ""
    @State private var smsBody = ""
    @State private var facetimeNumber = ""
    @State private var facetimeAudioNumber = ""
    @State private var address = ""
    @State private var latitude = ""
    @State private var longitude = ""

    // What will be written once a tag is scanned
    @State private var pendingAction: CallToAction?
    @State private var pendingData: String?
    @State private var pendingBody: String?

    private var waitingForTag: Bool { pendingAction != nil && pendingData != nil }

    var body: some View {
        ScreenContainer(title: "Call to Action", logoSize: 200) {
            Button("Select Action") { showPicker = true }
                .buttonStyle(ProtapButtonStyle())

            if waitingForTag, let action = pendingAction {
                InfoCard(background: .protapSecondaryContainer) {
                    Text("Input to be written:")
                        .font(.headline)
                    summary(for: action)
                }

                Button("Scan Tag to Write", action: writePending)
                    .buttonStyle(ProtapButtonStyle())
            }

            Text("Empower your NFC tags with Protap Club!")
                .font(.callout)
                .foregroundStyle(Color.protapPrimary.opacity(0.9))
                .padding(.top, 8)

            BackButton(title: "Back") { dismiss() }
        }
        .toast($toast)
        .confirmationDialog("Select Action Type", isPresented: $showPicker, titleVisibility: .visible) {
            ForEach(CallToAction.allCases) { action in
                Button(action.menuTitle) { present(action) }
            }
            Button("Cancel", role: .cancel) {}
        }
        .alert(inputAction?.dialogTitle ?? "", isPresented: $showInput) {
            inputFields
            Button("Cancel", role: .cancel) {}
            Button("Confirm", action: confirmInput)
        }
    }

    // MARK: - Pieces

    @ViewBuilder
    private func summary(for action: CallToAction) -> some View {
        switch action {
        case .emergency:
            Text("Phone Number: \(pendingData ?? "")")
        case .message:
            Text("Phone Number: \(pendingData ?? "")")
            if let body = pendingBody,
               !body.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                Text("Message: \(body)")
            }
        case .facetime:
            Text("FaceTime: \(pendingData ?? "")")
        case .facetimeAudio:
            Text("FaceTime Audio: \(pendingData ?? "")")
        case .address:
            Text("Address: \(pendingData ?? "")")
        case .location:
            Text("Location: \(pendingData ?? "")")
        }
    }

    @ViewBuilder
    private var inputFields: some View {
        switch inputAction {
        case .emergency:
            TextField("Phone Number", text: $emergencyPhone)
                .keyboardType(.phonePad)
        case .message:
            TextField("Phone Number", text: $smsNumber)
                .keyboardType(.phonePad)
            TextField("Message Body", text: $smsBody)
        case .facetime:
            TextField("Number or Email", text: $facetimeNumber)
                .keyboardType(.emailAddress)
                .textInputAutocapitalization(.never)
        case .facetimeAudio:
            TextField("Number or Email", text: $facetimeAudioNumber)
                .keyboardType(.emailAddress)
                .textInputAutocapitalization(.never)
        case .address:
            TextField("Address", text: $address)
        case .location:
            TextField("Latitude", text: $latitude)
                .keyboardType(.numbersAndPunctuation)
            TextField("Longitude", text: $longitude)
                .keyboardType(.numbersAndPunctuation)
        case nil:
            EmptyView()
        }
    }

    // MARK: - Actions

    private func present(_ action: CallToAction) {
        inputAction = action
        // Let the confirmation dialog finish dismissing before the alert appears.
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) { showInput = true }
    }

    /// Same required-field rules as the Android dialogs; invalid input is ignored.
    private func confirmInput() {
        guard let action = inputAction else { return }
        switch action {
        case .emergency:
            guard !emergencyPhone.isEmpty else { return }
            select(action, data: emergencyPhone, body: nil)
        case .message:
            guard !smsNumber.isEmpty else { return }
            select(action, data: smsNumber, body: smsBody)
        case .facetime:
            guard !facetimeNumber.isEmpty else { return }
            select(action, data: facetimeNumber, body: nil)
        case .facetimeAudio:
            guard !facetimeAudioNumber.isEmpty else { return }
            select(action, data: facetimeAudioNumber, body: nil)
        case .address:
            guard !address.isEmpty else { return }
            select(action, data: address, body: nil)
        case .location:
            guard !latitude.isEmpty, !longitude.isEmpty else { return }
            select(action, data: "\(latitude),\(longitude)", body: nil)
        }
    }

    private func select(_ action: CallToAction, data: String, body: String?) {
        pendingAction = action
        pendingData = data
        pendingBody = body
        writePending()
    }

    private func writePending() {
        guard let action = pendingAction, let data = pendingData else { return }
        guard let message = NFCUtils.ctaMessage(action: action, data: data, body: pendingBody) else {
            toast = "Nothing written to tag."
            return
        }
        nfc.write(
            message,
            successMessage: "Call to Action written successfully!",
            errorPrefix: "Error writing Call to Action"
        ) { result in
            guard case .finished(let outcome) = result else { return }
            toast = outcome.message
            if outcome.success {
                pendingAction = nil
                pendingData = nil
                pendingBody = nil
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) { dismiss() }
            }
        }
    }
}

#Preview {
    NavigationStack { WriteCallToActionView() }
}
