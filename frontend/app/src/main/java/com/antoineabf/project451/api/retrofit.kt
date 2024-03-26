package com.antoineabf.project451.api

import com.antoineabf.project451.api.model.CellData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

object CellDataService {
    private const val API_URL: String = "http://10.0.2.2:5000"
    fun CellDataApi():CellData {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(CellData::class.java);
    }
    interface Exchange {
        @POST("/cellData")
        fun add_cell_data(): Call<CellData>
        @POST("/statistics")
        fun get_statistics(): Call<Any>
    }
}