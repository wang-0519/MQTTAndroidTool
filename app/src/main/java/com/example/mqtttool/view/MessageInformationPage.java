package com.example.mqtttool.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;

import MQTTMessage.AbstractMess;
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
                        break;
                    case R.id.v_bin_history_hex:
                        break;
                }
            }
        });
    }

    /**
     * 刷新界面
     */
    private void flushView(){
        StringBuffer sb = new StringBuffer();
        byte[] uMess = null;
        if((uMess = message.getFixedHeader()) != null){
            for(byte by : uMess){
                sb.append(Translater.byteToBin(by) + "\n");
            }
            uFixding.setText(sb.toString());
        }
        if((uMess = message.getVariableHeader()) != null){
            sb.delete(0, sb.length());
            for(byte by : uMess){
                sb.append(Translater.byteToBin(by) + "\n");
            }
            uVariable.setText(sb.toString());
        }
        if((uMess = message.getPackageValue()) != null){
            sb.delete(0, sb.length());
            for(byte by : uMess){
                sb.append(Translater.byteToBin(by) + "\n");
            }
            uPackage.setText(sb.toString());
        }

        switch (BytesHandler.getTypeOfMessage(message.getFixedHeader()[0])){
            case 1:
                String str = "报文类型：Connect\n剩余长度：" +  message.getOther_mess().get("remain_length");
                fixding.setText(str);
                break;
            case 2: break;
            case 3: break;
            case 4: break;
            case 5: break;
            case 6: break;
            case 7: break;
            case 8: break;
            case 9: break;
            case 10: break;
            case 11: break;
            case 12: break;
            case 13: break;
            case 14: break;
        }
    }
}
