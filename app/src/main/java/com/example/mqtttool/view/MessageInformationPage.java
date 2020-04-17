package com.example.mqtttool.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;

import java.util.ArrayList;

import MQTTMessage.AbstractMess;
import MQTTMessage.impl.SubscribeMessage;
import MQTTMessage.impl.UnsubackMessage;
import MQTTMessage.impl.UnsubscribeMessage;
import client.TopicInformation;
import helperClass.BytesHandler;
import helperClass.Translater;

/**
 * 二进制报文查看界面
 */
public class MessageInformationPage extends AppCompatActivity {

    //界面组件
    private RadioGroup radio = null;
    private TextView uFixding = null;
    private TextView fixding = null;
    private TextView uVariable = null;
    private TextView variable = null;
    private TextView uPackage = null;
    private TextView packageInfo = null;
    private ActionBar actionBar = null;

    //报文信息
    private AbstractMess message = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_info_page);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        radio = findViewById(R.id.v_bin_history_type);
        uFixding = findViewById(R.id.v_message_ufixding);
        fixding = findViewById(R.id.v_message_fixding);
        uVariable = findViewById(R.id.v_message_uvariable);
        variable = findViewById(R.id.v_message_variable);
        uPackage = findViewById(R.id.v_message_upackage);
        packageInfo = findViewById(R.id.v_message_package);

        message = (AbstractMess)getIntent().getSerializableExtra("uMessage");

        addListener();
        flushView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 添加监听器
     */
    private void addListener(){
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.v_bin_history_bin:
                        setBinString();
                        break;
                    case R.id.v_bin_history_hex:
                        setHexString();
                        break;
                }
            }
        });
    }

    /**
     * 刷新界面
     */
    private void flushView(){
        setBinString();
        String str = null;
        int temp = BytesHandler.getTypeOfMessage(message.getFixedHeader()[0]);
        switch (temp){
            case 1:
                str = "报文类型：Connect\n剩余长度：" +  message.getOther_mess().get("remainLen");
                fixding.setText(str);
                str = "协议级别：0x04\n"
                        + "标志位：\n    用户名：" + (message.getOther_mess().get("userName") == null ? 0 : 1) + "\n"
                        + "    密码：" + (message.getOther_mess().get("password") == null ? 0 : 1) + "\n"
                        + "    遗嘱保留：" + (((message.getOther_mess().get("willRetain"))).equals("true") && message.getOther_mess().get("willTopic") != null ? 1 : 0) + "\n"
                        + "    遗嘱Qos：" + message.getOther_mess().get("willQos") + "\n"
                        + "    遗嘱标志：" + (message.getOther_mess().get("willTopic") == null ? 0 : 1) + "\n"
                        + "    清理会话：" + (message.getOther_mess().get("cleanSession").equals("false") ? 0 : 1) + "\n"
                        + "存活时间：" + message.getOther_mess().get("keepAlive");
                variable.setText(str);
                str = "客户端Id:" + message.getOther_mess().get("clientId") + "\n"
                        + "遗嘱主题：" + message.getOther_mess().get("willTopic") + "\n"
                        + "遗嘱信息：" + message.getOther_mess().get("willMessage") + "\n"
                        + "用户名：" + message.getOther_mess().get("userName") + "\n"
                        + "用户密码：" + message.getOther_mess().get("password");
                packageInfo.setText(str);
                break;
            case 2:
                str = "报文类型：ConnAck\n剩余长度：2";
                fixding.setText(str);
                str = "<-确认标志\n" +
                        "<-连接返回码";
                if(message.getOther_mess().get("errorMessage") != null){
                    str = str + "\n    错误信息：" + message.getOther_mess().get("errorMessage");
                }
                variable.setText(str);
                break;
            case 3:
                str = "报文类型：Publish\nQos:" + message.getOther_mess().get("Qos") + "\n剩余长度：" + message.getOther_mess().get("remainLength");
                fixding.setText(str);
                str = "主题：" + message.getOther_mess().get("topic");
                variable.setText(str);
                str = "消息：" + message.getOther_mess().get("message");
                packageInfo.setText(str);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                if(temp == 4){
                    str = "报文类型：PubAck\n剩余长度：2";
                } else if(temp == 5){
                    str = "报文类型：PubRec\n剩余长度：2";
                } else if(temp == 6){
                    str = "报文类型：PubRel\n剩余长度：2";
                } else {
                    str = "报文类型：PubComp\n剩余长度：2";
                }
                fixding.setText(str);
                str = "<-报文标识符";
                variable.setText(str);
                break;
            case 8:
            case 10:
                str = "报文类型：SubScribe\n剩余长度：" + message.getOther_mess().get("remainLength");
                fixding.setText(str);
                str = "<-报文标识符";
                variable.setText(str);
                str = "话题列表：\n";
                if(temp == 8){
                    for(TopicInformation ti : ((SubscribeMessage)message).getTopics()){
                        str += ("  名称:" + ti.getTopicName() + "\n  Qos:" + ti.getQos() + "\n");
                    }
                } else {
                    for(TopicInformation ti : ((UnsubscribeMessage)message).getTopics()){
                        str += ("  名称:" + ti.getTopicName() + "\n  Qos:" + ti.getQos() + "\n");
                    }
                }
                packageInfo.setText(str);
                break;
            case 9:
            case 11:
                str = "报文类型：SubAck\n剩余长度：" + message.getOther_mess().get("remainLength");
                fixding.setText(str);
                str = "<-报文标识符";
                variable.setText(str);
                if(temp == 9){
                    str = "<-返回码(对应每个订阅的信息的Qos，0x80表示订阅失败)";
                    packageInfo.setText(str);
                }
                break;
            case 12:
                str = "报文类型：PingReq\n剩余长度：0";
                fixding.setText(str);
                break;
            case 13:
                str = "报文类型：PingResp\n剩余长度：0";
                fixding.setText(str);
                break;
            case 14:
                str = "报文类型：Disconnect\n剩余长度：0";
                fixding.setText(str);
                break;
        }
    }

    /**
     * 设置显示二进制报文
     */
    private void setBinString(){
        byte[] uMess = null;
        if((uMess = message.getFixedHeader()) != null){
            uFixding.setText(getBytesToOthers(0, uMess));
        }
        if((uMess = message.getVariableHeader()) != null){
            uVariable.setText(getBytesToOthers(0, uMess));
        }
        if((uMess = message.getPackageValue()) != null){
            uPackage.setText(getBytesToOthers(0, uMess));
        }
    }

    /**
     * 设置显示16进制报文
     */
    private void setHexString(){
        byte[] uMess = null;
        if((uMess = message.getFixedHeader()) != null){
            uFixding.setText(getBytesToOthers(1, uMess));
        }
        if((uMess = message.getVariableHeader()) != null){
            uVariable.setText(getBytesToOthers(1, uMess));
        }
        if((uMess = message.getPackageValue()) != null){
            uPackage.setText(getBytesToOthers(1, uMess));
        }
    }

    /**
     * byte[] 转 不同进制字符串
     * @param temp
     * @param bytes
     * @return
     */
    public String getBytesToOthers(int temp, byte[] bytes){
        StringBuffer sb = new StringBuffer();
        if(temp == 0){
            for(byte by : bytes){
                sb.append(Translater.byteToBin(by) + "\n");
            }
        } else {
            int i = 0;
            for(byte by : bytes){
                sb.append(Translater.byteToHex(by) + " ");
                if(i % 4 == 3){
                    sb.append("\n");
                }
                i++;
            }
        }
        return sb.toString();
    }
}
