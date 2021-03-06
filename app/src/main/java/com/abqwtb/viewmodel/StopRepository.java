package com.abqwtb.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;
import com.abqwtb.service.AtMyStopService;

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

    public StopRepository() {
        stopsLiveData = new MutableLiveData<>();
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
        atMyStopService.getStopTimes(agency, stopId)
                .enqueue(new Callback<List<RealtimeTripInfo>>() {
                    @Override
                    public void onResponse(Call<List<RealtimeTripInfo>> call, Response<List<RealtimeTripInfo>> response) {
                        if (response.body() != null){
                            realTimeLiveData.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RealtimeTripInfo>> call, Throwable t) {

                    }
                });
    }

    public MutableLiveData<List<RealtimeTripInfo>> getRealTimeLiveData() {
        return realTimeLiveData;
    }

    public void clearRealTimeData() {
        realTimeLiveData.setValue(null);
    }
}
