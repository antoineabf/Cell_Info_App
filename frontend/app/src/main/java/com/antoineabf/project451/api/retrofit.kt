package com.antoineabf.project451.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.antoineabf.project451.api.model.CellData
import com.antoineabf.project451.api.model.Statistics
import com.antoineabf.project451.api.model.infoForStat

object CellDataService {
    private const val API_URL: String = "http://10.169.11.27:5000"
    fun CellDataApi():Cell {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(Cell::class.java);
    }
    interface Cell {
        @POST("/cellData")
        fun add_cell_data(@Body cellData: CellData): Call<Any>
        @POST("/statistics")
        fun get_statistics(@Body info: infoForStat): Call<Statistics>
    }
}