package api


import models.*
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


interface ServiceApi {

    companion object {

        fun apiGetToken(serverUrl: String, username: String, password: String, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            return apiService.authenticateUser("password", username, password).execute().body()?.token

        }

        fun apiQueryData(serverUrl: String, query: String, unsafe: Boolean, verbose: Boolean): List<ApiSearchResponse>? {
            val discoveredData: MutableList<ApiSearchResponse> = mutableListOf()
            if (verbose) println("Requetage pour  ($query)")
            getAllDataFragment(serverUrl, unsafe, query, 0, 100, "", discoveredData, verbose)
            return discoveredData
        }

//        fun apiKindsData(serverUrl: String, kind: String, unsafe: Boolean, verbose: Boolean): List<Kind> {
//            val kindsData: MutableList<Kind> = mutableListOf()
//            if (verbose) println("Requetage Kind pour  ($kind)")
//            getAllKindsFragment(serverUrl, unsafe, kind,  0, 100, "", kindsData, verbose)
//            return kindsData
//        }
//
//        fun apiNodesData(serverUrl: String, id: String, unsafe: Boolean, verbose: Boolean): Node? {
//            if (verbose) println("Requetage Noeud pour  ($id)")
//            return getNode(serverUrl, unsafe, id, verbose)
//        }
//
//        fun apiTraversedNodesData(serverUrl: String, id: String, traverse: String, unsafe: Boolean, verbose: Boolean): TraversedNode? {
//            if (verbose) println("Requetage Traverse : $traverse")
//            return getTraversedNode(serverUrl, traverse, unsafe, id, verbose)
//        }
//
//        fun apiGetSoftwareConnectedGraph(serverUrl: String, id: String, unsafe: Boolean, verbose: Boolean): Graph? {
//            if (verbose) println("Requetage Graphe pour le noeud  ($id)")
//            return getGraph(serverUrl, "software-connected", false, unsafe, id, verbose)
//        }
//
//        fun apiGetSoftwareGraph(serverUrl: String, id: String, unsafe: Boolean, verbose: Boolean): Graph? {
//            if (verbose) println("Requetage Graphe pour le noeud  ($id)")
//            return getGraph(serverUrl, "software", false, unsafe, id, verbose)
//        }
//
//        fun apiGetInfrastructureGraph(serverUrl: String, id: String, unsafe: Boolean, verbose: Boolean): Graph? {
//            if (verbose) println("Requetage Graphe pour le noeud  ($id)")
//            return getGraph(serverUrl, "infrastructure", false, unsafe, id, verbose)
//        }
//        fun apiGetNodeKindDetails(serverUrl: String, kind: String, unsafe: Boolean, verbose: Boolean): NodeKindDetails? {
//            if (verbose) println("Requetage Details pour le kind  ($kind)")
//            return getNodeKindDetails(serverUrl, unsafe, kind, verbose)
//        }



        private fun getAllDataFragment(
            serverUrl: String,
            unsafe: Boolean,
            query: String,
            offset: Int,
            limit: Int,
            results_id: String,
            discoveredData: MutableList<ApiSearchResponse>,
            verbose: Boolean
        ): List<Any> {

            val searchData = mutableListOf<ApiSearchResponse>()
            val result = mutableListOf<Any>()
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            //           val discoveredData: MutableList<ApiSearchData> = mutableListOf()
            val response = apiService.apiGetDataByQuery(
                query = query,
                offset = offset,
                limit = limit,
                results_id = results_id,
//                delete = false
            ).execute()

            val data = response.body()?.first()
            if (data != null) {
                discoveredData.add(data)
                data.results.forEach { result.add(it) }

                if (!data.next.isNullOrEmpty()) {
                    val next = URL(data.next)
                    val requestedNext = next.query
                        .removePrefix("query=")
                        .replace('+', ' ')
                        .split('&')
                    val nextQuery = requestedNext.first()
                    val nextOffset = data.next_offset
                    if (verbose) println("Requetage Node complémentaire avec offset ${data.next_offset} ${next}")
                    getAllDataFragment(
                        serverUrl,
                        unsafe,
                        nextQuery,
                        data.next_offset,
                        limit,
                        data.results_id!!,
                        discoveredData,
                        verbose
                    )
                    discoveredData.forEach {
                        it.results.forEach {
                            result.add(it)
                        }
                    }
                }
            }
            return result
        }


//        private fun getAllKindsFragment(
//            serverUrl: String,
//            unsafe: Boolean,
//            kind: String,
////            attributes_query: String?,
//            offset: Int,
//            limit: Int,
//            results_id: String,
//            kindsData: MutableList<Kind>,
//            verbose: Boolean
//        ): List<Kind> {
//
//            val searchData = mutableListOf<Kind>()
//            val retrofit =
//                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
//            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
//
//            //           val discoveredData: MutableList<ApiSearchData> = mutableListOf()
//            val response = apiDiscovery.apiGetKindsData(
//                kind = kind,
////                attributes = attributes_query,
//                offset = offset,
//                limit = limit,
//                results_id = results_id,
////                delete = false
//            ).execute()
//
//            val data = response.body()?.first()
//            if (data != null) {
//                data.results.forEach { kindsData.add(it) }
//                var nbOfKindAlreadyProcessed = 100
//                if (!data.next.isNullOrEmpty()) {
//                    val next = URL(data.next)
//                    val requestedNext = next.query
//                        .removePrefix("query=")
//                        .replace('+', ' ')
//                        .split('&')
//                    val nextQuery = requestedNext.first()
//                    val nextOffset = data.next_offset
//  //                  nbOfKindAlreadyProcessed = 0
//                    if (verbose) println("Requetage Kind complémentaire avec offset ${data.next_offset} ${next}")
//                    val nextData = getAllKindsFragment(
//                        serverUrl,
//                        unsafe,
//                        nextQuery,
//                        nextOffset,
//                        limit,
//                        data.results_id!!,
//                        searchData,
//                        verbose
//                    )
//                    nextData.forEach { kind ->
//                        kindsData.add(kind)
//                    }
//                }
//            }
//
//            return kindsData
//        }
//
//
//        private fun getNode(
//            serverUrl: String,
//            unsafe: Boolean,
//            id: String,
//            verbose: Boolean
//        ): Node? {
//
//            val retrofit =
//                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
//            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
//            val response = apiDiscovery.apiGetNodeData(
//                nodeId = id
//            ).execute()
//            return response.body()
//        }
//
//        private fun getTraversedNode(
//            serverUrl: String,
//            traverse: String,
//            unsafe: Boolean,
//            id: String,
//            verbose: Boolean
//        ): TraversedNode? {
//
//            val retrofit =
//                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
//            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
//            val response = apiDiscovery.apiGetTraversedNodeData(
//                nodeId = id,
//                traverse = traverse
//            ).execute()
//            return response.body()
//        }
//
//        private fun getGraph(
//            serverUrl: String,
//            focus: String,
//            complete: Boolean,
//            unsafe: Boolean,
//            id: String,
//            verbose: Boolean): Graph? {
//            val retrofit =
//                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
//            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
//            val response = apiDiscovery.apiGetGraph(
//                nodeId = id , focus = focus, complete = complete
//            ).execute()
//            return response.body()
//        }
//
//        private fun getNodeKindDetails(
//            serverUrl: String,
//            unsafe: Boolean,
//            kind: String,
//            verbose: Boolean): NodeKindDetails? {
//            val retrofit =
//                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
//            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
//            val response = apiDiscovery.apiGetNodeKindDetails(
//                kind = kind
//            ).execute()
//            println(response.body())
//            return response.body()
//        }
//

    }
}




