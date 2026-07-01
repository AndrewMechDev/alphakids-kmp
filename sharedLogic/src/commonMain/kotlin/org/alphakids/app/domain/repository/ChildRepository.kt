package org.alphakids.app.domain.repository

import org.alphakids.app.domain.model.ChildProfile

interface ChildRepository {
    suspend fun getProfiles(): List<ChildProfile>
    suspend fun getProfileById(id: String): ChildProfile?
    suspend fun saveProfile(profile: ChildProfile)
}
