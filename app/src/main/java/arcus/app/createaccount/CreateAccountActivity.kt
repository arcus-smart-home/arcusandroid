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
package arcus.app.createaccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View

import arcus.app.R
import arcus.app.activities.BaseActivity
import arcus.app.activities.LaunchActivity
import arcus.app.createaccount.almostfinished.AlmostFinishedFragmentV2
import arcus.app.createaccount.emailandpassword.EmailPasswordEntryFragment
import arcus.app.createaccount.emailsent.EmailSentFragment
import arcus.app.createaccount.nameandphone.NameAndPhoneEntryFragment

class CreateAccountActivity : BaseActivity(), CreateAccountFlow {
    private lateinit var loadingLayout : View
    private var backAllowed = true
    private var isLoading   = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        setSupportActionBar(findViewById(R.id.toolbar))
        hideBackButton()

        if (supportFragmentManager.fragments.size == 0) {
            val transaction = supportFragmentManager.beginTransaction()
            when {
                intent.getBooleanExtra(ARG_SHOW_ALMOST_FINISHED, false) -> {
                    val name = intent.getStringExtra(ARG_PERSON_NAME) ?: ""
                    val email = intent.getStringExtra(ARG_PERSON_EMAIL) ?: ""
                    transaction.replace(R.id.fragment_container, AlmostFinishedFragmentV2.newInstance(name, email))
                }
                intent.getBooleanExtra(ARG_SHOW_EMAIL_SENT, false) -> {
                    val address = intent.getStringExtra(ARG_PERSON_ADDRESS) ?: ""
                    transaction.replace(R.id.fragment_container, EmailSentFragment.newInstance(address))
                }
                else -> {
                    transaction.replace(R.id.fragment_container, NameAndPhoneEntryFragment.newInstance())
                }
            }

            transaction.addToBackStack(null).commit()
            supportFragmentManager.executePendingTransactions()
        }

        loadingLayout = findViewById(R.id.loading_layout)
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        if (backAllowed && !isLoading) {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStackImmediate()
            } else {
                finishFlow()
            }
        }
    }

    override fun finish() {
        startActivity(Intent(this, LaunchActivity::class.java))
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_through_bottom)
    }

    override fun nextFrom(here: Fragment) {
        when (here) {
            is NameAndPhoneEntryFragment -> {
                replaceFragment(EmailPasswordEntryFragment.newInstance(here.userInfo))
            }
            is EmailPasswordEntryFragment -> {
                replaceFragment(EmailSentFragment.newInstance(here.personAddress))
            }
            else -> { /* No-Op */ }
        }
    }

    private fun replaceFragment(fragment: Fragment, backStackName: String? = null) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(backStackName)
            .commit()
        supportFragmentManager.executePendingTransactions()
    }

    override fun finishFlow() {
        finish()
    }

    override fun showBackButton() {
        backAllowed = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun allowBackButton() {
        backAllowed = true
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun hideBackButton() {
        backAllowed = false
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun loading(loading: Boolean) {
        isLoading = loading
        loadingLayout.visibility = if (loading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val ARG_PERSON_ADDRESS       = "ARG_PERSON_ADDRESS"
        private const val ARG_SHOW_EMAIL_SENT      = "ARG_SHOW_EMAIL_SENT"

        private const val ARG_SHOW_ALMOST_FINISHED = "ARG_SHOW_ALMOST_FINISHED"
        private const val ARG_PERSON_NAME          = "ARG_PERSON_NAME"
        private const val ARG_PERSON_EMAIL         = "ARG_PERSON_EMAIL"

        @JvmStatic
        fun forEmailSentLandingPage(context: Context, personAddress: String) : Intent {
            val intent = Intent(context, CreateAccountActivity::class.java)
            intent.putExtra(ARG_PERSON_ADDRESS, personAddress)
            intent.putExtra(ARG_SHOW_EMAIL_SENT, true)

            return intent
        }

        @JvmStatic
        fun forAlmostFinishedLandingPage(context: Context, name: String, email: String) : Intent {
            val intent = Intent(context, CreateAccountActivity::class.java)
            intent.putExtra(ARG_SHOW_ALMOST_FINISHED, true)
            intent.putExtra(ARG_PERSON_NAME, name)
            intent.putExtra(ARG_PERSON_EMAIL, email)

            return intent
        }
    }
}
