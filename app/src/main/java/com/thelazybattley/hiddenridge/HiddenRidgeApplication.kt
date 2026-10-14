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
class HiddenRidgeApplication : Application()
