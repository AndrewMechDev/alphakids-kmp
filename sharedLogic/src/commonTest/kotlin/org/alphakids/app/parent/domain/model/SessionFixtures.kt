package org.alphakids.app.parent.domain.model

import org.alphakids.app.data.remote.dto.StudentProgressDto

internal fun child(id: String) = ChildSummary(
    id = id,
    name = "Niño $id",
    avatarSeed = id,
    level = 1,
    rank = "Explorador",
    lastActivity = "",
    wordsLearned = 0,
    stars = 0,
)

internal fun progress(studentId: String) = StudentProgressDto(
    id = "progress-$studentId",
    studentId = studentId,
    coinsBalance = 40,
    totalStars = 12,
    wordsCompleted = 5,
    currentLevel = 2,
    currentRankId = "rank-1",
    createdAt = "2026-10-01T00:00:00Z",
    updatedAt = "2026-10-01T00:00:00Z",
)
