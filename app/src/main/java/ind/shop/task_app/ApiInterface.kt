package ind.shop.task_app

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("random")//write api end point in the get brackets
    fun getquotedata(): Call<List<quote_data_classItem>>
}