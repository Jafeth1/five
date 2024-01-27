package com.example.myapplication;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class SharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Double> forecast = new MutableLiveData<>();

    public SharedViewModel(Application application) {
        super(application);
    }

    public void setForecast(double value) {
        forecast.setValue(value);
    }

    public MutableLiveData<Double> getForecast() {
        return forecast;
    }
}
