package com.example.mqtttool.memory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import client.ClientInformation;
import client.TopicInformation;

public class SQLiteHandler {

    private MySQLiteHelper helper = null;
    private SQLiteDatabase setter = null;
    private SQLiteDatabase getter = null;

    public SQLiteHandler(MySQLiteHelper helper){
        this.helper = helper;
        setter = helper.getWritableDatabase();
        getter = helper.getReadableDatabase();
    }

    /**
     * 添加客户端信息到数据库
     * @param clientInformation
     */
    public void insertClient(ClientInformation clientInformation){
        try{
            setter.execSQL("insert into t_client_information valuse('"
                    + clientInformation.getId() + "','"
                    + clientInformation.getName() + "','"
                    + clientInformation.getAddr() + "','"
                    + clientInformation.getUserName() + "','"
                    + clientInformation.getPassword() + "','"
                    + clientInformation.getReconnPeriod() + ","
                    + clientInformation.getConnTimeout() + ","
                    + clientInformation.getKeepAlive() + ",'"
                    + clientInformation.getWillTopic() + "',"
                    + clientInformation.getWillQos() + ",'"
                    + clientInformation.getWillMessage() + "',"
                    + boolToInteger(clientInformation.isReschedulePing()) +","
                    + boolToInteger(clientInformation.isCleanSession()) + ","
                    + boolToInteger(clientInformation.isAutoConn()) + ","
                    + boolToInteger(clientInformation.isMqttVersion()) + ","
                    + boolToInteger(clientInformation.isQueueQos0()) + ","
                    + boolToInteger(clientInformation.isWillRetain())
                    +")");
        }catch (Exception e){
            String error = "保存失败";
            System.out.println(error);
            e.printStackTrace();
        }
    }

    //boolean 转 integer
    public int boolToInteger(boolean bool){
        return bool ? 1 : 0;
    }

    //integer 转 boolean
    public boolean integerToBoolean(int i){
        return i == 0;
    }

    /**
     * 获取所有已创建客户端信息
     * @return
     */
    public ArrayList<ClientInformation> getAllClient(){
        ArrayList<ClientInformation> clients = new ArrayList<>();
        try{
            Cursor cursor = getter.rawQuery("select * from t_client_information", null);
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                ClientInformation ci = new ClientInformation();
                ci.setId(cursor.getString(0));
                ci.setName(cursor.getString(1));
                ci.setAddr(cursor.getString(2));
                ci.setUserName(cursor.getString(3));
                ci.setPassword(cursor.getString(4));
                ci.setReconnPeriod(cursor.getInt(5));
                ci.setConnTimeout(cursor.getInt(6));
                ci.setKeepAlive(cursor.getInt(7));
                ci.setWillTopic(cursor.getString(8));
                ci.setWillQos(cursor.getInt(9));
                ci.setWillMessage(cursor.getString(10));
                ci.setReschedulePing(integerToBoolean(cursor.getInt(11)));
                ci.setCleanSession(integerToBoolean(cursor.getInt(12)));
                ci.setAutoConn(integerToBoolean(cursor.getInt(13)));
                ci.setMqttVersion(integerToBoolean(cursor.getInt(14)));
                ci.setQueueQos0(integerToBoolean(cursor.getInt(15)));
                ci.setWillRetain(integerToBoolean(cursor.getInt(16)));
                Cursor cu = getter.rawQuery("select * from t_topic_information where m_client_id = '" + ci.getId() + "'", null);
                for(cu.moveToFirst(); !cu.isAfterLast(); cu.moveToNext()){
                    TopicInformation ti = new TopicInformation();
                    ti.setTopicName(cu.getString(2));
                    ti.setTpoicType(getTopicType(cu.getString(3)));
                    ti.setQos(cu.getInt(4));
                    //ti.setFilePath(cu.getString(5));
                    ci.addTopic(ti);
                }
                clients.add(ci);
            }
            cursor.close();
        }catch (Exception e){
            System.out.println("获取信息失败");
            e.printStackTrace();
        }
        return clients;
    }

    /**
     * 更新客户端信息
     * @param clientInformation
     */
    public void updateClient(ClientInformation clientInformation){
        try{
            String sql = "update t_client_information set" +
                    " m_client_name = '" + clientInformation.getName()
                    + "',m_client_addr = '" + clientInformation.getAddr()
                    + "',m_user_name = '" + clientInformation.getUserName()
                    + "',m_reconn_period = " + clientInformation.getReconnPeriod()
                    + ", m_conn_time_out = " + clientInformation.getConnTimeout()
                    + ", m_keep_alive = " + clientInformation.getKeepAlive()
                    + ", m_reschedule_ping = " + boolToInteger(clientInformation.isReschedulePing())
                    + ", m_clean_session = " + boolToInteger(clientInformation.isCleanSession())
                    + ", m_auto_conn = " + boolToInteger(clientInformation.isAutoConn())
                    + ", m_mqtt_version = " + boolToInteger(clientInformation.isMqttVersion())
                    + ", m_queue_qos = " + boolToInteger(clientInformation.isQueueQos0())
                    + ", m_will_retain = " + boolToInteger(clientInformation.isWillRetain());
            if(clientInformation.getPassword() != null){
                sql = sql + ", m_password = '" + clientInformation.getPassword() + "'";
            }
            if(clientInformation.getWillTopic() != null){
                sql = sql + ", m_will_topic = '" + clientInformation.getWillTopic() + "'";
                sql = sql + ", m_will_qos = " + clientInformation.getWillQos();
            }
            if(clientInformation.getWillMessage() != null){
                sql = sql + ", m_will_message = '" + clientInformation.getWillMessage() + "'";
            }
            sql = sql + "where m_client_id = '" + clientInformation.getId() + "'";
            setter.execSQL(sql);
        }catch (Exception e){
            String error = "更新失败";
            System.out.println(error);
            e.printStackTrace();
        }
    }

    /**
     * 删除客户端信息
     * @param clientId
     */
    public void dropClient(String clientId){
        getter.execSQL("delete from t_client_information where m_client_id = '" + clientId + "'");
        getter.execSQL("delete from t_topic_information where m_client_id = '" + clientId + "'");
    }

    /**
     * 添加话题信息
     * @param clientId
     * @param topicInformation
     */
    public void addTopicInformation(String clientId, TopicInformation topicInformation){
        setter.execSQL("insert into t_topic_information values(null,'"
                + clientId + "','"
                + topicInformation.getTopicName() + "','"
                + setTopicType(topicInformation.getTpoicType()) + "',"
                + topicInformation.getQos() + ",'"
                + clientId + "/" + setTopicType(topicInformation.getTpoicType()) + "/" + topicInformation.getTopicName() + ".txt')");
    }
    //topicType 转 String
    public String setTopicType(TopicInformation.TOPICTYPE type){
        switch (type){
            case PUBLISH:
                return "publish";
            case SUBSCRIBE:
                return "subscribe";
        }
        return null;
    }
    //String 转 TopicType
    public TopicInformation.TOPICTYPE getTopicType(String str){
        if(str.equals("publish")){
            return TopicInformation.TOPICTYPE.PUBLISH;
        }
        return TopicInformation.TOPICTYPE.SUBSCRIBE;
    }

    /**
     * 删除话题信息
     * @param clientId
     * @param topicInformation
     */
    public void deleteTopicInformation(String clientId, TopicInformation topicInformation){
        setter.execSQL("delete from t_topic_information where m_client_id = '" + clientId
                + "' and m_topic_name = '" + topicInformation.getTopicName()
                + "' and m_topic_type = '" + setTopicType(topicInformation.getTpoicType()) + "'");
    }


    public void close(){
        setter.close();
        getter.close();
        helper.close();
    }
}
