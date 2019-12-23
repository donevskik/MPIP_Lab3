package com.example.lab3.service;

import com.example.lab3.models.Results;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BankLocationApi {

    @GET("/maps/api/place/nearbysearch/json?key=AIzaSyB0TrNtcg8DL_GrIyNAkwHUqPtEZlEO53s&radius=5000&types=bank")
    Call<Results> getBankLocations(@Query("location") String location);
}
