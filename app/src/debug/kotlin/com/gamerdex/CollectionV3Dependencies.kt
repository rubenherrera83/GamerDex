package com.gamerdex

import com.gamerdex.core.domain.CollectionRepository
import com.gamerdex.ui.AppPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Debug-only entry point for instrumentation fixtures; absent from release builds. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CollectionV3Dependencies {
    fun collection(): CollectionRepository
    fun preferences(): AppPreferences
}
