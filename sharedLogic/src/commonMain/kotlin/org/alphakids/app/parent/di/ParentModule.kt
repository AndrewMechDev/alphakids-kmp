package org.alphakids.app.parent.di

import org.alphakids.app.parent.data.mock.MockParentRepository
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.koin.dsl.module

val parentModule = module {
    single<ParentRepository> { MockParentRepository() }
}
