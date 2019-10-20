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
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Protocol
import com.squareup.okhttp.Response
import com.squareup.okhttp.ResponseBody

import org.junit.Before
import org.junit.Test

import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicReference

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail

class TokenClientTest {
    private val okHttpClient = OkHttpClient()

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
        okHttpClient.interceptors().clear()
        okHttpClient.interceptors().add(interceptor)

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
                            .protocol(Protocol.HTTP_1_1)
                            .body(ResponseBody.create(MediaType.parse("text/json"), responseString))
            )
        }
    }
}