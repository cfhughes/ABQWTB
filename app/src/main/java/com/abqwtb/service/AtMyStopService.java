package com.abqwtb.service;

import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AtMyStopService {

    @GET("/stops")
    Call<List<BusStop>> getStops(@Query("lat") double lat, @Query("lon") double lon);

    @GET("/stops/search")
    Call<List<BusStop>> getStopsSearch(@Query("name") String name, @Query("route") String route, @Query("id") String id);

    @GET("/agency/{agency}/stops/{stopId}")
    Call<List<RealtimeTripInfo>> getStopTimes(@Path("agency") String agency, @Path("stopId") String stopId);

}
