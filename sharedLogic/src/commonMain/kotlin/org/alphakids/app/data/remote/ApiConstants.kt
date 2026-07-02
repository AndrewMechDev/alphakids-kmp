package org.alphakids.app.data.remote

/**
 * Base URL and endpoint paths for the AlphaKids API.
 *
 * Override [BASE_URL] at build time via Gradle buildConfigField or
 * environment variable for different environments (dev/staging/prod).
 */
object ApiConstants {

    /** Default to local dev server. Change this for production builds. */
    const val BASE_URL: String = "http://10.0.2.2:3000"

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

    // ── Store ──
    fun storePetsCatalog(studentId: String) = "students/$studentId/store/catalogs/pets"
    fun storeAccessoriesCatalog(studentId: String) = "students/$studentId/store/catalogs/accessories"
    fun storeBuyPet(studentId: String) = "students/$studentId/store/pets/buy"
    fun storeBuyAccessory(studentId: String) = "students/$studentId/store/accessories/buy"
}
