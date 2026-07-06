package org.alphakids.app.data.fake

import org.alphakids.app.domain.model.Grade
import org.alphakids.app.domain.model.Institution
import org.alphakids.app.domain.model.Section
import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.CreateChildRequest
import org.alphakids.app.parent.domain.model.CreateChildResult
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.model.PlanBenefit
import org.alphakids.app.parent.domain.model.PlanType
import org.alphakids.app.parent.domain.model.SubscriptionInfo
import org.alphakids.app.parent.domain.repository.ParentRepository

class FakeParentRepository : ParentRepository {

    private var childCounter = 3

    override suspend fun getChildren(): List<ChildSummary> = listOf(
        ChildSummary(id = "fake-child-1", name = "Sofía", avatarSeed = "sofia-seed", level = 3, rank = "Explorador", lastActivity = "2026-07-05", wordsLearned = 15, stars = 24),
        ChildSummary(id = "fake-child-2", name = "Mateo", avatarSeed = "mateo-seed", level = 1, rank = "Principiante", lastActivity = "2026-07-04", wordsLearned = 5, stars = 8),
    )

    override suspend fun getChildStats(childId: String): ChildStats = ChildStats(
        wordsLearned = 15, ocrCompleted = 8, spellingCompleted = 3, timePlayedMinutes = 45, coinsEarned = 120, starsEarned = 24, weeklyProgress = listOf(true, true, false, true, true, false, false),
    )

    override suspend fun getRecentActivity(): List<ChildActivity> = listOf(
        ChildActivity(date = "2026-07-05", description = "Sofía completó 'GATO' en Escaneo de Letras", type = "OCR_SCAN", detail = "+5 monedas"),
        ChildActivity(date = "2026-07-04", description = "Mateo aprendió la palabra 'CASA'", type = "WORD_LEARNED", detail = "+3 estrellas"),
        ChildActivity(date = "2026-07-03", description = "Sofía alimentó a su mascota Draco", type = "PET_FEED", detail = "Felicidad +10"),
    )

    override suspend fun getSubscription(): SubscriptionInfo = SubscriptionInfo(
        planType = PlanType.FREE, planName = "Gratuito", isActive = true, renewalDate = null,
        benefits = listOf(
            PlanBenefit(name = "Escaneo de Letras", included = true, isPremium = false),
            PlanBenefit(name = "Diccionario básico", included = true, isPremium = false),
            PlanBenefit(name = "1 mascota", included = true, isPremium = false),
            PlanBenefit(name = "Deletreo por voz", included = false, isPremium = true),
            PlanBenefit(name = "Mascotas ilimitadas", included = false, isPremium = true),
        ),
    )

    override suspend fun getFAQs(): List<FAQItem> = listOf(
        FAQItem(question = "¿Cómo funciona el Escaneo de Letras?", answer = "Tu hijo forma palabras con letras físicas y las escanea con la cámara del dispositivo."),
        FAQItem(question = "¿Qué son las monedas y estrellas?", answer = "Las monedas sirven para comprar mascotas y accesorios. Las estrellas miden el progreso general."),
        FAQItem(question = "¿Puedo agregar más de un hijo?", answer = "Sí, puedes agregar varios perfiles de hijos desde el panel de padres."),
    )

    override suspend fun submitContactForm(form: ContactForm): Boolean = true

    override suspend fun getPublicInstitutions(): List<Institution> = listOf(
        Institution(id = "inst-1", name = "Colegio San Martín", grades = listOf(
            Grade(id = "grade-1", name = "1er Grado", sections = listOf(Section(id = "sec-1a", name = "Sección A"), Section(id = "sec-1b", name = "Sección B"))),
            Grade(id = "grade-2", name = "2do Grado", sections = listOf(Section(id = "sec-2a", name = "Sección A"), Section(id = "sec-2b", name = "Sección B"))),
        )),
    )

    override suspend fun createChild(request: CreateChildRequest): CreateChildResult =
        CreateChildResult(id = "fake-child-${childCounter++}", verificationStatus = "VERIFIED", studentType = if (request.institutionId != null) "INSTITUTIONAL" else "FREEMIUM")
}
