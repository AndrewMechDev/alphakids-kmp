package org.alphakids.app.parent.domain.model

enum class PlanType { FREE, PREMIUM }

data class SubscriptionInfo(
    val planType: PlanType = PlanType.FREE,
    val planName: String = "Gratuito",
    val isActive: Boolean = true,
    val renewalDate: String? = null,
    val benefits: List<PlanBenefit> = listOf(),
)

data class PlanBenefit(
    val name: String,
    val included: Boolean,
    val isPremium: Boolean,
)

data class PaymentHistory(
    val date: String,
    val description: String,
    val amount: String,
    val status: String,
)
