package com.example.mqtttool.view;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.service.ClientService;

import client.ClientInformation;

import java.util.UUID;

public class CreateClientActivity extends AppCompatActivity {
    //判断输入是否符合要求
    private int count = 0;

    /**
     * 各组件
     */
    private Button back = null;
    private Button save = null;
    private EditText[] edits = new EditText[11];
    private CheckBox[] checks = new CheckBox[6];
    private ActionBar actionBar = null;

    /**
     * 客户端配置
     */
    private ClientInformation ci = null;

    //返回页面名称
    private String className = null;

    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_clients_layout);
        setTitle("客户端配置信息");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        findView();
        setListener();
        ci = (ClientInformation) getIntent().getSerializableExtra("client");
        className = getIntent().getStringExtra("className");
        if(ci != null){
            setItemInfo();
        } else {
            ci = new ClientInformation();
            edits[1].setText(UUID.randomUUID().toString());
        }
        Intent serviceIntent = new Intent(CreateClientActivity.this, ClientService.class);
        bindService(serviceIntent, sc, Service.BIND_AUTO_CREATE);
    }

    /**
     * 寻找组件
     */
    private void findView(){
        edits[0] = findViewById(R.id.v_conn_client_name);
        edits[1] = findViewById(R.id.v_client_id);
        edits[2] = findViewById(R.id.v_user_name);
        edits[3] = findViewById(R.id.v_password);
        edits[4] = findViewById(R.id.v_addr);
        edits[5] = findViewById(R.id.v_reconn_period);
        edits[6] = findViewById(R.id.v_conn_timeout);
        edits[7] = findViewById(R.id.v_keep_alive);
        edits[8] = findViewById(R.id.v_will_topic);
        edits[9] = findViewById(R.id.v_will_qos);
        edits[10] = findViewById(R.id.v_will_message);
        checks[0] = findViewById(R.id.v_reschedule_ping);
        checks[1] = findViewById(R.id.v_clean_session);
        checks[2] = findViewById(R.id.v_auto_conn);
        checks[3] = findViewById(R.id.v_mqtt_version);
        checks[4] = findViewById(R.id.v_queue_qos0);
        checks[5] = findViewById(R.id.v_will_retain);
        save = findViewById(R.id.v_save);
        back = findViewById(R.id.v_back);
    }

    protected void setItemInfo(){
        edits[0].setText(ci.getName());
        edits[1].setText(ci.getId());
        edits[2].setText(ci.getUserName());
        edits[3].setText(ci.getPassword());
        edits[4].setText(ci.getAddr());
        edits[5].setText("" + ci.getReconnPeriod());
        edits[6].setText("" + ci.getConnTimeout());
        edits[7].setText("" + ci.getKeepAlive());
        edits[8].setText(ci.getWillTopic());
        edits[9].setText("Qos" + ci.getWillQos());
        edits[10].setText(ci.getWillMessage());
        checks[0].setChecked(ci.isReschedulePing());
        checks[1].setChecked(ci.isCleanSession());
        checks[2].setChecked(ci.isAutoConn());
        checks[3].setChecked(ci.isMqttVersion());
        checks[4].setChecked(ci.isQueueQos0());
        checks[5].setChecked(ci.isWillRetain());
    }

    protected void setListener(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.v_save:
                        saveInfo();
                        if(count == 8){
                            Intent intent = null;
                            if(className.equals("MainPageActivity")){
                                binder.newClient(ci);
                                intent = new Intent(CreateClientActivity.this, MainPageActivity.class);
                            } else {
                                binder.updateClient(ci);
                                intent = new Intent(CreateClientActivity.this, ClientActivity.class);
                            }
                            startActivity(intent);
                        } else {
                            Toast.makeText(CreateClientActivity.this, "请按要求填写信息！", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.v_back:
                        Intent intent = null;
                        if(className.equals("MainPageActivity")){
                            intent = new Intent(CreateClientActivity.this, MainPageActivity.class);
                        } else if(className.equals("ClientActivity")){
                            intent = new Intent(CreateClientActivity.this, ClientActivity.class);
                        }
                        startActivity(intent);
                        break;
                    case R.id.v_will_qos:
                        PopupMenu popupMenu = new PopupMenu(CreateClientActivity.this, v);
                        getMenuInflater().inflate(R.menu.qos_popup_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.Qos0:
                                        edits[9].setText("Qos0");
                                        break;
                                    case R.id.Qos1:
                                        edits[9].setText("Qos1");
                                        break;
                                    case R.id.Qos2:
                                        edits[9].setText("Qos2");
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                        break;
                }
            }
            /**
             * 从界面中读取用户配置
             */
            private void saveInfo(){
                count = 0;
                if(!edits[0].getText().toString().isEmpty()){
                    ci.setName(edits[0].getText().toString());
                    count++;
                }
                if(!edits[1].getText().toString().isEmpty()){
                    ci.setId(edits[1].getText().toString());
                    count++;
                }
                if(!edits[2].getText().toString().isEmpty()){
                    ci.setUserName(edits[2].getText().toString());
                    count++;
                }
                if(!edits[3].getText().toString().isEmpty()){
                    ci.setPassword(edits[3].getText().toString());
                }else {
                    ci.setPassword(null);
                }
                if(!edits[4].getText().toString().isEmpty()){
                    ci.setAddr(edits[4].getText().toString());
                    count++;
                }
                if(!edits[5].getText().toString().isEmpty()){
                    ci.setReconnPeriod(Integer.valueOf(edits[5].getText().toString()));
                    count++;
                }
                if(!edits[6].getText().toString().isEmpty()){
                    ci.setConnTimeout(Integer.valueOf(edits[6].getText().toString()));
                    count++;
                }
                if(!edits[7].getText().toString().isEmpty()){
                    ci.setKeepAlive(Integer.valueOf(edits[7].getText().toString()));
                    count++;
                }
                if(!edits[8].getText().toString().isEmpty()){
                    ci.setWillTopic(edits[8].getText().toString());
                } else {
                    ci.setWillTopic(null);
                }
                if(!edits[9].getText().toString().isEmpty()){
                    if(edits[9].getText().toString().equals("Qos0")){
                        ci.setWillQos(0);
                    } else if(edits[9].getText().toString().equals("Qos1")){
                        ci.setWillQos(1);
                    } else if(edits[9].getText().toString().equals("Qos2")){
                        ci.setWillQos(2);
                    }
                    count++;
                }
                if(!edits[10].getText().toString().isEmpty()){
                    ci.setWillMessage(edits[10].getText().toString());
                }else {
                    ci.setWillMessage(null);
                }
                if(checks[0].isChecked()){
                    ci.setReschedulePing(true);
                } else {
                    ci.setReschedulePing(false);
                }
                if(checks[1].isChecked()){
                    ci.setCleanSession(true);
                } else {
                    ci.setCleanSession(false);
                }
                if(checks[2].isChecked()){
                    ci.setAutoConn(true);
                } else {
                    ci.setAutoConn(false);
                }
                if(checks[3].isChecked()){
                    ci.setMqttVersion(true);
                } else {
                    ci.setMqttVersion(false);
                }
                if(checks[4].isChecked()){
                    ci.setQueueQos0(true);
                } else {
                    ci.setQueueQos0(false);
                }
                if(checks[5].isChecked()){
                    ci.setWillRetain(true);
                } else {
                    ci.setWillRetain(false);
                }
            }
        };
        save.setOnClickListener(listener);
        back.setOnClickListener(listener);
        edits[9].setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
