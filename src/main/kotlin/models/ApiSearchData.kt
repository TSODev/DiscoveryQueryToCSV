package models

import com.google.gson.annotations.SerializedName

data class ApiSearchData(
    @SerializedName("count") var count: Int,
    @SerializedName("headings") var headings: List<String>,
    @SerializedName("kind") var kind: String?,
    @SerializedName("next") var next: String?,
    @SerializedName("next_offset") var next_offset: Int,
    @SerializedName("offset") var offset: Int,
    @SerializedName("results") var results: ArrayList<ArrayList<Any>>,
    @SerializedName("results_id") var results_id: String?,

//    @SerializedName("count"       ) var count      : Int?                         ,
//    @SerializedName("headings"    ) var headings   : ArrayList<String>           ,
//    @SerializedName("kind"        ) var kind       : String?                     ,
//    @SerializedName("next"        ) var next       : String?                     ,
//    @SerializedName("next_offset" ) var nextOffset : Int?                        ,
//    @SerializedName("offset"      ) var offset     : Int?                        ,
//    @SerializedName("results"     ) var results    : ArrayList<ArrayList<String>> ,
//    @SerializedName("results_id"  ) var resultsId  : String?
) {
    constructor() : this(0, arrayListOf(),null,"",0,0, arrayListOf(arrayListOf()),"id")
}