package com.abqwtb.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;
import com.abqwtb.service.AtMyStopService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StopRepository {

    private AtMyStopService atMyStopService;
    private final MutableLiveData<List<BusStop>> stopsLiveData;
    private final MutableLiveData<List<RealtimeTripInfo>> realTimeLiveData;
    private final MutableLiveData<List<BusStop>> searchStopsLiveData;

    public StopRepository() {
        stopsLiveData = new MutableLiveData<>();
        searchStopsLiveData = new MutableLiveData<>();
        realTimeLiveData = new MutableLiveData<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.5:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        atMyStopService = retrofit.create(AtMyStopService.class);
    }

    public void searchByLocation(double lat, double lon) {
        atMyStopService.getStops(lat, lon)
                .enqueue(new Callback<List<BusStop>>() {
                    @Override
                    public void onResponse(Call<List<BusStop>> call, Response<List<BusStop>> response) {
                        if (response.body() != null){
                            stopsLiveData.postValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BusStop>> call, Throwable t) {
                        stopsLiveData.postValue(null);
                    }
                });
    }

    public MutableLiveData<List<BusStop>> getStopsLiveData() {
        return stopsLiveData;
    }

    public void updateRealTimeData(String agency, String stopId) {
        getStopTimes(agency, stopId)
                .enqueue(new Callback<List<RealtimeTripInfo>>() {
                    @Override
                    public void onResponse(Call<List<RealtimeTripInfo>> call, Response<List<RealtimeTripInfo>> response) {
                        if (response.body() != null){
                            realTimeLiveData.postValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RealtimeTripInfo>> call, Throwable t) {

                    }
                });
    }

    private Call<List<RealtimeTripInfo>> getStopTimes(String agency, String stopId) {
        return atMyStopService.getStopTimes(agency, stopId);
    }

    public List<RealtimeTripInfo> getStopTimesSync(String agency, String stopId) throws IOException {
        return getStopTimes(agency, stopId).execute().body();
    }

    public MutableLiveData<List<RealtimeTripInfo>> getRealTimeLiveData() {
        return realTimeLiveData;
    }

    public void clearRealTimeData() {
        realTimeLiveData.setValue(null);
    }

    public void searchStops(String name, String route, String id) {
        atMyStopService.getStopsSearch(name, route, id)
                .enqueue(new Callback<List<BusStop>>() {
                    @Override
                    public void onResponse(Call<List<BusStop>> call, Response<List<BusStop>> response) {
                        if (response.body() != null){
                            searchStopsLiveData.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BusStop>> call, Throwable t) {
                        searchStopsLiveData.postValue(null);
                    }
                });
    }

    public LiveData<List<BusStop>> getSearchStopsLiveData() {
        return searchStopsLiveData;
    }
}
