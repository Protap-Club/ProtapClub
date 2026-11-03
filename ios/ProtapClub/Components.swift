//
//  Components.swift
//  ProtapClub
//
//  Shared theme and UI pieces (mirrors the Android Material theme in ui/theme).
//

import SwiftUI

extension Color {
    static let protapPrimary = Color(red: 0x3F / 255, green: 0x51 / 255, blue: 0xB5 / 255)
    static let protapBackground = Color(red: 0xF5 / 255, green: 0xF5 / 255, blue: 0xF5 / 255)
    static let protapOnBackground = Color(red: 0x1A / 255, green: 0x1C / 255, blue: 0x1E / 255)
    static let protapSecondaryContainer = Color(red: 0xD1 / 255, green: 0xD9 / 255, blue: 0xFF / 255)
    static let protapError = Color(red: 0xB3 / 255, green: 0x26 / 255, blue: 0x1E / 255)
}

struct ProtapButtonStyle: ButtonStyle {
    var tint: Color = .protapPrimary

    func makeBody(configuration: Configuration) -> some View {
        StyledLabel(configuration: configuration, tint: tint)
    }

    private struct StyledLabel: View {
        let configuration: Configuration
        let tint: Color
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            configuration.label
                .font(.headline)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: 52)
                .background(isEnabled ? tint : Color.gray.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .opacity(configuration.isPressed ? 0.8 : 1)
        }
    }
}

/// Page layout shared by every screen: title, logo, then the screen's own content.
struct ScreenContainer<Content: View>: View {
    let title: String
    var logoSize: CGFloat = 160
    @ViewBuilder var content: Content

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text(title)
                    .font(.title.weight(.semibold))
                    .multilineTextAlignment(.center)
                    .padding(.bottom, 8)

                Image("logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: logoSize, height: logoSize)
                    .accessibilityLabel("Protap Club Logo")

                content
            }
            .padding(16)
            .frame(maxWidth: .infinity)
        }
        .scrollDismissesKeyboard(.interactively)
        .foregroundStyle(Color.protapOnBackground)
        .background(Color.protapBackground.ignoresSafeArea())
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct InfoCard<Content: View>: View {
    var background: Color = .white
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            content
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(background)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct StatusText: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.callout)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
    }
}

struct BackButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(title, action: action)
            .buttonStyle(ProtapButtonStyle())
            .padding(.horizontal, 40)
    }
}

struct ToastView: View {
    let message: String

    var body: some View {
        Text(message)
            .font(.callout)
            .foregroundColor(.white)
            .multilineTextAlignment(.center)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Color.black.opacity(0.85))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
            .transition(.opacity)
    }
}

private struct ToastModifier: ViewModifier {
    @Binding var message: String?

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .bottom) {
                if let message {
                    ToastView(message: message)
                        .task(id: message) {
                            try? await Task.sleep(for: .seconds(3))
                            withAnimation { self.message = nil }
                        }
                        .allowsHitTesting(false)
                }
            }
            .animation(.easeInOut, value: message)
    }
}

extension View {
    /// Shows a transient message at the bottom of the screen (Android `Toast` equivalent).
    func toast(_ message: Binding<String?>) -> some View {
        modifier(ToastModifier(message: message))
    }
}
