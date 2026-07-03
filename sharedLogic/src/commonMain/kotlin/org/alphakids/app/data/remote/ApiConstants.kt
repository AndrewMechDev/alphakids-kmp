package org.alphakids.app.data.remote

/**
 * Base URL and endpoint paths for the AlphaKids API.
 *
 * Override [BASE_URL] at build time via Gradle buildConfigField or
 * environment variable for different environments (dev/staging/prod).
 */
object ApiConstants {

    /** Production API. */
    const val BASE_URL: String = "https://api.alphakids.org.pe"

    // ── Auth ──
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val REFRESH = "auth/refresh"
    const val LOGOUT = "auth/logout"
    const val PROFILE = "auth/profile"
    const val FORGOT_PASSWORD = "auth/forgot-password"
    const val RESET_PASSWORD = "auth/reset-password"
    const val SETUP_PASSWORD = "auth/setup-password"

    // ── Tutors (parent-specific) ──
    const val TUTORS_CHILDREN = "tutors/children"

    // ── Students ──
    const val STUDENTS = "students"
    fun student(id: String) = "students/$id"
    fun studentAchievements(id: String) = "students/$id/achievements"
    fun studentPlayableWords(id: String) = "students/$id/playable-words"
    fun studentDictionary(id: String) = "students/$id/dictionary"
    fun studentInventory(id: String) = "students/$id/inventory"

    // ── Game Sessions ──
    const val GAME_SESSIONS_COMPLETE = "game-sessions/complete"

    // ── Pets ──
    fun pets(studentId: String) = "pets/$studentId"
    fun feedPet(petId: String) = "pets/$petId/feed"

    // ── Institutions ──
    /**
     * List active institutions with grades and sections.
     * GET /institutions/public
     *
     * Available to authenticated users with role 'admin', 'parent', or 'teacher'.
     * Returns [org.alphakids.app.data.remote.dto.PublicInstitutionDto].
     */
    const val INSTITUTIONS_PUBLIC = "institutions/public"

    // ── Store ──
    fun storePetsCatalog(studentId: String) = "students/$studentId/store/catalogs/pets"
    fun storeAccessoriesCatalog(studentId: String) = "students/$studentId/store/catalogs/accessories"
    fun storeBuyPet(studentId: String) = "students/$studentId/store/pets/buy"
    fun storeBuyAccessory(studentId: String) = "students/$studentId/store/accessories/buy"
}
