package com.example.server;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    public interface OnServiceListener {
        void onClientChanged(List<Client> clients);

        void onNewMessage(String message, Client client);
    }

    private OnServiceListener listener;

    public void setOnServiceListener(OnServiceListener listener) {
        this.listener = listener;
    }

    boolean started = false;//是否已經啟動
    ServerSocket ss = null;
    List<Client> clients = new ArrayList<Client>();//客戶端的列表

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();//呼叫父類中方法與變數(因子類中的成員變數或方法與父類中的成員變數或方法同名)
        try {
            ss = new ServerSocket(8888);//port:8888
            started = true;
            System.out.println("server is started");
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (BindException e) {  //若port已經被使用
            System.out.println("port is not available....");
            System.out.println("please restart");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (started) {
                Socket s = ss.accept();//持續監聽新的socket連接
                Client c = new Client(s, Server.this);
                System.out.println("a client connected!");
                new Thread(c).start();
                addClient(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { //不管是否有發生例外都會執行finally的內容
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //取用共享變數時使用synchronized或volatile
    //synchronized:程式中會取出某一個共用的物件且會判斷物件內容值，再更新物件內容(避免同時執行兩個動作)
    public synchronized void sendMessage(String msg) {
        for (Client client1 : clients) { //每個clients
            client1.send(msg);
        }
    }

    public synchronized void newMessage(String msg, Client client) {
        if (listener != null) {
            listener.onNewMessage(msg, client);
            for (Client client1 : clients) {
                if (!client1.equals(client)) {
                    String[] strings = msg.split("#");
                    client1.send(strings[0] + "#" + strings[1]);
                }
            }
        }
    }

    public synchronized void addClient(Client client) {
        clients.add(client);
        if (listener != null) {
            listener.onClientChanged(clients);
        }
    }


    public synchronized void removeClient(Client client) {
        clients.remove(client);
        if (listener != null) {
            listener.onClientChanged(clients);
        }
    }

}
