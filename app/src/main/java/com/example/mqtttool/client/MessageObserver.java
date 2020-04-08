package com.example.mqtttool.client;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Observable;

import client.HelpMess;
import messageHandler.AbstractMessageObserver;

public class MessageObserver extends AbstractMessageObserver {

    private ArrayList<Handler> handlers = null;

    private Handler handler = null;

    public MessageObserver(){
        handlers = new ArrayList<>();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
        System.out.println("++++++++" + handler.getClass().toString());
        this.handlers.add(handler);
    }

    public void deleteHandler(Handler handler){
        this.handlers.remove(handler);
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);

        HelpMess helpMess = (HelpMess)arg;
        if(helpMess.isError()){
            System.out.println(helpMess.getErrorMessage());
        }else{
            System.out.println(helpMess.getMessage().getMessage());
        }

//        handlers.get(0).sendMessage(mess);
        if(handler == null){
            System.out.println("++++++++Observer");
        } else {
            Message mess = new Message();
            mess.obj = arg;
            this.handler.sendMessage(mess);
            System.out.println("++++++++Observer+++" + handler.getClass().toString());
        }
    }
}
