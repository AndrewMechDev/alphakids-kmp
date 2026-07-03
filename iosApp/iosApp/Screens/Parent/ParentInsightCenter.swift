import SwiftUI
import SharedLogic

struct ParentInsightCenter: View {
    let children: [SharedLogic.ChildSummary]
    let activities: [SharedLogic.ChildActivity]
    let onChildTap: (String) -> Void

    private var totalWords: Int32 {
        children.reduce(0) { $0 + $1.wordsLearned }
    }

    private var totalStars: Int32 {
        children.reduce(0) { $0 + $1.stars }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // KPI Grid
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    KPICard(emoji: "👶", value: "\(children.count)", label: "Hijos")
                    KPICard(emoji: "📚", value: "\(totalWords)", label: "Palabras")
                    KPICard(emoji: "⭐", value: "\(totalStars)", label: "Estrellas")
                    KPICard(emoji: "📊", value: "\(activities.count)", label: "Actividades")
                }
                .padding(.horizontal, 16)

                // Children summary
                if !children.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Hijos")
                            .font(.custom("DynaPuff", size: 16))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)

                        ForEach(children, id: \.id) { child in
                            Button { onChildTap(child.id) } label: {
                                HStack(spacing: 12) {
                                    Text("👦").font(.system(size: 20))
                                    Text(child.name)
                                        .font(.custom("DM Sans", size: 15))
                                        .fontWeight(.semibold)
                                        .foregroundColor(.white)
                                    Spacer()
                                    Text("Nivel \(child.level)")
                                        .font(.custom("DM Sans", size: 12))
                                        .foregroundColor(.white.opacity(0.6))
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.white.opacity(0.4))
                                }
                                .padding(12)
                                .background(RoundedRectangle(cornerRadius: 12).fill(.white.opacity(0.08)))
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.horizontal, 16)
                    }
                }

                // Activity timeline
                if !activities.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Actividad reciente")
                            .font(.custom("DynaPuff", size: 16))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)

                        ForEach(Array(activities.prefix(10).enumerated()), id: \.offset) { index, activity in
                            HStack(spacing: 12) {
                                VStack(spacing: 0) {
                                    Circle().fill(Color(hex: "3B7DF6")).frame(width: 8, height: 8)
                                    if index < min(9, activities.count - 1) {
                                        Rectangle().fill(.white.opacity(0.15)).frame(width: 2)
                                    }
                                }
                                .frame(width: 8)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(activity.date)
                                        .font(.custom("DM Sans", size: 11))
                                        .foregroundColor(.white.opacity(0.5))
                                    Text(activity.description_)
                                        .font(.custom("DM Sans", size: 14))
                                        .foregroundColor(.white)
                                    if !activity.detail.isEmpty {
                                        Text(activity.detail)
                                            .font(.custom("DM Sans", size: 12))
                                            .foregroundColor(.white.opacity(0.5))
                                    }
                                }
                                Spacer()
                            }
                            .padding(.vertical, 6)
                            .padding(.horizontal, 20)
                        }
                    }
                }
            }
            .padding(.bottom, 20)
        }
    }
}

private struct KPICard: View {
    let emoji: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 6) {
            Text(emoji).font(.system(size: 28))
            Text(value)
                .font(.custom("DynaPuff", size: 22))
                .fontWeight(.bold)
                .foregroundColor(.white)
            Text(label)
                .font(.custom("DM Sans", size: 12))
                .foregroundColor(.white.opacity(0.6))
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(RoundedRectangle(cornerRadius: 16).fill(.white.opacity(0.1)))
    }
}
