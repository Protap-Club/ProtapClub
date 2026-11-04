//
//  ContentView.swift
//  ProtapClub
//
//  Main menu. Port of MainActivity.kt.
//

import SwiftUI

struct ContentView: View {
    @State private var showNFCAlert = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    @State private var toast: String?

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Spacer()

                Image("logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 160, height: 160)
                    .accessibilityLabel("Protap Club Logo")

                Spacer().frame(height: 16)

                NavigationLink {
                    ReadTagView()
                } label: {
                    Text("Read NFC Tag")
                }
                .buttonStyle(ProtapButtonStyle())

                NavigationLink {
                    WriteOptionsView()
                } label: {
                    Text("Write NFC Tag")
                }
                .buttonStyle(ProtapButtonStyle())

                Spacer()
                Spacer()
            }
            .padding(.horizontal, 50)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .foregroundStyle(Color.protapOnBackground)
            .background(Color.protapBackground.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
        }
        .onAppear(perform: checkNFCStatus)
        .alert(alertTitle, isPresented: $showNFCAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
        .toast($toast)
    }

    private func checkNFCStatus() {
        #if DEBUG
        // The Simulator has no NFC; this lets App Store screenshots be captured there.
        if ProcessInfo.processInfo.arguments.contains("-ScreenshotMode") {
            toast = "Welcome to ProTap Club, buddy!"
            return
        }
        #endif
        if NFCSession.isAvailable {
            toast = "Welcome to ProTap Club, buddy!"
        } else {
            alertTitle = "NFC Not Supported"
            alertMessage = "Your device doesn't support NFC."
            showNFCAlert = true
        }
    }
}

#Preview {
    ContentView()
}
