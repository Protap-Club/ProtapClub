//
//  WriteSocialView.swift
//  ProtapClub
//
//  Port of WriteSocialActivity.kt.
//

import SwiftUI

struct WriteSocialView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var username = ""
    @State private var platform = SocialMediaType.instagram
    @State private var status = "Enter a username, then tap Write to Tag"

    var body: some View {
        ScreenContainer(title: "Write Social Media to Tag") {
            VStack(alignment: .leading, spacing: 8) {
                Text("1. Choose Platform")
                    .font(.headline)

                Picker("Platform", selection: $platform) {
                    ForEach(SocialMediaType.allCases) { option in
                        Text(option.displayName).tag(option)
                    }
                }
                .pickerStyle(.menu)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }

            VStack(alignment: .leading, spacing: 8) {
                Text("2. Enter Username/Handle")
                    .font(.headline)

                TextField(platform.placeholder, text: $username)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }

            StatusText(text: status)

            Button("Write to Tag", action: write)
                .buttonStyle(ProtapButtonStyle())
                .disabled(username.isEmpty)

            BackButton(title: "Back") { dismiss() }
        }
    }

    private func write() {
        guard !username.isEmpty else {
            status = "Please enter a username"
            return
        }
        let url = platform.profileURL(for: username)
        guard let message = NFCUtils.uriMessage(url) else {
            status = "Error writing social media profile to tag: invalid username"
            return
        }
        nfc.write(
            message,
            successMessage: "Social media profile written successfully!\n\nPlatform: \(platform.displayName)\nProfile: \(url)",
            errorPrefix: "Error writing social media profile to tag"
        ) { result in
            if case .finished(let outcome) = result { status = outcome.message }
        }
    }
}

#Preview {
    NavigationStack { WriteSocialView() }
}
