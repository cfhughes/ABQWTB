package com.abqwtb.service;

import com.abqwtb.model.BusStop;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AtMyStopService {

    @GET("/stops")
    Call<List<BusStop>> getStops(@Query("lat") double lat, @Query("lon") double lon);

}
