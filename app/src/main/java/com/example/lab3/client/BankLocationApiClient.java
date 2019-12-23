package com.example.lab3.client;

import com.example.lab3.service.BankLocationApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BankLocationApiClient {

    private static Retrofit retrofit;

    private static Retrofit getRetrofit(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static BankLocationApi getService(){
        return getRetrofit().create(BankLocationApi.class);
    }
}
