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
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;
import com.example.mqtttool.service.ClientService;

import client.Message;
import client.TopicInformation;
import helperClass.Translater;

public class PublishMessagePageActivity extends AppCompatActivity {

    private EditText publishQos = null;
    private CheckBox publishRetain = null;
    private RadioGroup publishType = null;
    private EditText publishMessage = null;
    private Button publish = null;
    private ActionBar actionBar = null;

    private String clientId = null;
    private TopicInformation topicInformation = null;

    private ClientService.MyBinder binder = null;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (ClientService.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_message_page);
        publishQos = findViewById(R.id.v_publish_message_qos);
        publishRetain = findViewById(R.id.v_publish_message_retain);
        publishType = findViewById(R.id.v_publish_message_type);
        publishMessage = findViewById(R.id.v_publish_message);
        publish = findViewById(R.id.v_publish_button);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        clientId = getIntent().getStringExtra("clientId");
        topicInformation = (TopicInformation) getIntent().getSerializableExtra("topic");
        actionBar.setTitle("话题：" + topicInformation.getTopicName());

        Intent intent = new Intent(PublishMessagePageActivity.this, ClientService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);

        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(binder != null){
            unbindService(sc);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addListener(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.v_publish_message_qos:
                        PopupMenu popupMenu = new PopupMenu(PublishMessagePageActivity.this, view);
                        getMenuInflater().inflate(R.menu.qos_popup_menu, popupMenu.getMenu());
                        popupMenu.show();
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.Qos0:
                                        ((EditText)findViewById(R.id.v_publish_message_qos)).setText("Qos0");
                                        break;
                                    case R.id.Qos1:
                                        ((EditText)findViewById(R.id.v_publish_message_qos)).setText("Qos1");
                                        break;
                                    case R.id.Qos2:
                                        ((EditText)findViewById(R.id.v_publish_message_qos)).setText("Qos2");
                                        break;
                                }
                                return true;
                            }
                        });
                        break;
                    case R.id.v_publish_button:
                        if(publishMessage.getText().length() != 0){
                            boolean bool = true;
                            int count = 0;
                            Message message = null;
                            switch (publishType.getCheckedRadioButtonId()){
                                case R.id.v_b_string:
                                    message = new Message(publishMessage.getText().toString());
                                    break;
                                case R.id.v_b_bin:
                                    String binstr = publishMessage.getText().toString();
                                    binstr = binstr.replaceAll(" ", "");
                                    binstr = binstr.replaceAll("\n", "");
                                    if(binstr.length() % 8 != 0){
                                        int k = 8 - binstr.length() % 8;
                                        while(k != 0){
                                            binstr = "0" + binstr;
                                            k--;
                                        }
                                    }
                                    while(count < binstr.length() && "01".indexOf(binstr.charAt(count)) != -1){
                                        count++;
                                    }
                                    if(count < binstr.length()){
                                        bool = false;
                                    } else {
                                        message = new Message(Translater.binToBytes(binstr));
                                    }
                                    break;
                                case R.id.v_b_hex:
                                    String hexstr = publishMessage.getText().toString();
                                    hexstr = hexstr.replaceAll(" ", "");
                                    hexstr = hexstr.replaceAll("\n", "");
                                    if(hexstr.length() % 2 != 0){
                                        hexstr = "0" + hexstr;
                                    }
                                    while(count < hexstr.length() && "013456789ABCDEF".indexOf(hexstr.charAt(count)) != -1){
                                        count++;
                                    }
                                    if(count < hexstr.length()){
                                        bool = false;
                                    } else {
                                        message = new Message(Translater.hexTobytes(hexstr));
                                    }
                                    break;
                            }
                            if(bool){
                                message.setQos(publishQos.getText().toString());
                                message.setRetain(publishRetain.isChecked());
                                binder.publish(clientId, topicInformation, message);
                                Intent intent = new Intent(PublishMessagePageActivity.this, MessagePageActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(PublishMessagePageActivity.this, "输入内容格式错误！", Toast.LENGTH_LONG).show();
                            }
                        } else{
                            Toast.makeText(PublishMessagePageActivity.this, "内容为空！", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        };
        publishQos.setOnClickListener(listener);
        publish.setOnClickListener(listener);

        publishType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                publishMessage.setText("");
            }
        });
    }
}
