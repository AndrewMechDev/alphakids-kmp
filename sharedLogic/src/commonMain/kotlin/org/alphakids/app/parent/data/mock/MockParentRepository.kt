package org.alphakids.app.parent.data.mock

import org.alphakids.app.parent.domain.model.ChildActivity
import org.alphakids.app.parent.domain.model.ChildStats
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.ContactForm
import org.alphakids.app.parent.domain.model.FAQItem
import org.alphakids.app.parent.domain.model.PlanBenefit
import org.alphakids.app.parent.domain.model.PlanType
import org.alphakids.app.parent.domain.model.SubscriptionInfo
import org.alphakids.app.parent.domain.repository.ParentRepository

class MockParentRepository : ParentRepository {
    private val children = listOf(
        ChildSummary("1", "Sofía", "sofia-seed", 3, "Hoja Verde 🍃", "Hoy: 2 palabras", 15, 8),
        ChildSummary("2", "Mateo", "mateo-seed", 1, "Semillita 🌱", "Ayer: 1 palabra", 5, 2),
    )

    override suspend fun getChildren(): List<ChildSummary> = children

    override suspend fun getChildStats(childId: String): ChildStats = ChildStats(
        wordsLearned = if (childId == "1") 15 else 5,
        ocrCompleted = if (childId == "1") 10 else 3,
        spellingCompleted = if (childId == "1") 5 else 2,
        timePlayedMinutes = if (childId == "1") 120 else 45,
        coinsEarned = if (childId == "1") 250 else 80,
        starsEarned = if (childId == "1") 8 else 2,
        weeklyProgress = listOf(true, true, false, true, false, false, true),
    )

    override suspend fun getRecentActivity(): List<ChildActivity> = listOf(
        ChildActivity("Hoy 10:30", "Sofía aprendió 'CASA'", "word", "3 estrellas ⭐⭐⭐"),
        ChildActivity("Hoy 09:15", "Mateo escaneó 'SOL'", "ocr", "2 estrellas ⭐⭐"),
        ChildActivity("Ayer 16:00", "Sofía completó 5 palabras", "achievement", "Nuevo récord 🎉"),
        ChildActivity("Ayer 14:30", "Mateo deletreó 'LUNA'", "spelling", "1 estrella ⭐"),
        ChildActivity("Lun 10:00", "Sofía obtuvo trofeo 'Primera palabra'", "achievement", "Trofeo desbloqueado 🏆"),
    )

    override suspend fun getSubscription(): SubscriptionInfo = SubscriptionInfo(
        planType = PlanType.FREE,
        planName = "Gratuito",
        isActive = true,
        benefits = listOf(
            PlanBenefit("OCR básico", true, false),
            PlanBenefit("1 mascota", true, false),
            PlanBenefit("Juegos básicos", true, false),
            PlanBenefit("Mascotas ilimitadas", false, true),
            PlanBenefit("Actividades avanzadas", false, true),
            PlanBenefit("Panel de padres detallado", false, true),
            PlanBenefit("Sin anuncios", false, true),
        ),
    )

    override suspend fun getFAQs(): List<FAQItem> = listOf(
        FAQItem("¿Cómo agrego un nuevo hijo?", "Ve al panel de padres, selecciona 'Crear perfil' y sigue los pasos del asistente."),
        FAQItem("¿Qué beneficios tiene Premium?", "Acceso a mascotas ilimitadas, actividades avanzadas, panel detallado y sin anuncios."),
        FAQItem("¿Cómo se calculan las monedas?", "Por cada palabra completada: 50 monedas. Por racha de 7 días: bonus de 100 monedas."),
        FAQItem("¿Puedo tener varios hijos?", "Sí, puedes crear perfiles ilimitados desde el panel de padres."),
        FAQItem("¿Cómo restablezco el progreso?", "Contacta a soporte desde esta misma sección y te ayudaremos."),
    )

    override suspend fun submitContactForm(form: ContactForm): Boolean = true
}
