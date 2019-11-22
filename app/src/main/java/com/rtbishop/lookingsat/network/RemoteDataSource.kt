package com.rtbishop.lookingsat.network

import com.rtbishop.lookingsat.repo.Transmitter
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.SequenceInputStream
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: TransmittersApi,
    private val client: OkHttpClient
) {

    fun fetchTleStream(): InputStream {
        val requestAmateur =
            Request.Builder().url("https://celestrak.com/NORAD/elements/amateur.txt").build()
        val requestWeather =
            Request.Builder().url("https://celestrak.com/NORAD/elements/weather.txt").build()
        val streamAmateur = client.newCall(requestAmateur).execute().body()?.byteStream()
        val streamWeather = client.newCall(requestWeather).execute().body()?.byteStream()
        return SequenceInputStream(streamAmateur, streamWeather)
    }

    suspend fun fetchTransmittersList(): List<Transmitter> {
        return api.fetchTransmittersList()
    }
}