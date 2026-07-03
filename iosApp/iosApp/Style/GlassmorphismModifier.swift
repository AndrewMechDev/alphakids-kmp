import SwiftUI

struct GlassBackground: ViewModifier {
    var tint: Color
    var opacity: Double

    func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(tint.opacity(opacity))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.white.opacity(0.25), lineWidth: 1)
                    )
                    .shadow(color: .black.opacity(0.15), radius: 15, x: 0, y: 8)
            )
    }
}

extension View {
    func glassBackground(tint: Color = .white, opacity: Double = 0.3) -> some View {
        modifier(GlassBackground(tint: tint, opacity: opacity))
    }
}
