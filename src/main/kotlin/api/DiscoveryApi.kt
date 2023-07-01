package api

import models.AuthentificationResponse
import models.ApiSearchData
import models.ApiSearchResponse
import models.User
import retrofit2.Call
import retrofit2.http.*

interface DiscoveryApi {
    @GET("users")
    suspend fun getUsers(): Call<List<User>>

//    @POST("/token")
//    @JvmSuppressWildcards
//    suspend fun postAuthRequest(@Body body: RequestBody): Call<AuthentificationResponse>

    @Headers(
        "Content-Type: application/x-www-form-urlencoded"
    )
    @FormUrlEncoded
    @POST("/api/token")
    fun authenticateUser(
        @Field("grant_type") grant_type: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AuthentificationResponse>

    @Headers(
        "Content-Type: application/json"
    )
    @GET("data/search")
    fun apiGetDataByQuery(
        @Query("query") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("results_id") results_id: String,
//        @Query("delete") delete: Boolean
    ): Call<List<ApiSearchResponse>>


}