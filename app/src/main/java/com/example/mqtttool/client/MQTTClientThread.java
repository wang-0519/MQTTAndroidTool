package com.example.mqtttool.client;

import java.util.ArrayList;

import client.ClientInformation;
import client.MQTTClient;
import client.TopicInformation;

/**
 * 客户端线程
 */
public class MQTTClientThread implements Runnable{

    //MQTT客户端
    private MQTTClient client = null;

    //需要线程进行的操作
    public enum RUNTYPE{CONNECT, CLOSE}
    public RUNTYPE sign = RUNTYPE.CONNECT;

    //客户端信息
    private ClientInformation clientInformation = null;

    //锁变量
    private byte[] by = new byte[0];

    public MQTTClientThread(ClientInformation clientInformation){
        this.clientInformation = clientInformation;
    }

    @Override
    public void run() {
        client = new MQTTClient(clientInformation);
        while(sign != RUNTYPE.CLOSE){
            synchronized (by){
                try{
                    by.wait();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    //关闭客户端线程
    public void closeThread(){
        client.disConnect();
        client.close();
        this.sign = RUNTYPE.CLOSE;
        synchronized (by){
            by.notify();
        }
    }

    /**
     * 传入、获取客户端信息
     * @return
     */
    public ClientInformation getClientInformation() {
        return this.clientInformation;
    }
    public void setClientInformation(ClientInformation clientInformation){
        this.clientInformation = clientInformation;
        client.setClient(clientInformation);
        if(! clientInformation.getAddr().equals(this.clientInformation.getAddr())){
            closeThread();
        }
    }

    /**
     * 连接服务器
     */
    public void connect(){
        client.connect();
    }

    /**
     * 发布报文
     */
    public void publish(int qos, boolean retain, String topic, String mess){
        client.publish(qos,retain,topic,mess);
    }


    /**
     * 订阅主题
     */
    public void subscribe(ArrayList<TopicInformation> topic){
        client.subscribe(topic);
    }
    public void subscribe(TopicInformation topic){
        ArrayList<TopicInformation> topics = new ArrayList<>();
        topics.add(topic);
        client.subscribe(topics);
    }


    /**
     * 取消订阅
     */
    public void unSubscribe(ArrayList<TopicInformation> topic){
        client.unSubscribe(topic);
    }
    public void unsubscribe(TopicInformation topic){
        ArrayList<TopicInformation> topics = new ArrayList<>();
        topics.add(topic);
        client.unSubscribe(topics);
    }

}
