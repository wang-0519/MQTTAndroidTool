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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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

import client.HelpMess;
import client.Message;
import client.TopicInformation;

/**
 * 发送或接收消息查看界面
 */
public class MessagePageActivity extends AppCompatActivity {

    //界面组件
    private LinearLayout messageInput = null;
    private ListView messagesView = null;
    private ActionBar actionBar = null;

    //客户端信息
    private TopicInformation ti = null;
    private String clientId = null;

    //界面填充信息
    private ArrayList<Message> messages = null;
    private List<Map<String, Object>> mapList = null;

    //Handler对象
    private MessagePageHandler handler = null;

    //ClientService 绑定
    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
            binder.setHandler(handler);
            flushView();
            handler.setBinder(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_page);
        messagesView = findViewById(R.id.v_messages_view);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mapList = new ArrayList<Map<String, Object>>();
        ti = (TopicInformation) getIntent().getSerializableExtra("topic");
        clientId = getIntent().getStringExtra("client_id");
        setTitle("话题:" + ti.getTopicName());
        handler = new MessagePageHandler();

        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(binder != null){
            flushView();
        }
    }

    @Override
    protected void onDestroy(){
        binder.deleteHandler(handler);
        unbindService(sc);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.v_m_message_publish:
                if(ti.getTpoicType() == TopicInformation.TOPICTYPE.PUBLISH){
                    Intent intent = new Intent(MessagePageActivity.this, PublishMessagePageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("clientId", clientId);
                    bundle.putSerializable("topic",ti);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(MessagePageActivity.this, "订阅主题不可用", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.v_m_history:
                Intent intent = new Intent(MessagePageActivity.this, HistoryMessage.class);
                Bundle bundle = new Bundle();
                bundle.putString("className", "MessagePage");
                bundle.putSerializable("topic", ti);
                bundle.putString("clientId", clientId);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.v_m_delete_topic:
                new AlertDialog.Builder(MessagePageActivity.this)
                        .setTitle("确认删除此话题所有相关信息？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binder.deleteTopicInformation(clientId, ti);
                                Intent in = null;
                                in = new Intent(MessagePageActivity.this, ClientActivity.class);
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
     * 界面刷新
     */
    protected void flushView(){
        ti = binder.getMQTTClientThread(clientId).getClientInformation().getTopic(ti.getTopicName(), ti.getTpoicType());
        if(ti != null){
            mapList.clear();
            messages = ti.getMessages();
            for(Message message : messages){
                Map<String, Object> map = new HashMap<>();
                map.put("message", message.getMessage());
                String info = "Qos:Qos" + message.getQos() + "\nisRetain:" + message.isRetain() + "\ntime:" + message.getTime();
                map.put("info", info);
                mapList.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, mapList,R.layout.message_page_adapter_layout,
                    new String[]{"message","info"}, new int[]{R.id.v_message_what, R.id.v_message_info});
            messagesView.setAdapter(adapter);
        }
    }

    public class MessagePageHandler extends AbsMyHandler{
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            HelpMess helpMess = (HelpMess)msg.obj;
            if(helpMess.isError()){
                Toast.makeText(MessagePageActivity.this, helpMess.getErrorMessage(), Toast.LENGTH_LONG).show();
            } else{
                if(helpMess.getId().equals(clientId) && helpMess.getTopic().equals(ti.getTopicName()) && ti.getTpoicType() == TopicInformation.TOPICTYPE.SUBSCRIBE){
                    flushView();
                    MessagePageActivity.this.binder.setNewFalse(clientId, ti);
                }
            }
            super.handleMessage(msg);
        }
    }
}
