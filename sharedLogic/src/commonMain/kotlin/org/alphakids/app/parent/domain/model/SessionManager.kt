package org.alphakids.app.parent.domain.model

import com.russhwolf.settings.Settings

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
        currentChild = child
        settings?.putString(KEY_ACTIVE_CHILD_ID, child.id)
    }

    fun clearSession() {
        currentChild = null
        settings?.remove(KEY_ACTIVE_CHILD_ID)
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
