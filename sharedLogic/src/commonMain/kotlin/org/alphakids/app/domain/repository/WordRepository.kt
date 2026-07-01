package org.alphakids.app.domain.repository

import org.alphakids.app.domain.model.Word

interface WordRepository {
    suspend fun getWords(): List<Word>
    suspend fun getWordById(id: String): Word?
}
