package com.example.mqtttool.view;

import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MQTTMessage.AbstractMess;
import client.ClientInformation;
import client.TopicInformation;
import helperClass.BytesHandler;

public class HistoryMessage extends AppCompatActivity {

    private ListView history = null;
    private ActionBar actionBar = null;

    private ClientInformation client = null;
    private TopicInformation topic = null;
    private String className = null;
    private String clientId = null;

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

        flushView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void flushView(){
        SimpleAdapter sa = null;
        List<Map<String, Object>> mapList = new ArrayList<>();
        if(className.equals("ClientActivity")){
            client = (ClientInformation) getIntent().getSerializableExtra("client");
            uMessages = client.getHistoryMessage();
            for(AbstractMess absMess : uMessages){
                Map<String, Object> map = new HashMap<>();
                map.put("type", getMessType(absMess));
                mapList.add(map);
            }
            sa = new SimpleAdapter(this, mapList, R.layout.history_ubytes_adapter,
                    new String[]{"type"}, new int[]{R.id.v_history_message_type});
            history.setAdapter(sa);
        } else {
            clientId = getIntent().getStringExtra("clientId");
            topic = (TopicInformation)getIntent().getSerializableExtra("topic");
            cMessages = new ArrayList<>();
        }
    }

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
}
