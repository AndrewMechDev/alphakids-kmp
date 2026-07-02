package org.alphakids.app.studentpet.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.studentpet.data.StudentPetRepositoryImpl
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val studentPetModule: Module = module {
    single<StudentPetRepository> { StudentPetRepositoryImpl(get<AlphaKidsApiClient>()) }
}
