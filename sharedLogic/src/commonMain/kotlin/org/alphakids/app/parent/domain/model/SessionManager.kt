package org.alphakids.app.parent.domain.model

import com.russhwolf.settings.Settings
import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.game.domain.model.GameSessionState

/**
 * In-memory session manager that holds the currently active child, plus the
 * active child's id persisted via [Settings] so the session can be restored
 * after Android kills the process in the background — the in-memory
 * [currentChild] alone doesn't survive that, which used to surface as the
 * app silently falling back to demo data (looking like a random profile
 * switch) on resume.
 *
 * This is a singleton used to share state between the onboarding wizard,
 * the home screen, and the parent dashboard within a single app session.
 *
 * It also owns the lifetime of the other per-child session holders
 * ([GameProgressManager], [AchievementAnalytics], [GameSessionState]): they
 * are not keyed by child, so they are wiped whenever the active child
 * changes or the session ends — otherwise the next child (or the next
 * parent on a shared device) would briefly see the previous one's coins
 * and history.
 */
object SessionManager {
    private var settings: Settings? = null

    var currentChild: ChildSummary? = null
        private set

    val hasActiveChild: Boolean get() = currentChild != null

    /** Call once at app startup (after Koin starts) to enable persistence. */
    fun init(settings: Settings) {
        this.settings = settings
    }

    fun setActiveChild(child: ChildSummary) {
        if (child.id != activeChildId()) clearChildState()
        currentChild = child
        settings?.putString(KEY_ACTIVE_CHILD_ID, child.id)
    }

    /** Ends the session: forgets the active child and everything cached for it. */
    fun clearSession() {
        currentChild = null
        settings?.remove(KEY_ACTIVE_CHILD_ID)
        clearChildState()
    }

    private fun clearChildState() {
        GameProgressManager.clear()
        AchievementAnalytics.clear()
        GameSessionState.clear()
    }

    /**
     * The last active child's id persisted from a prior session, or null if
     * none was saved. Used to re-hydrate [currentChild] after a process
     * restart wiped the in-memory value.
     */
    fun persistedActiveChildId(): String? = settings?.getStringOrNull(KEY_ACTIVE_CHILD_ID)

    /**
     * The active child's id, falling back to the persisted one when the
     * in-memory [currentChild] was wiped by a process restart. Use this for
     * anything that must reach the backend for the right child (e.g.
     * reporting a finished game) rather than [currentChild] alone.
     */
    fun activeChildId(): String? = currentChild?.id ?: persistedActiveChildId()

    private const val KEY_ACTIVE_CHILD_ID = "session_active_child_id"
}
