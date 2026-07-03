import SwiftUI

enum CircadianPeriod: String, CaseIterable {
    case morning, afternoon, evening, night

    static var current: CircadianPeriod {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 6...11: return .morning
        case 12...18: return .afternoon
        case 19...20: return .evening
        default: return .night
        }
    }

    var gradientColors: [Color] {
        switch self {
        case .morning:
            return [Color(hex: "4FA8F0"), Color(hex: "C9B8F5")]
        case .afternoon:
            return [Color(hex: "4FA8F0"), Color(hex: "B9A6F2")]
        case .evening:
            return [Color(hex: "B9A6F2"), Color(hex: "6C5CE7")]
        case .night:
            return [Color(hex: "2D2157"), Color(hex: "1A1035")]
        }
    }
}

struct CircadianBackground: ViewModifier {
    var period: CircadianPeriod

    func body(content: Content) -> some View {
        content
            .background(
                LinearGradient(
                    gradient: Gradient(colors: period.gradientColors),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
            )
    }
}

extension View {
    func circadianBackground(_ period: CircadianPeriod = .current) -> some View {
        modifier(CircadianBackground(period: period))
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
