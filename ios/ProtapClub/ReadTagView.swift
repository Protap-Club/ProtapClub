//
//  ReadTagView.swift
//  ProtapClub
//
//  Port of ReadTagActivity.kt.
//

import SwiftUI
import UIKit

struct ReadTagView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var tagInfo = "Scan an NFC tag to see details"
    @State private var toast: String?

    var body: some View {
        ScreenContainer(title: "Read NFC Tag") {
            Text("Hold your iPhone near an NFC tag to read its contents")
                .font(.callout)
                .multilineTextAlignment(.center)

            InfoCard {
                Text("Tag Information")
                    .font(.headline)
                Text(tagInfo)
                    .textSelection(.enabled)
            }

            Button("Scan NFC Tag", action: scan)
                .buttonStyle(ProtapButtonStyle())

            BackButton(title: "Back to Main Menu") { dismiss() }
        }
        .toast($toast)
    }

    private func scan() {
        nfc.read { result in
            switch result {
            case .message(let message):
                tagInfo = NFCUtils.describe(message)
                runAutomations(NFCUtils.textRecords(in: message))
            case .noData:
                tagInfo = "No NDEF data found on this tag."
            case .notNDEF:
                tagInfo = "Tag is not NDEF formatted."
            case .failure(let text):
                tagInfo = text
            case .cancelled:
                break
            }
        }
    }

    /// Text records starting with SILENT:true, EMERGENCY:<phone> or MESSAGE:<text> trigger an action.
    private func runAutomations(_ texts: [String]) {
        for text in texts {
            if text.hasPrefix("SILENT:true") {
                // iOS has no public API for apps to change the ringer/silent state.
                toast = "iOS doesn't allow apps to turn on silent mode. Use a Focus or Shortcut instead."
            } else if text.hasPrefix("EMERGENCY:") {
                let phone = String(text.dropFirst("EMERGENCY:".count)).trimmingCharacters(in: .whitespacesAndNewlines)
                if !phone.isEmpty { call(phone) }
            } else if text.hasPrefix("MESSAGE:") {
                let message = String(text.dropFirst("MESSAGE:".count)).trimmingCharacters(in: .whitespacesAndNewlines)
                if !message.isEmpty { toast = message }
            }
        }
    }

    private func call(_ phone: String) {
        let encoded = phone.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? phone
        guard let url = URL(string: "tel:\(encoded)") else {
            toast = "Permission denied to make calls."
            return
        }
        // iOS always asks the user to confirm before dialing.
        UIApplication.shared.open(url) { success in
            if !success { toast = "Permission denied to make calls." }
        }
    }
}

#Preview {
    NavigationStack { ReadTagView() }
}
