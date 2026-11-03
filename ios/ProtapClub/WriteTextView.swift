//
//  WriteTextView.swift
//  ProtapClub
//
//  Port of WriteTextActivity.kt.
//

import SwiftUI

struct WriteTextView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var text = ""
    @State private var status = "Enter text, then tap Write to Tag"

    var body: some View {
        ScreenContainer(title: "Write Text to Tag") {
            TextField("Your message here", text: $text, axis: .vertical)
                .lineLimit(3...8)
                .textFieldStyle(.roundedBorder)

            StatusText(text: status)

            Button("Write to Tag", action: write)
                .buttonStyle(ProtapButtonStyle())

            BackButton(title: "Back") { dismiss() }
        }
    }

    private func write() {
        guard !text.isEmpty else {
            status = "Please enter text first"
            return
        }
        guard let message = NFCUtils.textMessage(text) else {
            status = "Error writing to tag: could not encode text"
            return
        }
        nfc.write(
            message,
            successMessage: "Text written successfully!",
            errorPrefix: "Error writing to tag"
        ) { result in
            if case .finished(let outcome) = result { status = outcome.message }
        }
    }
}

#Preview {
    NavigationStack { WriteTextView() }
}
