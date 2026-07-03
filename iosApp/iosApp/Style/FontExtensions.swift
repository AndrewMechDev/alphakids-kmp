import SwiftUI

extension Font {
    static func dynaPuff(size: CGFloat, weight: Weight = .regular) -> Font {
        let name: String
        switch weight {
        case .bold: name = "DynaPuff-Bold"
        case .semibold: name = "DynaPuff-SemiBold"
        case .medium: name = "DynaPuff-Medium"
        default: name = "DynaPuff-Regular"
        }
        return .custom(name, size: size)
    }

    static func dmSans(size: CGFloat, weight: Weight = .regular) -> Font {
        let name: String
        switch weight {
        case .bold: name = "DMSans-Bold"
        case .semibold: name = "DMSans-SemiBold"
        case .medium: name = "DMSans-Medium"
        default: name = "DMSans-Regular"
        }
        return .custom(name, size: size)
    }
}
