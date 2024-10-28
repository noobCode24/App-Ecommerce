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
    // cau hinh retrofit
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
                    .baseUrl(baseUrl) //Đặt URL cơ sở của server mà Retrofit sẽ gửi yêu cầu đến.
                    .addConverterFactory(GsonConverterFactory.create(gson)) // giúp Retrofit tự động chuyển đổi dữ liệu JSON từ server thành đối tượng Java
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // Cho phép Retrofit sử dụng RxJava để quản lý các yêu cầu không đồng bộ
                    .client(okHttpClient)
                    .build();
        }
        return instance;
    }
}
