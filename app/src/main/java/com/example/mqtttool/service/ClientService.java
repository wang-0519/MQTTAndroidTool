package com.example.mqtttool.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import client.Message;
import client.TopicInformation;
import messageHandler.MessageObservable;

public class ClientService extends Service {

    private MemoryService.MemoryBinder memoryBinder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            memoryBinder = (MemoryService.MemoryBinder)iBinder;
            ArrayList<ClientInformation> clients = memoryBinder.getAllClient();
            for(ClientInformation client : clients){
                MQTTClientThread thread = new MQTTClientThread(client);
                ClientService.this.threadPool.execute(thread);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

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
        /**
         * 设置 Handler 对象
         * @param handler
         */
        public void setHandler(AbsMyHandler handler){
            ClientService.this.observer.setHandler(handler);
        }

        /**
         * 解绑时删除Handler对象
         * @param handler
         */
        public void deleteHandler(AbsMyHandler handler){
            ClientService.this.observer.deleteHandler(handler);
        }

        /**
         * 获取指定 clientID 对应的 Runnable 对象
         * @param id
         * @return
         */
        public MQTTClientThread getMQTTClientThread(String id){
            return ClientService.this.threadPool.findClientThread(id);
        }

        /**
         * 获取指定位置的 Runnable 对象
         * @param position
         * @return
         */
        public MQTTClientThread getMQTTClientThread(int position){
            return ClientService.this.threadPool.findClientThread(position);
        }

        /**
         * 新建一个客户端
         * @param clientInformation
         */
        public void newClient(ClientInformation clientInformation){
            ClientService.this.memoryBinder.insertClient(clientInformation);
            MQTTClientThread thread = new MQTTClientThread(clientInformation);
            ClientService.this.threadPool.execute(thread);
        }

        /**
         * 更新客户端信息
         * @param clientInformation
         */
        public void updateClient(ClientInformation clientInformation){
            ClientService.this.memoryBinder.updateClient(clientInformation);
            ClientService.this.threadPool.updateThread(clientInformation);
        }

        /**
         * 指定 clientID 客户端重连
         * @param id
         * @return
         */
        public boolean reConnect(String id){
            return ClientService.this.threadPool.reConnect(id);
        }

        /**
         * 结束某客户端线程 Runnable 对象保留
         * @param id
         * @return
         */
        public boolean stopClient(String id){
            return ClientService.this.threadPool.stopThread(id);
        }

        /**
         * 删除客户端，停止客户端线程，删除对应的 Runnable 对象
         * @param id
         * @return
         */
        public boolean deleteClient(String id){
            ClientService.this.memoryBinder.dropClient(getMQTTClientThread(id).getClientInformation());
            if(ClientService.this.threadPool.findClientThread(id).getClientInformation().getState() == ClientInformation.CONN_STATE.CONN){
                ClientService.this.threadPool.stopThread(id);
            }
            return ClientService.this.threadPool.deleteThread(id);
        }

        /**
         * 获取所有客户端 Runnable 对象
         * @return
         */
        public ArrayList<MQTTClientThread> getClientsThread(){
            return ClientService.this.threadPool.getThreads();
        }

        /**
         * 清除历史记录，client 与服务器交互信息
         * @param id
         */
        public void clearHistoryMessage(String id){

        }

        /**
         * 订阅主题
         * @param clientId
         * @param topicInformation
         */
        public void subscribe(String clientId, TopicInformation topicInformation){
            getMQTTClientThread(clientId).subscribe(topicInformation);
            ClientService.this.memoryBinder.addTopicInformation(clientId, topicInformation);
        }

        /**
         * 发布主题
         * @param clientId
         * @param topicInformation
         */
        public void publishTopic(String clientId, TopicInformation topicInformation){
            getMQTTClientThread(clientId).getClientInformation().addTopic(topicInformation);
            ClientService.this.memoryBinder.addTopicInformation(clientId, topicInformation);
        }

        /**
         * 删除话题
         * @param clientId
         * @param topicInformation
         */
        public void deleteTopicInformation(String clientId, TopicInformation topicInformation){
            ClientService.this.memoryBinder.deleteTopicInformation(clientId, topicInformation);
            getMQTTClientThread(clientId).getClientInformation().deleteTopic(topicInformation);
        }

        /**
         * 发布报文
         * @param clientId
         * @param topicInformation
         * @param message
         */
        public void publish(String clientId, TopicInformation topicInformation, Message message){

        }

        /**
         * 添加报文到历史记录
         * @param clientId
         * @param topicInformation
         * @param message
         */
        public void addHistory(String clientId, TopicInformation topicInformation, Message message){
            ClientService.this.memoryBinder.addHistory(clientId, topicInformation, message);
        }

        /**
         * 清除历史记录，某主题的历史记录
         * @param clientId
         * @param topicInformation
         */
        public void clearHistory(String clientId, TopicInformation topicInformation){
            ClientService.this.memoryBinder.clearHistory(clientId, topicInformation);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(ClientService.this, MemoryService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
