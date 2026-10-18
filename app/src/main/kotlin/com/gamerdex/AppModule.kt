package com.gamerdex

import android.content.Context
import com.gamerdex.core.domain.MetadataRepository
import com.gamerdex.core.network.NetworkFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun evercade(@ApplicationContext context:Context):com.gamerdex.core.domain.EvercadeRepository = com.gamerdex.core.network.EvercadeCatalog.create(context)
    @Provides @Singleton fun barcodes(): com.gamerdex.core.domain.BarcodeRepository = com.gamerdex.core.network.ScanDexRepository.create(BuildConfig.SCANDEX_API_TOKEN)
    @Provides @Singleton fun metadata(@ApplicationContext context: Context): MetadataRepository = NetworkFactory.create(context, BuildConfig.RAWG_API_KEY)
}
