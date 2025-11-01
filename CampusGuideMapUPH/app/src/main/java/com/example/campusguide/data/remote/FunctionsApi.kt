package com.example.campusguide.data.remote

import okhttp3.ResponseBody
import retrofit2.http.*

interface FunctionsApi {
    @GET("listEvents")
    suspend fun listEvents(): List<EventDto>

    @POST("createEvent")
    suspend fun createEvent(
        @Header("Authorization") bearer: String,
        @Body e: EventDto
    ): EventDto

    @PATCH("updateEvent/{id}")
    suspend fun updateEvent(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body partial: Map<String, @JvmSuppressWildcards Any?>
    ): EventDto

    @DELETE("deleteEvent/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): ResponseBody

    @GET("listAllEvents")
    suspend fun listAllEvents(@Header("Authorization") bearer: String): List<EventDto>
}
