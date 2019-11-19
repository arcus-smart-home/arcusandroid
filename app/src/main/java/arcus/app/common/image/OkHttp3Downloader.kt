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
package arcus.app.common.image

import android.net.Uri
import com.squareup.picasso.Downloader
import com.squareup.picasso.NetworkPolicy
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.IOException
import okhttp3.Request.Builder as RequestBuilder

class OkHttp3Downloader(private val client: OkHttpClient) : Downloader {
    @Throws(IOException::class)
    override fun load(uri: Uri, networkPolicy: Int): Downloader.Response {
        var cacheControl: CacheControl? = null
        if (networkPolicy != 0) {
            cacheControl = if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                CacheControl.FORCE_CACHE
            } else {
                val builder = CacheControl.Builder()
                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                    builder.noCache()
                }
                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                    builder.noStore()
                }
                builder.build()
            }
        }

        val builder = RequestBuilder().url(uri.toString())
        if (cacheControl != null) {
            builder.cacheControl(cacheControl)
        }

        val response = client.newCall(builder.build()).execute()
        if (!response.isSuccessful) {
            response.body?.close()
            throw Downloader.ResponseException(
                    "${response.code} ${response.message}",
                    networkPolicy,
                    response.code
            )
        }

        val fromCache = response.cacheResponse != null

        val responseBody = response.body!!
        return Downloader.Response(responseBody.byteStream(), fromCache, responseBody.contentLength())
    }

    override fun shutdown() {
        val cache = client.cache
        if (cache != null) {
            try {
                cache.close()
            } catch (ignored: IOException) {
            }

        }
    }
}
