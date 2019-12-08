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
package arcus.app.pairing.hub.customization

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import arcus.app.common.utils.inflate
import arcus.app.R
import arcus.app.common.fragment.BackPressInterceptor
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.image.ImageCategory
import arcus.app.common.image.ImageManager
import arcus.app.common.image.ImageRepository
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.pairing.hub.V3HubSuccessFragment
import arcus.presentation.pairing.hub.HubKitCheckPresenterImpl
import arcus.presentation.pairing.hub.HubKitCheckView
import arcus.presentation.pairing.hub.ModelNameAndPhotoPresenterImpl
import arcus.presentation.pairing.hub.ModelNameAndPhotoView
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso

class HubNameAndPictureFragment : Fragment(),
    ModelNameAndPhotoView,
    HubKitCheckView,
    BackPressInterceptor {
    private lateinit var deviceName : EditText
    private lateinit var inputFieldContainer : TextInputLayout
    private lateinit var nextButton : Button
    private lateinit var productImage : ImageView
    private lateinit var cameraImage : ImageView
    private lateinit var progressBar : ProgressBar

    private var fragmentContainerHolder: FragmentContainerHolder? = null
    private var hasKitItems = false

    private val presenter = ModelNameAndPhotoPresenterImpl()
    private val hubKitPresenter = HubKitCheckPresenterImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return container?.inflate(R.layout.fragment_hub_name_and_photo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceName = view.findViewById(R.id.input_field)
        nextButton = view.findViewById(R.id.next_btn)
        productImage = view.findViewById(R.id.product_image)
        cameraImage = view.findViewById(R.id.camera_image)
        progressBar = view.findViewById(R.id.progress_bar)

        nextButton.setOnClickListener { _ ->
            if (deviceName.text.isNullOrBlank()) {
                inputFieldContainer.error = getString(R.string.missing_device_name)
            } else {
                inputFieldContainer.error = null
                deviceName.clearFocus()
                deviceName.isEnabled = false

                progressBar.visibility = View.VISIBLE

                nextButton.isEnabled = false

                presenter.setNameForHub(deviceName.text.toString())
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContainerHolder = context as FragmentContainerHolder?
    }

    override fun onStart() {
        super.onStart()
        presenter.setView(this)
        presenter.loadHubName()

        hubKitPresenter.setView(this)
        hubKitPresenter.checkIfHubHasKitItems()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.clearView()
        hubKitPresenter.clearView()
    }

    override fun onBackPressed(): Boolean = true // Just consume

    override fun showName(name: String, placeId: String, deviceId: String) {
        activity?.title = name

        deviceName.setText(name, TextView.BufferType.EDITABLE)
        cameraImage.setOnClickListener {
            if (progressBar.visibility == View.GONE) {
                ImageManager
                    .with(context)
                    .putUserGeneratedDeviceImage(placeId, deviceId)
                    .fromCameraOrGallery()
                    .withTransform(CropCircleTransformation())
                    .into(productImage)
                    .execute()
            }
        }

        context?.let {
            if (ImageRepository.imageExists(
                    it,
                    ImageCategory.DEVICE_LARGE,
                    placeId,
                    deviceId
                )) {

                Picasso
                    .with(it)
                    .load(
                        ImageRepository.getUriForImage(
                            it,
                            ImageCategory.DEVICE_LARGE,
                            placeId,
                            deviceId
                        )
                    )
                    .transform(CropCircleTransformation())
                    .noFade()
                    .error(R.drawable.v3hub_custom_190x190)
                    .into(productImage)
            }
        }
    }

    override fun onHubHasKitItems(hasItems: Boolean) {
        hasKitItems = hasItems
    }

    override fun saveSuccessful() {
        proceedToNext()
    }

    override fun saveUnsuccessful() {
        // Swallow errors
        proceedToNext()
    }

    private fun proceedToNext() {
        nextButton.isEnabled = true
        deviceName.isEnabled = true
        progressBar.visibility = View.GONE
        fragmentContainerHolder?.replaceFragmentContainerWith(V3HubSuccessFragment.newInstance(hasKitItems))
    }

    companion object {
        @JvmStatic
        fun newInstance() = HubNameAndPictureFragment()
    }
}
