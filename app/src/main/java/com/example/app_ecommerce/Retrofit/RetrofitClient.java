package com.example.app_ecommerce.Retrofit;

import android.util.Log;

import com.example.app_ecommerce.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;
    public static Retrofit getInstance(String baseUrl) {
        if (instance == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)  // Thời gian chờ kết nối
                    .readTimeout(30, TimeUnit.SECONDS)     // Thời gian chờ đọc dữ liệu từ server
                    .writeTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ ghi dữ liệu
                    .build();;

            Gson gson = new GsonBuilder()
                    .setLenient() // Cho phép JSON bị lỗi
                    .create();
            instance = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return instance;
    }
}
