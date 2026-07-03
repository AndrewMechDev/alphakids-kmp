import SwiftUI
import SharedLogic

struct SubscriptionScreen: View {
    let subscription: SharedLogic.SubscriptionInfo?

    /// Kotlin `enum class PlanType { FREE, PREMIUM }` bridges to ObjC/Swift with
    /// a `.name` property ("FREE" / "PREMIUM"). Comparing by name avoids relying
    /// on the exact case-folding Kotlin/Native chooses for exported enum cases.
    private func isFree(_ sub: SharedLogic.SubscriptionInfo) -> Bool {
        sub.planType.name == "FREE"
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                if let sub = subscription {
                    // Current plan
                    VStack(spacing: 12) {
                        let free = isFree(sub)
                        Text(free ? "🆓" : "👑").font(.system(size: 48))
                        Text(sub.planName)
                            .font(.custom("DynaPuff", size: 22))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                        Text(free ? "Plan actual" : "Plan activo")
                            .font(.custom("DM Sans", size: 14))
                            .foregroundColor(.white.opacity(0.6))
                        if let renewal = sub.renewalDate {
                            Text("Renueva: \(renewal)")
                                .font(.custom("DM Sans", size: 12))
                                .foregroundColor(.white.opacity(0.5))
                        }
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity)
                    .background(RoundedRectangle(cornerRadius: 20).fill(.white.opacity(0.1)))
                    .padding(.horizontal, 16)

                    // Benefits
                    if !sub.benefits.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Beneficios")
                                .font(.custom("DynaPuff", size: 16))
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 16)

                            ForEach(Array((sub.benefits as? [SharedLogic.PlanBenefit] ?? []).enumerated()), id: \.offset) { _, benefit in
                                HStack(spacing: 12) {
                                    Image(systemName: benefit.included ? "checkmark.circle.fill" : "xmark.circle")
                                        .foregroundColor(benefit.included ? Color(hex: "34C759") : .white.opacity(0.3))
                                    Text(benefit.name)
                                        .font(.custom("DM Sans", size: 14))
                                        .foregroundColor(benefit.included ? .white : .white.opacity(0.4))
                                    Spacer()
                                    if benefit.isPremium {
                                        Text("PREMIUM")
                                            .font(.custom("DM Sans", size: 10))
                                            .fontWeight(.bold)
                                            .foregroundColor(Color(hex: "FFC93C"))
                                            .padding(.horizontal, 6)
                                            .padding(.vertical, 2)
                                            .background(Capsule().fill(Color(hex: "FFC93C").opacity(0.2)))
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                        }
                    }

                    // Upgrade button for free plan
                    if isFree(sub) {
                        Button {
                            // Future: open upgrade flow
                        } label: {
                            HStack {
                                Text("👑")
                                Text("Mejorar a Premium")
                                    .font(.custom("DM Sans", size: 16))
                                    .fontWeight(.bold)
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [Color(hex: "FFC93C"), Color(hex: "FF9500")]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .padding(.horizontal, 16)
                    }
                } else {
                    VStack(spacing: 12) {
                        Text("💳").font(.system(size: 48))
                        Text("Información no disponible")
                            .font(.custom("DM Sans", size: 15))
                            .foregroundColor(.white.opacity(0.6))
                    }
                    .padding(.top, 60)
                }
            }
            .padding(.bottom, 20)
        }
    }
}
