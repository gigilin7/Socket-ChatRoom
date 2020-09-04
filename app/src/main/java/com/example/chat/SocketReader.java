package com.example.chat;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SocketReader extends Thread {

    private Handler msgHandler;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    //取用共享變數時使用synchronized或volatile
    //volatile:會阻止使用暫存器數值這種"偷懶"的優化行為，以確保變數數值的一致性
    private volatile boolean isConnected = true;

    public SocketReader(Handler msgHandler, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.msgHandler = msgHandler;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public void disconnect() {
        isConnected = false;
    }

    @Override
    public void run() {
        super.run();
        while (isConnected) {
            try {
                while (isConnected) {
                    String str = dataInputStream.readUTF(); //讀取使用經修訂的UTF-8編碼的基礎輸出流，並以string形式返回
                    if (msgHandler != null) {
                        Message message = Message.obtain(); //不能用"new Message"的方式來獲取，必須使用obtain()的方式來獲取Message對象
                        message.what = 2; //用戶自定義的消息碼讓接受者識別消息種類,int類型
                        message.obj = str; //Message自帶的Object類型對象，用來傳遞一些對象，兼容性最高避免對齊進行類型轉換等
                        msgHandler.sendMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                msgHandler.sendEmptyMessage(3);
            }
        }

    }
}
