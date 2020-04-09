package com.example.mqtttool.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtttool.R;

import java.util.Arrays;

import MQTTMessage.AbstractMess;
import helperClass.Translater;

public class MessageInformationPage extends AppCompatActivity {

    private TextView uFixding = null;
    private TextView fixding = null;
    private TextView uVariable = null;
    private TextView variable = null;
    private TextView uPackage = null;
    private TextView packageInfo = null;

    private ActionBar actionBar = null;

    private AbstractMess message = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_info_page);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        uFixding = findViewById(R.id.v_message_ufixding);
        fixding = findViewById(R.id.v_message_fixding);
        uVariable = findViewById(R.id.v_message_uvariable);
        variable = findViewById(R.id.v_message_variable);
        uPackage = findViewById(R.id.v_message_upackage);
        packageInfo = findViewById(R.id.v_message_package);

        message = (AbstractMess)getIntent().getSerializableExtra("uMessage");

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
    }
}
