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
package arcus.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.MenuItem
import android.view.View

import arcus.app.R
import arcus.app.common.fragment.BackPressInterceptor
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.enterFromRightExitToRight
import kotlin.properties.Delegates
import android.view.WindowManager

/**
 * This activity act as a general purpose container for fragments so we don't have to have
 * a lot of Activitites to simply hold a fragment and navigate forward/backwards.
 *
 * This Activity will check for a connection to the platform when it is resumed and if one
 * is not present it will attempt to reconnect or error back to the login screen if it cannot.
 */
class GenericConnectedFragmentActivity : ConnectedActivity(), FragmentContainerHolder {
    private var currentFragment : Fragment? = null
    private var allowBackPress by Delegates.notNull<Boolean>()
    private var keepScreenOn by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_connected_fragment)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (intent.getBooleanExtra(SHOW_FULLSCREEN, false)) {
            findViewById<View>(R.id.app_bar_layout).visibility = View.GONE
        }

        keepScreenOn = intent.getBooleanExtra(KEEP_SCREEN_ON, false)

        allowBackPress = intent.getBooleanExtra(ALLOW_BACK_PRESS, true)

        try {
            val fragmentClass = intent.getSerializableExtra(FRAGMENT_CLASS) as Class<*>
            val fragmentInstance = fragmentClass.newInstance() as Fragment
            val arguments = intent.getBundleExtra(FRAGMENT_ARGUMENTS)
            if (arguments != null) {
                fragmentInstance.arguments = arguments
            }
            supportFragmentManager
                .addOnBackStackChangedListener {
                    currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                }

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragmentInstance)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            finish()
        }
    }

    public override fun onResume() {
        super.onResume()
        if(keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    public override fun onPause() {
        super.onPause()
        if(keepScreenOn) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (allowBackPress) {
            if (!fragmentConsumedBackPress(currentFragment)) {
                if (supportFragmentManager.backStackEntryCount < 2) {
                    supportFragmentManager.popBackStackImmediate()
                    super.onBackPressed()
                } else {
                    super.onBackPressed()
                }
            } /* else the fragment took care of the back press */
        }
    }

    private fun fragmentConsumedBackPress(fragment: Fragment?) : Boolean {
        return fragment is BackPressInterceptor && fragment.onBackPressed()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_through_bottom)
    }

    override fun replaceFragmentContainerWith(fragment: Fragment, addToBackStack: Boolean) {
        val tag = getTagName(fragment)
        val transaction = supportFragmentManager
            .beginTransaction()
            .enterFromRightExitToRight()
            .replace(R.id.fragment_container, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }

    override fun addToFragmentContainer(fragment: Fragment, addToBackStack: Boolean) {
        val tag = getTagName(fragment)
        val transaction = supportFragmentManager
            .beginTransaction()
            .enterFromRightExitToRight()
            .add(R.id.fragment_container, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }

    override fun showBackButtonOnToolbar(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
        allowBackPress = show // If this is off, and we're showing the button.. it doesn't work.
    }

    override fun setTitle(title: String) {
        this.title = title
    }

    private fun getTagName(fragment: Fragment) : String = fragment.javaClass.name

    companion object {
        private const val FRAGMENT_CLASS = "FRAGMENT_CLASS"
        private const val FRAGMENT_ARGUMENTS = "FRAGMENT_ARGUMENTS"
        private const val ALLOW_BACK_PRESS = "ALLOW_BACK_PRESS"
        private const val SHOW_FULLSCREEN = "SHOW_FULLSCREEN"
        private const val KEEP_SCREEN_ON = "KEEP_SCREEN_ON"

        @JvmStatic
        @JvmOverloads
        fun getLaunchIntent(
            context: Context,
            clazz: Class<out Fragment>,
            fragmentArguments: Bundle? = null,
            allowBackPress : Boolean = true,
            showFullscreen: Boolean = false,
            keepScreenOn: Boolean = false
        ) : Intent {

            val intent = Intent(context, GenericConnectedFragmentActivity::class.java)
            intent.putExtra(FRAGMENT_CLASS, clazz)
            intent.putExtra(FRAGMENT_ARGUMENTS, fragmentArguments)
            intent.putExtra(ALLOW_BACK_PRESS, allowBackPress)
            intent.putExtra(SHOW_FULLSCREEN, showFullscreen)
            intent.putExtra(KEEP_SCREEN_ON, keepScreenOn)

            return intent
        }
    }
}
