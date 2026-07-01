package org.alphakids.app.domain.repository

import org.alphakids.app.domain.model.Pet

interface PetRepository {
    suspend fun getPets(): List<Pet>
    suspend fun getPetById(id: String): Pet?
}
