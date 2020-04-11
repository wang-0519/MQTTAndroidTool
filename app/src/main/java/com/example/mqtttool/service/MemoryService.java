package com.example.mqtttool.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.example.mqtttool.memory.MySQLiteHelper;
import com.example.mqtttool.memory.SQLiteHandler;

import java.io.File;
import java.io.FileOutputStream;
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
            sqLiteHandler.insertClient(clientInformation);
            File filedir = getExternalFilesDir(clientInformation.getId() + "/publish");
            if(!filedir.exists()){
                filedir.mkdirs();
            }
            filedir = getExternalFilesDir(clientInformation.getId() + "/subscribe");
            if(!filedir.exists()){
                filedir.mkdirs();
            }
        }

        /**
         * 删除客户端信息
         * @param clientInformation
         */
        public void dropClient(ClientInformation clientInformation){
            for(TopicInformation ti : clientInformation.getTopicInformation()){
                deleteTopicInformation(clientInformation.getId(), ti);
            }
            sqLiteHandler.dropClient(clientInformation.getId());
            File filedir = getExternalFilesDir(clientInformation.getId());
            if(filedir.exists()){
                deleteDirWithFiles(filedir);
            }
        }

        /**
         * 更新客户端信息
         * @param clientInformation
         */
        public void updateClient(ClientInformation clientInformation){
            sqLiteHandler.updateClient(clientInformation);
        }

        /**
         * 获取全部客户端信息
         * @return
         */
        public ArrayList<ClientInformation> getAllClient(){
            return sqLiteHandler.getAllClient();
        }

        /**
         * 添加话题信息
         * @param clientId
         * @param topicInformation
         */
        public void addTopicInformation(String clientId, TopicInformation topicInformation){
            sqLiteHandler.addTopicInformation(clientId, topicInformation);
        }

        /**
         * 删除话题信息
         * @param clientId
         * @param topicInformation
         */
        public void deleteTopicInformation(String clientId, TopicInformation topicInformation){
            sqLiteHandler.deleteTopicInformation(clientId, topicInformation);
            File filedir = getExternalFilesDir(clientId + sqLiteHandler.setTopicType(topicInformation.getTpoicType()));
            if(filedir.exists()){
                deleteDirWithFiles(filedir);
            }
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
         * 添加消息到历史记录
         * @param clientId
         * @param topicInformation
         * @param message
         */
        public void addHistory(String clientId, TopicInformation topicInformation, client.Message message){
            try{
                File filedir = getExternalFilesDir(clientId + "/" + sqLiteHandler.setTopicType(topicInformation.getTpoicType()));
                if(!filedir.exists()){
                    filedir.mkdirs();
                }
                String fileName = filedir.getPath() + File.separator + topicInformation.getTopicName() + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        /**
         * 清空历史记录
         * @param clientId
         * @param topicInformation
         */
        public void clearHistory(String clientId, TopicInformation topicInformation){
            File filedir = getExternalFilesDir(clientId + "/" + sqLiteHandler.setTopicType(topicInformation.getTpoicType()));
            File file = new File(filedir.getPath() + File.separator + topicInformation.getTopicName() + ".txt");
            if(file.isFile() && file.exists()){
                file.delete();
            }
        }

        /**
         * 删除文件及目录
         * @param file
         */
        private void deleteDirWithFiles(File file){
            if(!file.isFile()){
                for(File fi : file.listFiles()){
                    deleteDirWithFiles(fi);
                }
            }
            file.delete();
        }
    }
}
