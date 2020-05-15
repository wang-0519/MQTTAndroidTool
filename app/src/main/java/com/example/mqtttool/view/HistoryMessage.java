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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.service.ClientService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MQTTMessage.AbstractMess;
import client.ClientInformation;
import client.Message;
import client.TopicInformation;
import helperClass.BytesHandler;
import helperClass.Translater;

/**
 * 历史记录查看界面
 */
public class HistoryMessage extends AppCompatActivity {

    //界面组件
    private ListView history = null;
    private ActionBar actionBar = null;

    //客户端信息
    private ClientInformation client = null;
    private TopicInformation topic = null;
    private String className = null;
    private String clientId = null;

    //ClientService 绑定
    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
            if(!className.equals("ClientActivity")){
                cMessages = binder.getHistory(clientId, topic);
                flushView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //界面信息填充内容
    private ArrayList<AbstractMess> uMessages = null;
    private ArrayList<Message>  cMessages = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_message_page);

        history = findViewById(R.id.v_history_message);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("历史记录");

        className = getIntent().getStringExtra("className");

        if(className.equals("ClientActivity")){
            client = (ClientInformation) getIntent().getSerializableExtra("client");
        } else {
            clientId = getIntent().getStringExtra("clientId");
            topic = (TopicInformation) getIntent().getSerializableExtra("topic");
        }

        Intent intent = new Intent(HistoryMessage.this, ClientService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);

        flushView();
        addListener();
    }

    @Override
    protected void onDestroy() {
        if(binder != null){
            unbindService(sc);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_message_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.v_m_history_clear:
                new AlertDialog.Builder(HistoryMessage.this)
                        .setTitle("确认清除？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(className.equals("ClientActivity")){
                                    binder.clearHistoryMessage(client.getId());
                                    client = binder.getMQTTClientThread(client.getId()).getClientInformation();
                                } else {
                                    binder.clearHistory(clientId, topic);
                                }
                                flushView();
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

    //刷新界面，填充 ListView
    private void flushView(){
        SimpleAdapter sa = null;
        List<Map<String, Object>> mapList = new ArrayList<>();
        if(className.equals("ClientActivity")){
            uMessages = client.getHistoryMessage();
            for(AbstractMess absMess : uMessages){
                Map<String, Object> map = new HashMap<>();
                map.put("type", getMessType(absMess));
                map.put("dir", absMess.getMessDir());
                mapList.add(map);
            }
            sa = new SimpleAdapter(this, mapList, R.layout.history_ubytes_adapter,
                    new String[]{"type","dir"}, new int[]{R.id.v_history_message_type,R.id.v_history_message_dir});
            history.setAdapter(sa);
        } else {
            if(binder != null){
                cMessages = binder.getHistory(clientId, topic);
            }
            if(cMessages != null){
                for(Message message : cMessages){
                    Map<String,Object> map = new HashMap<>();
                    map.put("message", message.getMessage());
                    String info = "Qos:Qos" + message.getQos() + "\nisRetain:" + message.isRetain() + "\ntime:" + message.getTime();
                    map.put("info", info);
                    mapList.add(map);
                }
                sa = new SimpleAdapter(this, mapList, R.layout.message_page_adapter_layout,
                        new String[]{"message","info"}, new int[]{R.id.v_message_what, R.id.v_message_info});
                history.setAdapter(sa);
            }
        }
    }

    //获取报文类型
    private String getMessType(AbstractMess message){
        switch (BytesHandler.getTypeOfMessage(message.getTypeOfMess()[0])){
            case 1:  return "Connect";
            case 2:  return "ConnAck";
            case 3:  return "Publish";
            case 4:  return "PubAck";
            case 5:  return "PubRec";
            case 6:  return "PubRel";
            case 7:  return "PubComp";
            case 8:  return "Subscribe";
            case 9:  return "SubAck";
            case 10:  return "Unsubscribe";
            case 11:  return "UnsubAck";
            case 12:  return "PingReq";
            case 13:  return "PingResp";
            case 14:  return "DisConnect";
        }
        return null;
    }

    /**
     * 添加监听器
     */
    private void addListener(){
        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(className.equals("ClientActivity")) {
                    Intent intent = new Intent(HistoryMessage.this, MessageInformationPage.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("uMessage", uMessages.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    RelativeLayout table = (RelativeLayout)getLayoutInflater().inflate(R.layout.message_info_table, null);
                    final byte[] bytes = cMessages.get(position).getBytes();
                    final TextView messageInfo = table.findViewById(R.id.v_message_t_text);
                    String str = "";
                    int count = 0;
                    while(count < bytes.length){
                        str += bytes[count++] + " ";
                    }
                    messageInfo.setText(str);
                    table.findViewById(R.id.v_message_t_bin).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String str = "";
                            int count = 0;
                            while(count < bytes.length){
                                str += (Translater.byteToBin(bytes[count++]) + "  ");
                            }
                            messageInfo.setText(str);
                        }
                    });
                    table.findViewById(R.id.v_message_t_integer).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String str = "";
                            int count = 0;
                            while(count < bytes.length){
                                str += (bytes[count++]) + " ";
                            }
                            messageInfo.setText(str);
                        }
                    });
                    table.findViewById(R.id.v_message_t_hex).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String str = "";
                            int count = 0;
                            while(count < bytes.length){
                                str += (Translater.byteToHex(bytes[count++])) + " ";
                            }
                            messageInfo.setText(str);
                        }
                    });
                    new AlertDialog.Builder(HistoryMessage.this)
                            .setTitle("报文查看")
                            .setView(table)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).show();
                }
            }
        });
    }
}
