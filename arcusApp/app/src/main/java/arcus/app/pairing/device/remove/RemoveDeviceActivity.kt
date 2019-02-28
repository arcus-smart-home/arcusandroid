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
package arcus.app.pairing.device.remove

import android.content.Context
import android.content.Intent
import android.os.Bundle
import arcus.app.R
import arcus.app.activities.ConnectedActivity
import arcus.app.common.fragment.TitledFragment

class RemoveDeviceActivity : ConnectedActivity() {
    private var pairingDeviceAddress : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_device)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        pairingDeviceAddress = intent.getStringExtra(ARG_PAIRING_DEVICE_ADDRESS)

        supportFragmentManager
            .addOnBackStackChangedListener {
                val fragment = supportFragmentManager.findFragmentById(R.id.container)
                if (fragment is TitledFragment) {
                    title = fragment.getTitle()
                }

            }

        supportFragmentManager
                .beginTransaction()
                .replace(
                        R.id.container,
                        RemoveDeviceWarningFragment.newInstance(pairingDeviceAddress))
                .addToBackStack(null)
                .commit()
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"

        @JvmStatic
        fun newIntent(context: Context, pairingDeviceAddress: String) = Intent(
            context,
            RemoveDeviceActivity::class.java
        ).also {
            it.putExtra(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
        }
    }
}
