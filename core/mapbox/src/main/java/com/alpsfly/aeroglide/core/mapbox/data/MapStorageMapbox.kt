package com.alpsfly.aeroglide.core.mapbox.data

import android.location.Location
import com.alpsfly.aeroglide.core.firebase.Response
import com.alpsfly.aeroglide.core.model.common.mapbox.Direction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import com.alpsfly.aeroglide.core.mapbox.BuildConfig
import okhttp3.*


class MapStorageMapbox : MapStorage {
    override suspend fun loadRoute(origin: Location, destination: Location): Flow<Response<Direction?>> = callbackFlow {
        val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("api.mapbox.com")
            .port(443)
            .addPathSegment("directions")
            .addPathSegment("v5")
            .addPathSegment("mapbox")
            .addPathSegment("cycling")
            .addQueryParameter("access_token", accessToken)
            .build()

        val formBody = FormBody.Builder()
            .add("coordinates", "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}")
            .add("alternatives", "false")
            .add("geometries", "geojson")
            .add("overview", "simplified")
            .add("steps", "false")
            .build()

        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url(httpUrl)
            .post(formBody)
            .build()
        val httpResponse = client.newCall(httpRequest).execute()
        val response = httpResponse.body.let {
                Response.Success(toJson(it.string()))
            }

        trySend(response).isSuccess
        awaitClose {
            httpResponse.body.close()
            httpResponse.close()
        }
    }

    private fun toJson(jsonString: String) : Direction {
        return Json.decodeFromString(jsonString)
    }
}
