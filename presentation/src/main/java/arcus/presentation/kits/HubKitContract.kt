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
package arcus.presentation.kits

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import arcus.cornea.presenter.BasePresenterContract

interface HubKitView {

    fun onHubKitsReceived(hubs: List<ProductAddModel>)
}

interface HubKitPresenter : BasePresenterContract<HubKitView> {

    fun showHubKits(context: Context)
}

data class KitHubProductResults(
    @SerializedName("results")
    val kitHubProductGroups: List<KitHubProductGroup>
)

data class KitHubProductGroup(
    @SerializedName("groupName")
    val groupName: String,

    @SerializedName("products")
    val products: List<KitHubProduct>
)

data class KitHubProduct(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("canUseBle")
    val canUseBle: Boolean
)

sealed class ProductAddModel {
    abstract val name: String
}

data class ProductDataModel(
        override val name: String,
        val id: String,
        val canUseBle: Boolean
) : ProductAddModel(), Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            (source.readInt() == 1)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(id)
        writeValue(canUseBle)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ProductDataModel> = object :
                Parcelable.Creator<ProductDataModel> {
            override fun createFromParcel(source: Parcel): ProductDataModel =
                    ProductDataModel(source)

            override fun newArray(size: Int): Array<ProductDataModel?> = arrayOfNulls(size)
        }
    }
}

data class ProductHeaderModel(
        override val name: String
) : ProductAddModel(), Parcelable {
    constructor(source: Parcel) : this(
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ProductHeaderModel> = object :
                Parcelable.Creator<ProductHeaderModel> {
            override fun createFromParcel(source: Parcel): ProductHeaderModel =
                    ProductHeaderModel(source)

            override fun newArray(size: Int): Array<ProductHeaderModel?> = arrayOfNulls(size)
        }
    }
}


