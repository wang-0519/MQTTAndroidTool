package com.example.mqtttool.memory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists t_client_information(" +
                "m_client_id varchar primary key not null," +
                "m_client_name varchar not null," +
                "m_client_addr varchar not null," +
                "m_user_name varchar not null," +
                "m_password varchar," +
                "m_reconn_period integer not null," +
                "m_conn_time_out integer not null," +
                "m_keep_alive integer not null," +
                "m_will_topic varchar," +
                "m_will_qos integer," +
                "m_will_message varchar," +
                "m_reschedule_ping integer," +
                "m_clean_session integer," +
                "m_auto_conn integer," +
                "m_mqtt_version integer," +
                "m_queue_qos integer," +
                "m_will_retain integer)");
        db.execSQL("create table if not exists t_topic_information(" +
                "m_topic_id int primary key AUTOINCREMENT," +
                "m_client_id varchar not null," +
                "m_topic_name varchar not null," +
                "m_topic_type varchar not null," +
                "m_topic_qos integer not null," +
                "m_message_file varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
