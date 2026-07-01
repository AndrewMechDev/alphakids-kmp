package org.alphakids.app.parent.domain.model

/**
 * Simple in-memory session manager that holds the currently active child.
 *
 * This is a singleton used to share state between the onboarding wizard,
 * the home screen, and the parent dashboard within a single app session.
 */
object SessionManager {
    var currentChild: ChildSummary? = null
        private set

    val hasActiveChild: Boolean get() = currentChild != null

    fun setActiveChild(child: ChildSummary) {
        currentChild = child
    }

    fun clearSession() {
        currentChild = null
    }
}
