package com.example.tuninghub

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://truulcorreio1.hospedagemdesites.ws/tuninghub/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val instance: ApiService = retrofit.create(ApiService::class.java)
}
