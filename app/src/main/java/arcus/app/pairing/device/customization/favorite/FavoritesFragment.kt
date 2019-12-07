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
package arcus.app.pairing.device.customization.favorite

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import arcus.app.R
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.favorite.FavoritesPresenter
import arcus.presentation.pairing.device.customization.favorite.FavoritesPresenterImpl
import arcus.presentation.pairing.device.customization.favorite.FavoritesView
import org.slf4j.LoggerFactory

class FavoritesFragment : Fragment(),
    TitledFragment,
    FavoritesView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep
    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private lateinit var favoritesImage: ImageView
    private lateinit var favoritesTitle: ScleraTextView
    private lateinit var favoritesDescription: ScleraTextView
    private lateinit var favoritesInfo: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private lateinit var mCallback: CustomizationNavigationDelegate
    private val presenter : FavoritesPresenter =
        FavoritesPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = bundle.getParcelable(ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = bundle.getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_to_favorites, container, false)

        favoritesImage = view.findViewById(R.id.favorite_image)
        favoritesTitle = view.findViewById(R.id.favorites_title)
        favoritesDescription = view.findViewById(R.id.favorites_desc)
        favoritesInfo = view.findViewById(R.id.favorites_info)
        nextButton = view.findViewById(R.id.next_button)
        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            mCallback.cancelCustomization()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            try {
                mCallback = it as CustomizationNavigationDelegate
            } catch (exception: ClassCastException){
                logger.debug(it.toString() +
                        " must implement CustomizationNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress)
    }

    override fun showDevice(isFavorite: Boolean) {
        // Deviates from standards:
        // uses "info" for "description"  and hard-coded "info" strings (if favorite or not)

        favoritesTitle.text = customizationStep.title ?: getString(R.string.favorites_title_text)
        favoritesDescription.text = customizationStep.info ?: getString(R.string.favorites_desc)

        if (isFavorite) {
            favoritesInfo.text = getString(R.string.favorites_info_remove)
            favoritesImage.setImageResource(R.drawable.heart_teal_fill_81x75)
        } else {
            favoritesInfo.text = getString(R.string.favorites_info_add)
            favoritesImage.setImageResource(R.drawable.heart_teal_unfill_81x75)
        }

        favoritesImage.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.favorite_heart_anim))
            showDevice(!isFavorite)
        }

        nextButton.text = getString(nextButtonText)
        nextButton.setOnClickListener {
            presenter.favorite(isFavorite)
            mCallback.navigateForwardAndComplete(CustomizationType.FAVORITE)
        }

        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
        } else {
            cancelButton.visibility = View.GONE
        }

    }

    override fun showError(throwable: Throwable) {
        Log.e("Favorites Customization", "Error Received", throwable)
    }

    override fun getTitle() = customizationStep.header ?: getString(R.string.favorites_header)

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(FavoritesFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String, step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : FavoritesFragment {
            val fragment =
                FavoritesFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
                args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
