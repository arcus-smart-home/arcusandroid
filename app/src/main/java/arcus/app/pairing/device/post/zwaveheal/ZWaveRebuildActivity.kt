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
package arcus.app.pairing.device.post.zwaveheal

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import arcus.app.R
import arcus.app.activities.ConnectedActivity
import arcus.app.activities.DashboardActivity
import arcus.app.common.utils.commitAndExecute

interface FragmentFlow {
    /**
     * Navigates to the next [newFragment] in the Flow
     */
    fun navigateTo(newFragment: Fragment)

    /**
     * Navigates back to the previous place, if applicable
     */
    fun navigateBack()

    /**
     * Completes the current Flow
     */
    fun completeFlow()

    /**
     * Completes the current Flow with the specified [intent] by calling
     * startActivity on the received [intent].
     */
    fun flowToActivity(intent: Intent)
}

class ZWaveRebuildActivity : ConnectedActivity(), FragmentFlow {

    private fun FragmentTransaction.enterFromRightExitToRight() = apply {
        setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_z_wave_rebuild)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, EnterZWaveRebuildFragment.newInstance())
                .commit()
        supportFragmentManager.executePendingTransactions()
    }

    override fun onBackPressed() {
        // Do nothing to prevent Back Press
    }

    override fun navigateTo(newFragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .enterFromRightExitToRight()
                .replace(
                        R.id.container,
                        newFragment,
                        "ZW_REBUILD"
                )
                .commitAndExecute(this.supportFragmentManager)
    }

    override fun navigateBack() {
        // No Op
    }

    override fun completeFlow() {
        flowToActivity(DashboardActivity.getHomeFragmentIntent(this))
    }

    override fun flowToActivity(intent: Intent) {
        startActivity(intent)
    }

}
