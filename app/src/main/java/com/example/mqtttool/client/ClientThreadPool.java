package com.example.mqtttool.client;


import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import client.ClientInformation;

public class ClientThreadPool extends ThreadPoolExecutor {

    private ArrayList<MQTTClientThread> threads = null;

    public ClientThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        threads = new ArrayList<>();
    }


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

    public MQTTClientThread findClientThread(int position){
        if(position >= threads.size()){
            return null;
        }
        return threads.get(position);
    }

    public boolean reConnect(String id){
        MQTTClientThread thread = this.findClientThread(id);
        execute(thread);
        return true;
    }

    public boolean stopThread(String id){
        MQTTClientThread thread = this.findClientThread(id);
        thread.closeThread();
        return true;
    }

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
