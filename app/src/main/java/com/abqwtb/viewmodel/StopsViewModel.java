package com.abqwtb.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;

import java.util.List;

public class StopsViewModel extends ViewModel {

    private StopRepository stopRepository;
    private LiveData<List<BusStop>> stopsLiveData;
    private LiveData<List<RealtimeTripInfo>> realTimeLiveData;
    private MutableLiveData<BusStop> selectedStopLiveData = new MutableLiveData<>();

    public StopsViewModel() {
        stopRepository = new StopRepository();
        stopsLiveData = stopRepository.getStopsLiveData();
        realTimeLiveData = stopRepository.getRealTimeLiveData();
    }

    public LiveData<List<BusStop>> getStops() {
        return stopsLiveData;
    }

    public LiveData<List<RealtimeTripInfo>> getRealTimeLiveData() {
        return realTimeLiveData;
    }

    public LiveData<BusStop> getSelectedStop() {
        return selectedStopLiveData;
    }

    public void setLocation(double lat, double lon) {
        stopRepository.searchByLocation(lat, lon);
    }

    public void updateRealTimeData(String agency, String stopId) {
        stopRepository.updateRealTimeData(agency, stopId);
    }

    public void setSelectedStop(BusStop stop) {
        selectedStopLiveData.setValue(stop);
    }

    public void clearRealTimeData() {
        stopRepository.clearRealTimeData();
    }
}
