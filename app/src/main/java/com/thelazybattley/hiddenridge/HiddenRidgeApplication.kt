package com.thelazybattley.hiddenridge

import android.app.Application
import com.thelazybattley.hiddenridge.core.data.sample.SampleDataSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class HiddenRidgeApplication : Application() {

    @Inject
    lateinit var sampleDataSeeder: SampleDataSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Until booking screens exist, the timeline is filled with the design's sample bookings.
        applicationScope.launch { sampleDataSeeder.seedIfEmpty() }
    }
}
