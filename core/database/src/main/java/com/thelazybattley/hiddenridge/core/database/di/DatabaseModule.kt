package com.thelazybattley.hiddenridge.core.database.di

import android.content.Context
import androidx.room.Room
import com.thelazybattley.hiddenridge.core.database.DatabaseMigrations
import com.thelazybattley.hiddenridge.core.database.HiddenRidgeDatabase
import com.thelazybattley.hiddenridge.core.database.dao.BookingDao
import com.thelazybattley.hiddenridge.core.database.dao.HouseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    private const val DATABASE_NAME = "hidden_ridge.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HiddenRidgeDatabase =
        Room.databaseBuilder(context, HiddenRidgeDatabase::class.java, DATABASE_NAME)
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

    @Provides
    fun provideHouseDao(database: HiddenRidgeDatabase): HouseDao = database.houseDao()

    @Provides
    fun provideBookingDao(database: HiddenRidgeDatabase): BookingDao = database.bookingDao()
}
