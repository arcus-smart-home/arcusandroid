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
package arcus.cornea.billing

import androidx.annotation.VisibleForTesting

import com.google.common.net.HttpHeaders
import com.iris.client.event.ClientFuture
import com.iris.client.event.SettableClientFuture
import com.iris.client.exception.ErrorResponseException
import com.iris.client.impl.ClientMessageSerializer

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class TokenClient @VisibleForTesting
internal constructor(private val okHttpClient: OkHttpClient) {
    constructor() : this(OkHttpClient())

    fun getBillingToken(request: BillingTokenRequest): ClientFuture<String> {
        check(!request.tokenURL.isNullOrBlank()) { "Token URL cannot be null" }
        check(!request.publicKey.isNullOrBlank()) { "Public Key cannot be null" }

        return doGetBillingToken(request, request.tokenURL)
    }

    private fun doGetBillingToken(
            billingInfoRequest: BillingTokenRequest,
            tokenURL: String
    ): ClientFuture<String> = SettableClientFuture<String>().also { future ->
        val requestBody = buildRequestBody(billingInfoRequest)
        val request = buildRequest(tokenURL, requestBody)

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                future.setError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val message = response.body!!.string().deserialize()
                    if (message.isError) {
                        throw ErrorResponseException(message.code, message.message)
                    } else {
                        future.setValue(message.id)
                    }
                } catch (ex: Exception) {
                    future.setError(ex)
                }
            }
        })
    }

    @VisibleForTesting
    internal fun buildRequest(
            tokenURL: String,
            requestBody: RequestBody
    ): Request = Request.Builder()
            .addHeader(HttpHeaders.ACCEPT, APP_XML)
            .addHeader(HttpHeaders.CONTENT_TYPE, APP_FORM)
            .url(tokenURL)
            .post(requestBody)
            .build()

    @VisibleForTesting
    internal fun buildRequestBody(
            billingInfoRequest: BillingTokenRequest
    ): RequestBody = FormBody.Builder().apply {
        for ((key, value) in billingInfoRequest.mappings) {
            add(key, value)
        }
    }.build()

    private fun String.deserialize(): RecurlyJSONResponse =
            ClientMessageSerializer.deserialize(this, RecurlyJSONResponse::class.java)

    companion object {
        private const val APP_XML = "application/xml"
        private const val APP_FORM = "application/x-www-form-urlencoded"
    }
}
