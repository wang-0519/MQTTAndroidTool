package com.example.mqtttool.test;

import org.junit.Test;

import client.Message;

public class Test1 {

    @Test
    public void stringTest(){
        Message message = new Message("abc");
        message.setRetain(false);
        message.setQos(0);
        String str = "Qos:Qos" + message.getQos() + "\nisRetain:" + message.isRetain() + "\ntime:" + message.getTime();
        System.out.println(str);
    }
}
