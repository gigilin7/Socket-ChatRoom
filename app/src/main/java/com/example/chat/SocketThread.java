package com.example.chat;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//Looper ：負責關聯線程以及消息的分發在該線程下，從 MessageQueue 獲取 Message，分發給 Handler ；
//MessageQueue ：是個隊列，負責消息的存儲與管理，負責管理由 Handler 發送過來的 Message ；
//Handler : 負責發送並處理消息，面向開發者，提供 API，並隱藏背後實現的細節。
//一個Handler中只能有一個Looper，一個Looper則可以對應多個Handler
//使用Looper.prepare()和Looper.loop()建立了訊息佇列就可以讓訊息處理在該執行緒中完成

public class SocketThread extends Thread {

    private Socket socket;
    private boolean isConnected = false;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Handler uiHandler;//UIHandler的what值1:代表socket收到新消息，2:代表連接成功，斷連
    private Handler msgHandler;//這個是當前SocketThread子線程的handler，what值1:代表UI線程要發送一個消息，2:代表reader線程讀取到一個消息，3:代表斷連
    private SocketReader socketReader;

    public SocketThread(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    @SuppressLint("HandlerLeak") //@SuppressLint標註忽略指定的警告，因編寫代碼時的一些不規範的寫法導致
    @Override
    public void run() {
        super.run();
        Looper.prepare(); //給執行緒建立一個訊息迴圈
        try {
            //創建一個Socket對象，並指定伺服端的ip及port
            socket = new Socket("你的伺服器ip", 8888); //這裡的ip是你的伺服的ip(電腦ip)
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("~~~~~~~~連接成功~~~~~~~~!");
            isConnected = true;
            uiHandler.sendEmptyMessage(2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Looper.myLooper():獲取當前進程的looper對象
        msgHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    //發送一個消息
                    try {
                        dataOutputStream.writeUTF((String) msg.obj); //將一個字符串寫入使用經修訂的UTF-8編碼的基礎輸出流
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == 2) {
                    //讀取到一個消息
                    Message message = Message.obtain(); //獲取Message對象
                    message.what = 1; //用戶自定義的消息碼讓接受者識別消息種類,int類型
                    message.obj = msg.obj; //用來傳遞一些對象
                    uiHandler.sendMessage(message);
                } else if (msg.what == 3) {
                    disconnect();
                    uiHandler.sendEmptyMessage(3);
                }
            }
        };

        socketReader = new SocketReader(msgHandler, dataInputStream, dataOutputStream);
        socketReader.start();

        Looper.loop(); //使訊息迴圈起作用
    }

    public void disconnect() {
        socketReader.disconnect();
        try {
            if (dataInputStream != null)
                dataInputStream.close();
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (socket != null) {
                socket.close();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (msgHandler != null) {
            Message m = Message.obtain(); //獲取Message對象
            m.what = 1; //用戶自定義的消息碼讓接受者識別消息種類,int類型
            m.obj = message; //用來傳遞一些對象
            msgHandler.sendMessage(m);
        }
    }

}
