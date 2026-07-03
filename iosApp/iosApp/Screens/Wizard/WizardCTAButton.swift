import SwiftUI

struct WizardCTAButton: View {
    let title: String
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.custom("DynaPuff", size: 16))
                .fontWeight(.bold)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(enabled ? Color(hex: "3B7DF6") : Color.gray.opacity(0.4))
                .clipShape(Capsule())
                .shadow(color: enabled ? Color(hex: "3B7DF6").opacity(0.3) : .clear, radius: 12, y: 6)
        }
        .disabled(!enabled)
    }
}
