//
//  WriteUrlView.swift
//  ProtapClub
//
//  Port of WriteUrlActivity.kt.
//

import SwiftUI

struct WriteUrlView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var url = ""
    @State private var status = "Enter a URL, then tap Write to Tag"

    var body: some View {
        ScreenContainer(title: "Write URL to Tag") {
            TextField("https://example.com", text: $url)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.URL)
                .textContentType(.URL)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()

            StatusText(text: status)

            Button("Write to Tag", action: write)
                .buttonStyle(ProtapButtonStyle())

            BackButton(title: "Back") { dismiss() }
        }
    }

    private func write() {
        guard !url.isEmpty else {
            status = "Please enter a URL first"
            return
        }
        guard let message = NFCUtils.uriMessage(NFCUtils.formatUrl(url)) else {
            status = "Error writing to tag: invalid URL"
            return
        }
        nfc.write(
            message,
            successMessage: "URL written successfully! The link will open when tapped.",
            errorPrefix: "Error writing to tag"
        ) { result in
            if case .finished(let outcome) = result { status = outcome.message }
        }
    }
}

#Preview {
    NavigationStack { WriteUrlView() }
}
