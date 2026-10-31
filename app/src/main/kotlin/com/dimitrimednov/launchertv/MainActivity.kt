package com.dimitrimednov.launchertv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dimitrimednov.launchertv.ui.cuarto.CityApp
import com.dimitrimednov.launchertv.ui.cuarto.CityViewModel

/** Pantalla de inicio de la tele del cuarto (responde al botón Home). */
class MainActivity : ComponentActivity() {
    private val vm: CityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(EXTRA_DEMO, false)) vm.enableDemo()
        setContent { CityApp(vm) }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    /** Home presionado estando ya en el launcher: vuelve al principio. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        vm.navigator.reset()
    }

    private companion object {
        /** `adb shell am start -n com.dimitrimednov.launchertv/.MainActivity --ez demo true` */
        const val EXTRA_DEMO = "demo"
    }
}
