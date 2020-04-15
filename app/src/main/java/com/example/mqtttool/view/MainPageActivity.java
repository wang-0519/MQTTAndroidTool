package com.example.mqtttool.view;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.client.MQTTClientThread;
import com.example.mqtttool.service.ClientService;
import com.example.mqtttool.service.MemoryService;

import client.ClientInformation;
import client.HelpMess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主界面
 */
public class MainPageActivity extends AppCompatActivity {

    //界面组件
    private Button createClient = null;
    private ListView createdClients = null;

    //填充信息
    private List<Map<String, Object>> mapList = null;
    ArrayList<MQTTClientThread> threads = null;

    //客户端信息
    private ClientInformation ci = null;

    //ClientService 绑定
    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
            binder.setHandler(handler);
            handler.setBinder(binder);
            flushView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    //Handler对象
    private AbsMyHandler handler = new MainPageHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mqtt_clients_activity);

        createdClients = findViewById(R.id.v_created_clients);
        createClient = findViewById(R.id.v_create_new_client);

        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);

        ci = new ClientInformation();
        mapList = new ArrayList<Map<String, Object>>();

        addListener();
    }

    /**
     * 添加监听器
     */
    private void addListener(){
        createdClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(view.getId());
                if(view.getId() == R.id.v_client_is_connect){
                    binder.reConnect(threads.get(position).getClientInformation().getId());
                    flushView();
                } else{
                    Intent intent = new Intent(MainPageActivity.this, ClientActivity.class);
                    Bundle bundle = new Bundle();
                    ci = binder.getMQTTClientThread(position).getClientInformation();
                    bundle.putSerializable("client",ci);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        createClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, CreateClientActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("className", "MainPageActivity");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * 刷新界面
     */
    private void flushView(){
        mapList.clear();
        binder.flushAllClientOfNew();
        threads = binder.getClientsThread();
        for(MQTTClientThread thread : threads){
            ClientInformation ci = thread.getClientInformation();
            Map<String, Object> map = new HashMap<>();
            map.put("name", ci.getName());
            map.put("addr", ci.getAddr());
            if(ci.getState() == ClientInformation.CONN_STATE.CONN){
                map.put("isConn",R.drawable.conn_successed);
            } else {
                map.put("isConn", R.drawable.conn_error);
            }
            if(ci.hasNew()){
                map.put("hasNew",R.drawable.new_message);
            } else {
                map.put("hasNew", null);
            }
            mapList.add(map);
        }
        SimpleAdapter sa = new SimpleAdapter(this, mapList, R.layout.clients_adapter_layout,
                new String[]{"name", "addr", "isConn", "hasNew"}, new int[]{R.id.v_client_name, R.id.v_client_addr, R.id.v_client_is_connect, R.id.v_client_has_new});
        createdClients.setAdapter(sa);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(binder != null){
            binder.setHandler(handler);
            flushView();
        }
    }

    @Override
    protected void onDestroy() {
        binder.deleteHandler(handler);
        unbindService(sc);
        super.onDestroy();
    }

    public class MainPageHandler extends AbsMyHandler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            HelpMess helpMess = (HelpMess)msg.obj;
            if(helpMess.isError()){
                Toast.makeText(MainPageActivity.this, helpMess.getErrorMessage(), Toast.LENGTH_LONG).show();
                flushView();
            } else{
                flushView();
            }
            super.handleMessage(msg);
        }
    }
}
