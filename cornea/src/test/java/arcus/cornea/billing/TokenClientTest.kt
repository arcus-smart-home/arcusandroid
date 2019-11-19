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

import arcus.cornea.KFixtures
import com.iris.client.exception.ErrorResponseException

import org.junit.Before
import org.junit.Test

import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicReference

import com.google.common.truth.Truth.assertThat
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.fail

class TokenClientTest {
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var interceptor: SettableInterceptor
    private lateinit var tokenClient: TokenClient

    private val baseTokenRequest: BillingTokenRequest
        get() {
            val tokenRequest = BillingTokenRequest()
            tokenRequest.setTokenUrl("http://www.google.com/")
            tokenRequest.publicKey = "PublicKey"

            return tokenRequest
        }

    @Before
    fun setUp() {
        interceptor = SettableInterceptor()
        okHttpClient = OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build()

        tokenClient = TokenClient(okHttpClient)
    }

    @Test
    fun successfulTokenGenerated() {
        interceptor.setResponseString(KFixtures.loadString("/billing/success.json"))

        val future = tokenClient.getBillingToken(baseTokenRequest)
        val response = future.get()

        assertThat(future.isError).isFalse()
        assertThat(response).isEqualTo("1234567890")
    }

    @Test
    fun failureTokenGenerated() {
        interceptor.setResponseString(KFixtures.loadString("/billing/error.json"))

        val future = tokenClient.getBillingToken(baseTokenRequest)
        try {
            future.get()
            fail("Should throw an ExecutionException")
        } catch (ex: ExecutionException) {
            if (ex.cause is ErrorResponseException) {
                val realEx = ex.cause as ErrorResponseException

                assertThat(realEx.code)
                        .isEqualTo("invalid-parameter")
                assertThat(realEx.errorMessage)
                        .isEqualTo("Number is not a valid credit card number")
            } else {
                fail("Should be caused by an ErrorResponseException")
            }
        }

    }

    private class SettableInterceptor : Interceptor {
        private val response404 = Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("Error / Not Found.")

        private val responseRef = AtomicReference(response404)

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            return responseRef.getAndUpdate { _ -> response404 }
                    .request(chain.request())
                    .build()
        }

        fun setResponseString(responseString: String) {
            responseRef.set(
                    Response.Builder()
                            .code(200)
                            .message("Success")
                            .protocol(Protocol.HTTP_1_1)
                            .body(responseString.toResponseBody("text/json".toMediaTypeOrNull()))
            )
        }
    }
}
