//
//  WriteOptionsView.swift
//  ProtapClub
//
//  Port of WriteOptionsActivity.kt.
//

import SwiftUI

struct WriteOptionsView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScreenContainer(title: "Choose Write Option") {
            Spacer().frame(height: 8)

            NavigationLink("Write URL") { WriteUrlView() }
                .buttonStyle(ProtapButtonStyle())
            NavigationLink("Write Text") { WriteTextView() }
                .buttonStyle(ProtapButtonStyle())
            NavigationLink("Write Contact") { WriteContactView() }
                .buttonStyle(ProtapButtonStyle())
            NavigationLink("Write Social Media") { WriteSocialView() }
                .buttonStyle(ProtapButtonStyle())
            NavigationLink("Call to Action") { WriteCallToActionView() }
                .buttonStyle(ProtapButtonStyle())
            NavigationLink("Reset Tag") { ResetTagView() }
                .buttonStyle(ProtapButtonStyle())

            Spacer().frame(height: 8)

            BackButton(title: "Back to Main Menu") { dismiss() }
        }
    }
}

#Preview {
    NavigationStack { WriteOptionsView() }
}
