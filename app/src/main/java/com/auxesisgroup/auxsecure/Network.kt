package com.auxesisgroup.auxsecure

import android.util.Log.d
import android.util.Log.e
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

object WebService {

    private fun getService(): ApiService {
        return setupRetrofit().create(ApiService::class.java)
    }

    private fun setupLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = Level.BODY
        return logging
    }


    private fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.addNetworkInterceptor(setupLogging())
        client.addNetworkInterceptor { chain ->
            val request = chain.request()
            val newRequest = request.newBuilder()
                    .build()
            chain.proceed(newRequest)
        }
        return client.build()
    }

    private fun setupRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.ZORAPI)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .client(provideOkHttpClient())
                .build()
    }

    private fun <T> apiRequest(epCall: Call<T>): Observable<T> {
        return Observable.create { subscriber ->
            val response = epCall.execute()
            /*Log.d("RESPONSE CODE ", response.code().toString())
            Log.d("REQUEST URL ", epCall.request().url().toString())*/
            when {
                response.isSuccessful -> {
                    subscriber.onNext(response.body()!!)
                    subscriber.onComplete()
                }
                else -> subscriber.onError(Throwable(response.toString()))
            }
        }
    }

    // API CALLS
    fun getItem(clientId: String, itemCode: String) : Observable<Item> {
        return apiRequest(getService().fetchItemInfo(clientId, itemCode))
                .subscribeOn(Schedulers.io())
    }

    fun getItemForUpdate(clientId: String, itemCode: String, cb: ApiCallback) {
        apiRequest(getService().fetchItemInfo(clientId, itemCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { res ->
                            cb.onResponse(res)
                            /*d("Response", res.toJSONLike())*/
                        },
                        { err ->
                            cb.onError(err)
                            e("Error", err.toJSONLike())
                        }
                )
    }

    fun updateItem(clientId: String, itemCode: String, itemUpdate: ItemUpdate, cb: ApiCallback) {
        apiRequest(getService().pushItemInfo(clientId, itemCode, itemUpdate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { res ->
                            cb.onResponse(res)
                            d("Response", res.toJSONLike())
                        },
                        { err ->
                            cb.onError(err)
                            e("Error", err.toJSONLike())
                        }
                )
    }

    fun addItemFirst(clientId: String, itemCode: String, itemUpdate: ItemUpdate) : Observable<Item> {
        return apiRequest(getService().pushItemInfo(clientId, itemCode, itemUpdate))
    }
}

interface ApiService {
    @GET("/item/{clientId}/{itemCode}")
    fun fetchItemInfo(@Path("clientId") cId: String, @Path("itemCode") iCode: String) : Call<Item>

    @PUT("/item/{clientId}/{itemCode}")
    @Headers(BuildConfig.ZORHEADER)
    fun pushItemInfo(@Path("clientId") cId: String, @Path("itemCode") iCode: String, @Body itemUpdate: ItemUpdate) : Call<Item>
}