package com.vaicomp.karkun.ui.Reports;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;



    public LiveData<String> getText() {
        return mText;
    }
}