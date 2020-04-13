package com.example.mqtttool.client;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Observable;

import client.HelpMess;
import messageHandler.AbstractMessageObserver;

/**
 * 消息观察者，通过此对象处理接收到的消息向主线程发送消息
 */
public class MessageObserver extends AbstractMessageObserver {

    //各Activity对应的Handler对象
    private ArrayList<Handler> handlers = null;
    private Handler handler = null;

    public MessageObserver(){
        handlers = new ArrayList<>();
    }

    /**
     * 替换Handler对象
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
        this.handlers.add(handler);
    }


    /**
     * 删除Handler对象
     * @param handler
     */
    public void deleteHandler(Handler handler){
        this.handlers.remove(handler);
        this.handler = this.handlers.get(handlers.size() - 1);
    }

    /**
     * 有消息需要发送时回调该方法
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);

        HelpMess helpMess = (HelpMess) arg;


//        handlers.get(0).sendMessage(mess);


        Message mess = new Message();
        mess.obj = arg;
        this.handler.sendMessage(mess);
    }
}
