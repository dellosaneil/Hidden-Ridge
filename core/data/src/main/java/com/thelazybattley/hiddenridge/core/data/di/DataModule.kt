package com.thelazybattley.hiddenridge.core.data.di

import com.thelazybattley.hiddenridge.core.data.repository.OfflineBookingRepository
import com.thelazybattley.hiddenridge.core.data.repository.OfflineHouseRepository
import com.thelazybattley.hiddenridge.core.data.time.SystemDateProvider
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindHouseRepository(repository: OfflineHouseRepository): HouseRepository

    @Binds
    abstract fun bindBookingRepository(repository: OfflineBookingRepository): BookingRepository

    @Binds
    abstract fun bindDateProvider(provider: SystemDateProvider): DateProvider

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
