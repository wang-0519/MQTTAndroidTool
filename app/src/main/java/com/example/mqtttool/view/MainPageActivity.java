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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.client.MQTTClientThread;
import com.example.mqtttool.service.ClientService;

import client.ClientInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPageActivity extends AppCompatActivity {

    private Button createClient = null;
    private ListView createdClients = null;

    private List<Map<String, Object>> mapList = null;

    private ClientInformation ci = null;

    private ClientService.MyBinder iBinder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBinder = (ClientService.MyBinder)service;
            iBinder.setHandler(handler);
            flushView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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

        createdClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainPageActivity.this, ClientActivity.class);
                Bundle bundle = new Bundle();
                ci = iBinder.getMQTTClientThread(position).getClientInformation();
                bundle.putSerializable("client",ci);
                intent.putExtras(bundle);
                startActivity(intent);
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

    private void flushView(){
        mapList.clear();
        ArrayList<MQTTClientThread> threads = iBinder.getClientsThread();
        for(MQTTClientThread thread : threads){
            ClientInformation ci = thread.getClientInformation();
            Map<String, Object> map = new HashMap<>();
            map.put("name", ci.getName());
            map.put("addr", ci.getAddr());
            mapList.add(map);
        }
        SimpleAdapter sa = new SimpleAdapter(this, mapList, R.layout.clients_adapter_layout,
                new String[]{"name", "addr"}, new int[]{R.id.v_client_name, R.id.v_client_addr});
        createdClients.setAdapter(sa);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(iBinder != null){
            flushView();
        }
    }

    @Override
    protected void onDestroy() {
        iBinder.deleteHandler(handler);
        unbindService(sc);
        super.onDestroy();
    }

    public class MainPageHandler extends AbsMyHandler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }
}
