package com.example.mqtttool.view;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AbsMyHandler extends Handler implements Serializable {

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
    }
}
