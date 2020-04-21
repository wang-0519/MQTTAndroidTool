package com.example.mqtttool.view;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.mqtttool.service.ClientService;

import java.io.Serializable;

import client.HelpMess;
import client.TopicInformation;

/**
 * Handler 抽象类
 */
public class AbsMyHandler extends Handler implements Serializable {

    private ClientService.MyBinder binder = null;

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        HelpMess helpMess = (HelpMess)msg.obj;
        if(helpMess.getType() == HelpMess.HELP_MESS_TYPE.RECIVE){
            binder.addHistory(helpMess.getId(),
                    binder.getMQTTClientThread(helpMess.getId()).getClientInformation().getTopic(helpMess.getTopic(), TopicInformation.TOPICTYPE.SUBSCRIBE),
                    helpMess.getMessage());
        }
    }

    public void setBinder(ClientService.MyBinder binder){
        this.binder = binder;
    }
}
