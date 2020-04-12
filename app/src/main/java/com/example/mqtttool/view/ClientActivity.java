package com.example.mqtttool.view;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.service.ClientService;

import client.ClientInformation;
import client.HelpMess;
import client.TopicInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端 Activity
 */
public class ClientActivity extends AppCompatActivity {

    /**
     * 各组件
     */
    private ListView topics = null;
    private ActionBar actionBar = null;

    /**
     * 客户端信息
     */
    private ClientInformation ci = null;
    private List<Map<String,Object>> mapList = null;

    //Handler对象
    private AbsMyHandler handler = new ClientActivityHandler();

    //ClientService 连接所需变量
    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
            binder.setHandler(handler);
            flushView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_information);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        topics = findViewById(R.id.v_topics_view);
        addListener();

        mapList = new ArrayList<>();
        ci = (ClientInformation)getIntent().getSerializableExtra("client");
        setTitle("客户端：" + ci.getName());

        Intent service = new Intent(this, ClientService.class);
        bindService(service, sc, Service.BIND_AUTO_CREATE);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 导航栏按钮接听事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        Bundle bundle = null;
        switch (item.getItemId()){
            case R.id.v_m_publish:
                if(ci.getState() != ClientInformation.CONN_STATE.CONN){
                   Toast.makeText(ClientActivity.this, "未连接服务器", Toast.LENGTH_LONG).show();
                    return false;
                }
                publishTopic();
                flushView();
                return true;
            case R.id.v_m_subscribe:
                if(ci.getState() != ClientInformation.CONN_STATE.CONN){
                    Toast.makeText(ClientActivity.this, "未连接服务器", Toast.LENGTH_LONG).show();
                    return false;
                }
                subscribeTopic();
                flushView();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.v_m_client_setting:
                intent = new Intent(ClientActivity.this, CreateClientActivity.class);
                bundle = new Bundle();
                bundle.putSerializable("client", ci);
                bundle.putString("className", "ClientActivity");
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.v_m_history_ubytes:
                intent = new Intent(ClientActivity.this, HistoryMessage.class);
                bundle = new Bundle();
                bundle.putSerializable("client", ci);
                bundle.putString("className", "ClientActivity");
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.v_m_connect_service:
                if(ci.getState() != ClientInformation.CONN_STATE.CONN){
                    binder.reConnect(ci.getId());
                } else {
                    Toast.makeText(ClientActivity.this, "客户端已连接！", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.v_m_disconnect_service:
                if(ci.getState() == ClientInformation.CONN_STATE.CONN){
                    binder.stopClient(ci.getId());
                } else {
                    Toast.makeText(ClientActivity.this, "客户端未连接！", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.v_m_delete_client:
                new AlertDialog.Builder(ClientActivity.this)
                        .setTitle("确认删除？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binder.deleteClient(ci.getId());
                                Intent in = null;
                                in = new Intent(ClientActivity.this, MainPageActivity.class);
                                startActivity(in);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create()
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 订阅话题的动作
     */
    private void subscribeTopic(){
        final LinearLayout table = (LinearLayout) getLayoutInflater().inflate(R.layout.subscribe_topic_table, null);
        table.findViewById(R.id.v_t_subscribe_topic_qos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ClientActivity.this, v);
                getMenuInflater().inflate(R.menu.qos_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.Qos0:
                                ((EditText)table.findViewById(R.id.v_t_subscribe_topic_qos)).setText("Qos0");
                                break;
                            case R.id.Qos1:
                                ((EditText)table.findViewById(R.id.v_t_subscribe_topic_qos)).setText("Qos1");
                                break;
                            case R.id.Qos2:
                                ((EditText)table.findViewById(R.id.v_t_subscribe_topic_qos)).setText("Qos2");
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        new AlertDialog.Builder(ClientActivity.this)
                .setTitle("话题订阅")
                .setView(table)
                .setPositiveButton("订阅", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TopicInformation ti = new TopicInformation();
                        ti.setTpoicType(TopicInformation.TOPICTYPE.SUBSCRIBE);
                        ti.setTopicName(((EditText)(table.findViewById(R.id.v_t_subscribe_topic_name))).getText().toString());
                        ti.setQos(((EditText)(table.findViewById(R.id.v_t_subscribe_topic_qos))).getText().toString());
                        binder.subscribe(ci.getId() ,ti);
                        flushView();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ClientActivity.this, "取消订阅", Toast.LENGTH_LONG).show();
                    }
                })
                .create()
                .show();
    }

    /**
     * 发布话题动作
     */
    private void publishTopic(){
        final LinearLayout table = (LinearLayout) getLayoutInflater().inflate(R.layout.publish_topic_table, null);
        table.findViewById(R.id.v_t_publish_topic_qos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ClientActivity.this, v);
                getMenuInflater().inflate(R.menu.qos_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.Qos0:
                                ((EditText)table.findViewById(R.id.v_t_publish_topic_qos)).setText("Qos0");
                                break;
                            case R.id.Qos1:
                                ((EditText)table.findViewById(R.id.v_t_publish_topic_qos)).setText("Qos1");
                                break;
                            case R.id.Qos2:
                                ((EditText)table.findViewById(R.id.v_t_publish_topic_qos)).setText("Qos2");
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        new AlertDialog.Builder(ClientActivity.this)
                .setTitle("话题发布")
                .setView(table)
                .setPositiveButton("发布", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TopicInformation ti = new TopicInformation();
                        ti.setTpoicType(TopicInformation.TOPICTYPE.PUBLISH);
                        ti.setTopicName(((EditText)(table.findViewById(R.id.v_t_publish_topic_name))).getText().toString());
                        ti.setQos(((EditText)(table.findViewById(R.id.v_t_publish_topic_qos))).getText().toString());
                        binder.publishTopic(ci.getId() ,ti);
                        flushView();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ClientActivity.this, "取消发布", Toast.LENGTH_LONG).show();
                    }
                })
                .create()
                .show();
    }

    /**
     * 按钮添加监听器
     */
    protected void addListener(){
        topics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ClientActivity.this, MessagePageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("topic",ci.getTopicInformation().get(position));
                bundle.putString("client_id", ci.getId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * 刷新界面
     */
    public void flushView(){
        mapList.clear();
        ci = binder.getMQTTClientThread(ci.getId()).getClientInformation();
        if(ci.getTopicInformation().size() != 0){
            for(TopicInformation ti : ci.getTopicInformation()){
                Map<String, Object> map = new HashMap<>();
                map.put("topic", ti.getTopicName());
                map.put("type",ti.getTpoicType());
                mapList.add(map);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, mapList, R.layout.topic_adapter_layout,
                new String[]{"topic","type"}, new int[]{R.id.v_topic_name, R.id.v_topic_type});
        topics.setAdapter(adapter);
    }

    /**
     * Handler 类
     */
    public class ClientActivityHandler extends AbsMyHandler{
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            HelpMess helpMess = (HelpMess)msg.obj;
            if(helpMess.isError()){
                Toast.makeText(ClientActivity.this, helpMess.getErrorMessage(), Toast.LENGTH_LONG).show();
            } else {
                if(helpMess.getId().equals(ci.getId())){

                }
            }
            super.handleMessage(msg);
        }
    }
}
