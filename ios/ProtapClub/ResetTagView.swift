//
//  ResetTagView.swift
//  ProtapClub
//
//  Port of ResetTagActivity.kt.
//

import SwiftUI

struct ResetTagView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var confirmed = false
    @State private var status = "Confirm below, then scan the NFC tag you want to reset"

    var body: some View {
        ScreenContainer(title: "Reset NFC Tag") {
            InfoCard {
                Text("Warning")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(Color.protapError)
                    .frame(maxWidth: .infinity)

                Text("Resetting will clear all data on the NFC tag. This action cannot be undone.")
                    .font(.callout)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                if !confirmed {
                    Button("I understand, continue") { confirmed = true }
                        .buttonStyle(ProtapButtonStyle(tint: .protapError))
                } else {
                    Text("Ready to reset. Scan your NFC tag now.")
                        .font(.callout)
                        .foregroundStyle(Color.protapError)
                        .frame(maxWidth: .infinity)
                }
            }

            StatusText(text: status)

            Button("Scan Tag to Reset", action: reset)
                .buttonStyle(ProtapButtonStyle(tint: .protapError))
                .disabled(!confirmed)

            BackButton(title: "Back") { dismiss() }
        }
    }

    private func reset() {
        guard confirmed else {
            status = "Please confirm you want to reset the tag first"
            return
        }
        let info = NFCUtils.resetInfo()
        guard let message = NFCUtils.textMessage(info) else {
            status = "Error resetting tag: could not encode reset message"
            return
        }
        nfc.write(
            message,
            successMessage: "Tag reset successfully!\n\n\(info)",
            errorPrefix: "Error resetting tag",
            prompt: "Hold your iPhone near the NFC tag you want to reset."
        ) { result in
            if case .finished(let outcome) = result { status = outcome.message }
        }
    }
}

#Preview {
    NavigationStack { ResetTagView() }
}
