package com.example.mqtttool.client;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Observable;

import messageHandler.AbstractMessageObserver;

public class MessageObserver extends AbstractMessageObserver {

    private ArrayList<Handler> handlers = null;

    public MessageObserver(){
        handlers = new ArrayList<>();
    }

    public void setHandler(Handler handler) {
        this.handlers.add(handler);
    }

    public void deleteHandler(Handler handler){
        this.handlers.remove(handler);
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);
        Message mess = new Message();
        mess.what = 1;
        mess.obj = arg;
        handlers.get(0).sendMessage(mess);
//        for(Handler handler : handlers){
//            handler.sendMessage(mess);
//        }
    }
}
