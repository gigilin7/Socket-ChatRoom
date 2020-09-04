package com.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable{

    private Socket s;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private boolean connected = false;
    private Server server;

    public Socket getSocket() {
        return s;
    }

    public Client(Socket s, Server ser) {
        this.s=s;
        this.server = ser;
        try {
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //訊息傳送
    public void send(String str) {
        try {
            dos.writeUTF(str);//將一個字符串寫入使用經修訂的UTF-8編碼的基礎輸出流
        } catch (IOException e) {
            server.removeClient(this);
        }
    }

    //訊息接收
    public void run() {
        try {
            while (connected) {
                String str = dis.readUTF();//讀取使用經修訂的UTF-8編碼的基礎輸出流，並以string形式返回
                server.newMessage(str,this);
            }
        } catch (EOFException e) {
            System.out.println("Client closed!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {  //不管是否有發生例外都會執行finally的內容
            try {
                if (dis != null)
                    dis.close();
                if (dos != null)
                    dos.close();
                if (s != null) {
                    server.removeClient(this);
                    s.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    //比較兩個物件的實質相等性(不能用==要用equals()方法)
    //equals()跟hashCode()都要override
    @Override
    public boolean equals(Object o) { //比較兩個物件的實質相等性(不能用==要用equals()方法)
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return s.equals(client.s);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
