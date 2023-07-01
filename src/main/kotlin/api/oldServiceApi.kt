package api


import models.ApiSearchData
import models.ApiSearchResponse
import network.RetrofitClient
import java.net.URL


interface oldServiceApi {

    companion object {

        fun apiGetToken(serverUrl: String, username: String, password: String): String? {
            val retrofit = RetrofitClient.getClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            return apiService.authenticateUser("password", username, password).execute().body()?.token

        }

        fun apiQueryData(serverUrl: String, query: String, verbose: Boolean): List<ApiSearchResponse>? {
            val discoveredData: MutableList<ApiSearchResponse> = mutableListOf()
            if (verbose) println("Requetage pour  ($query)")
            getAllDataFragment(serverUrl, query, 0, 100, "", discoveredData, verbose)
            return discoveredData
        }


        private fun getAllDataFragment(
            serverUrl: String,
            query: String,
            offset: Int,
            limit: Int,
            results_id: String,
            discoveredData: MutableList<ApiSearchResponse>,
            verbose: Boolean
        ): List<ApiSearchResponse> {

            val retrofit = RetrofitClient.getClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

 //           val discoveredData: MutableList<ApiSearchData> = mutableListOf()
            val response = apiService.apiGetDataByQuery(
                query = query ,
                offset = offset,
                limit = limit,
               results_id = results_id,
//                delete = false
            ).execute()

            val data = response.body()?.first()
            if (data != null) {
                discoveredData.add(data)
                if (!data.next.isNullOrEmpty()) {
                    val next= URL(data.next)
                    val requestedNext = next.query
                        .removePrefix("query=")
                        .replace('+', ' ')
                        .split('&')
                    val nextQuery = requestedNext.first()
                    if (verbose) println("Requetage complémentaire avec offset ${data.next_offset}")
                    getAllDataFragment(serverUrl, nextQuery, data.next_offset, limit, data.results_id!!, discoveredData, verbose)
                }
            }
            return discoveredData
        }

    }
}




