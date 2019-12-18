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
import arcus.cornea.presenter.KBasePresenter
import com.google.gson.Gson

class HubKitPresenterImpl : KBasePresenter<HubKitView>(), HubKitPresenter {
    private val gsonInstance = Gson()
    private val kitJson = """
{
  "results": [
    {
      "groupName": "Kits",
      "products": [
        {
          "productId": "kitsafesec",
          "productName": "Safe & Secure Starter Kit",
          "canUseBle": true
        },
        {
          "productId": "kitpromon",
          "productName": "Pro Monitoring Starter Kit"
        }
      ]
    },
    {
      "groupName": "Hubs",
      "products": [
        {
          "productId": "dee001",
          "productName": "Wi-Fi Smart Hub",
          "canUseBle": true
        },
        {
          "productId": "dee000",
          "productName": "Smart Hub"
        }
      ]
    }
  ]
}
    """

    override fun showHubKits(context: Context) {
        // API call to retrieve data will replace fake data
        onlyIfView { view ->
            view.onHubKitsReceived(parseHubKitResponse(kitJson))
        }
    }

    private fun parseHubKitResponse(json: String): List<ProductAddModel> = gsonInstance
        .fromJson(json, KitHubProductResults::class.java)
        .kitHubProductGroups
        .flatMap { productGroups ->
            val products = mutableListOf<ProductAddModel>(ProductHeaderModel(productGroups.groupName))
            productGroups
                .products
                .mapTo(products) { product ->
                    ProductDataModel(
                        product.productName,
                        product.productId,
                        product.canUseBle
                    )
                }
        }
}
