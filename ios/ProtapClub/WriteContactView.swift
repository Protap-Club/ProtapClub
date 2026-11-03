//
//  WriteContactView.swift
//  ProtapClub
//
//  Port of WriteContactActivity.kt.
//

import SwiftUI

struct WriteContactView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var nfc = NFCSession()
    @State private var contact = ContactInfo()
    @State private var status = "Fill in the contact details, then tap Write Contact"

    var body: some View {
        ScreenContainer(title: "Write Contact to Tag", logoSize: 100) {
            VStack(spacing: 8) {
                TextField("Name", text: $contact.name)
                    .textContentType(.name)
                TextField("Phone", text: $contact.phone)
                    .keyboardType(.phonePad)
                    .textContentType(.telephoneNumber)
                TextField("Email", text: $contact.email)
                    .keyboardType(.emailAddress)
                    .textContentType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                TextField("Company", text: $contact.company)
                    .textContentType(.organizationName)
                TextField("Job Title", text: $contact.title)
                    .textContentType(.jobTitle)
                TextField("Address", text: $contact.address, axis: .vertical)
                    .lineLimit(2...5)
                    .textContentType(.fullStreetAddress)
            }
            .textFieldStyle(.roundedBorder)

            StatusText(text: status)

            Button("Write Contact", action: write)
                .buttonStyle(ProtapButtonStyle())

            BackButton(title: "Back") { dismiss() }
        }
    }

    private func write() {
        nfc.write(
            NFCUtils.contactMessage(contact),
            successMessage: "Contact information written successfully!",
            errorPrefix: "Error writing contact to tag"
        ) { result in
            if case .finished(let outcome) = result { status = outcome.message }
        }
    }
}

#Preview {
    NavigationStack { WriteContactView() }
}
