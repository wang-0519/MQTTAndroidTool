package com.example.mqtttool.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.example.mqtttool.memory.MySQLiteHelper;
import com.example.mqtttool.memory.SQLiteHandler;

import java.util.ArrayList;

import client.ClientInformation;
import client.TopicInformation;

public class MemoryService extends Service {

    private MemoryBinder binder = new MemoryBinder();

    private SQLiteHandler sqLiteHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sqLiteHandler = new SQLiteHandler(new MySQLiteHelper(MemoryService.this, "mqtt_tool.db", null, 1));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MemoryBinder extends Binder {
        /**
         * 添加客户端信息
         * @param clientInformation
         */
        public void insertClient(ClientInformation clientInformation){

        }

        /**
         * 删除客户端信息
         * @param clientInformation
         */
        public void dropClient(ClientInformation clientInformation){

        }

        /**
         * 更新客户端信息
         * @param clientInformation
         */
        public void updateClient(ClientInformation clientInformation){

        }

        /**
         * 获取全部客户端信息
         * @return
         */
        public ArrayList<ClientInformation> getAllClient(){
            return null;
        }

        /**
         * 添加话题信息
         * @param clientId
         * @param topicInformation
         */
        public void addTopicInformation(String clientId, TopicInformation topicInformation){

        }

        /**
         * 删除话题信息
         * @param clientId
         * @param topicInformation
         */
        public void deleteTopicInformation(String clientId, TopicInformation topicInformation){

        }

        /**
         * 获取历史记录
         * @param clientId
         * @param topicInformation
         * @return
         */
        public ArrayList<Message> getHistory(String clientId, TopicInformation topicInformation){
            return null;
        }

        /**
         * 清空历史记录
         * @param clientId
         * @param topicInformation
         */
        public void clearHistory(String clientId, TopicInformation topicInformation){

        }
    }
}
