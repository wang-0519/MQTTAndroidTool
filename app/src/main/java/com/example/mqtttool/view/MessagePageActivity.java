package com.example.mqtttool.view;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

import client.Message;
import client.TopicInformation;

public class MessagePageActivity extends AppCompatActivity {

    private TextView messageInput = null;
    private ListView messagesView = null;
    private ActionBar actionBar = null;

    private TopicInformation ti = null;
    private String clientId = null;
    private ArrayList<Message> messages = null;
    private List<Map<String, Object>> mapList = null;

    private MessagePageHandler handler = null;
    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ClientService.MyBinder)service;
            binder.setHandler(handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_page);
        messageInput = findViewById(R.id.v_message_input);
        messagesView = findViewById(R.id.v_messages_view);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mapList = new ArrayList<Map<String, Object>>();
        ti = (TopicInformation) getIntent().getSerializableExtra("topic");
        clientId = getIntent().getStringExtra("client_id");
        setTitle("话题:" + ti.getTopicName());

        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);

        if(ti != null && ti.getTpoicType() == TopicInformation.TOPICTYPE.PUBLISH){
            messageInput.setHeight(50);
        }

        addListener();
        flushView();
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
            case R.id.v_m_history:
                Intent intent = new Intent(MessagePageActivity.this, HistoryMessage.class);
                Bundle bundle = new Bundle();
                bundle.putString("className", "MessagePage");
                bundle.putSerializable("topic", ti);
                bundle.putString("clientId", clientId);
                intent.putExtras(bundle);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void addListener(){
        messagesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MessagePageActivity.this, messages.get(position).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void flushView(){
        if(ti != null){
            mapList.clear();
            messages = ti.getMessages();
            for(Message message : messages){
                Map<String, Object> map = new HashMap<>();
                map.put("message", message.getMessage());
                map.put("time", message.getTime());
                mapList.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, mapList,R.layout.message_page_adapter_layout,
                    new String[]{"message","time"}, new int[]{R.id.v_message_what, R.id.v_message_info});
            messagesView.setAdapter(adapter);
        }
    }

    public class MessagePageHandler extends AbsMyHandler{
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
        }
    }
}
