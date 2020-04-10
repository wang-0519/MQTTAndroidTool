package com.example.mqtttool.client;


import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import client.ClientInformation;

public class ClientThreadPool extends ThreadPoolExecutor {

    //Runnable对象队列
    private ArrayList<MQTTClientThread> threads = null;

    public ClientThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        threads = new ArrayList<>();
    }

    /**
     * 启动客户端线程，如果客户端Runnable对象在队列中直接启动，否则将Runnable对象加入到队列中
     * @param command
     */
    @Override
    public void execute(Runnable command) {
        if(this.findClientThread(((MQTTClientThread)command).getClientInformation().getId()) == null){
            threads.add((MQTTClientThread)command);
        }
        super.execute(command);
    }

    /**
     * 查找某id对应的客户端信息
     * @param id
     * @return
     */
    public MQTTClientThread findClientThread(String id){
        int i = 0;
        while(i < threads.size() && ! id.equals(threads.get(i).getClientInformation().getId())){
            i++;
        }
        if(i >= threads.size()){
            return null;
        }
        return threads.get(i);
    }

    /**
     * 通过位置信息获取客户端线程
     * @param position
     * @return
     */
    public MQTTClientThread findClientThread(int position){
        if(position >= threads.size()){
            return null;
        }
        return threads.get(position);
    }

    /**
     * 客户端重新连接
     * @param id
     * @return
     */
    public boolean reConnect(String id){
        MQTTClientThread thread = this.findClientThread(id);
        execute(thread);
        return true;
    }

    /**
     * 断开客户端连接，结束客户端线程，Runnable对象依旧保存在ArrayList中
     * @param id
     * @return
     */
    public boolean stopThread(String id){
        MQTTClientThread thread = this.findClientThread(id);
        thread.closeThread();
        return true;
    }

    /**
     * 删除客户端线程，删除Runnable对象
     * @param id
     * @return
     */
    public boolean deleteThread(String id){
        threads.remove(this.findClientThread(id));
        return true;
    }

    public boolean updateThread(ClientInformation clientInformation){
        MQTTClientThread thread = this.findClientThread(clientInformation.getId());
        if(thread.sign == MQTTClientThread.RUNTYPE.CONNECT){
            thread.closeThread();
        }
        thread.setClientInformation(clientInformation);
        this.execute(thread);
        return true;
    }

    public ArrayList<MQTTClientThread> getThreads() {
        return threads;
    }
}
