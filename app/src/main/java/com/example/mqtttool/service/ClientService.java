package com.example.mqtttool.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.mqtttool.client.ClientThreadPool;
import com.example.mqtttool.client.MQTTClientThread;
import com.example.mqtttool.client.MessageObserver;
import com.example.mqtttool.view.AbsMyHandler;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import client.ClientInformation;
import messageHandler.MessageObservable;

public class ClientService extends Service {

    //消息观察者
    private MessageObserver observer = new MessageObserver();
    {
        MessageObservable.getInstance().addObserver(observer);
    }
    //线程池
    private ClientThreadPool threadPool = new ClientThreadPool(5,10,30,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    //MyBinder对象
    private MyBinder binder = new MyBinder();


    /**
     * Binder类
     * Activity类绑定后获取此类的对象实例，通过此对象Activity可传递信息
     */
    public class MyBinder extends Binder{
        public void setHandler(AbsMyHandler handler){
            ClientService.this.observer.setHandler(handler);
        }

        public void deleteHandler(AbsMyHandler handler){
            ClientService.this.observer.deleteHandler(handler);
        }

        public MQTTClientThread getMQTTClientThread(String id){
            return ClientService.this.threadPool.findClientThread(id);
        }

        public MQTTClientThread getMQTTClientThread(int position){
            return ClientService.this.threadPool.findClientThread(position);
        }

        public void newClient(ClientInformation clientInformation){
            MQTTClientThread thread = new MQTTClientThread(clientInformation);
            ClientService.this.threadPool.execute(thread);
        }

        public void updateClient(ClientInformation clientInformation){
            ClientService.this.threadPool.updateThread(clientInformation);
        }

        public boolean stopClient(String id){
            return ClientService.this.threadPool.stopThread(id);
        }

        public ArrayList<MQTTClientThread> getClientsThread(){
            return ClientService.this.threadPool.getThreads();
        }
    }

    @Override
    public void onCreate() {
        System.out.println("Service++++++++onCreate()");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Service++++++++++onBind()");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Service++++++++++onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("Service++++++++++++++onUnbind");
        return super.onUnbind(intent);
    }
}
