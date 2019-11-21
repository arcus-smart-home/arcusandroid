/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.camera

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.activities.LaunchActivity
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

class CameraActivity : AppCompatActivity() {
    private var allowBackPress by Delegates.notNull<Boolean>()
    private val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
    private val receiver = ScreenReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiver, filter)

        supportActionBar?.hide()
        allowBackPress = intent.getBooleanExtra(ALLOW_BACK_PRESS, true)

        try {
            if (shouldUseCamera2Api()) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Camera2Fragment.newInstance())
                .commit()
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, Camera1Fragment.newInstance())
                        .commit()
            }
        } catch (exception: Exception) {
            logger.info("Exception! ", exception)
            finish()
        }
    }

    override fun onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_through_bottom)
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun shouldUseCamera2Api() : Boolean {
        try {
            val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val idList = manager.cameraIdList
            var cameraApi2 = true
            if (idList.isEmpty()) {
                cameraApi2 = false
            } else {
                for (id in idList) {
                    if (id.trim { it <= ' ' }.isEmpty()) {
                        cameraApi2 = false
                        break
                    }
                    val characteristics = manager.getCameraCharacteristics(id)

                    val supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        cameraApi2 = false
                        break
                    }
                }
            }
            return cameraApi2
        } catch (e: Exception) {
            return false
        }
    }

    override fun onResume() {
        if(receiver.screenWasOn){   // Coming back from screen off,
            if(ArcusApplication.shouldReload()){
                val intent = Intent(this, LaunchActivity::class.java)
                startActivity(intent)
                finish()

                Runtime.getRuntime().exit(0)
            }
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CameraActivity::class.java)

        private const val ALLOW_BACK_PRESS = "ALLOW_BACK_PRESS"

        @JvmStatic
        @JvmOverloads
        fun getLaunchIntent(
                context: Context,
                allowBackPress : Boolean = true
        ) : Intent {

            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra(ALLOW_BACK_PRESS, allowBackPress)

            return intent
        }
    }

}
