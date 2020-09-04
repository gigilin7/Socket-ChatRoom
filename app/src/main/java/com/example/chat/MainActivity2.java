package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private SocketThread socketThread;
    private StringBuilder stringBuilder = new StringBuilder();
    private EditText contentEt;
    private Button sendBtn;

    private List<Msg> msgList = new ArrayList<>();
    private List<String> client_name = new ArrayList<>();//名字的列表


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String message = (String) msg.obj;
                Log.i("收到的信息i", message);
                String[] s = message.split("#");
                stringBuilder.delete(0,stringBuilder.length()); //清空stringBuilder
                stringBuilder.append(s[0]);//名字
                stringBuilder.append("\n");
                stringBuilder.append(s[1]);//伺服器或其他人說的話
                msgList.add(new Msg(stringBuilder.toString(), Msg.TYPE.RECEIVED)); //手機顯示伺服器(或其他人)傳的訊息
                RecyclerView msgRecyclerView=(RecyclerView)findViewById(R.id.msg);
                MsgAdapter adapter=new MsgAdapter(msgList);
                msgRecyclerView.setAdapter(adapter);
                //如果有新訊息，則設置適配器的長度（通知適配器，有新的數據被插入），並讓 RecyclerView 定位到最後一行
                int newSize = msgList.size() - 1;
                adapter.notifyItemInserted(newSize);
                msgRecyclerView.scrollToPosition(newSize);
            } else if (msg.what == 2) {
                Toast.makeText(MainActivity2.this, "連接成功", Toast.LENGTH_LONG).show();
            } else if (msg.what == 3) {
                Toast.makeText(MainActivity2.this, "連接断開", Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //跳頁
        Intent intent = getIntent();
        final String name = intent.getStringExtra("name");//name是使用者輸入的名字

        client_name.add(name);//自己的名字存起來

        
        contentEt = (EditText) findViewById(R.id.et_content);
        sendBtn = (Button) findViewById(R.id.btn_send);
        socketThread = new SocketThread(mHandler);
        socketThread.start();

        //聊天框
        final RecyclerView msgRecyclerView=(RecyclerView)findViewById(R.id.msg);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);

        final MsgAdapter adapter=new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //有輸入才能傳送訊息
                if("".equals(contentEt.getText().toString()))
                    return;
                stringBuilder.delete(0,stringBuilder.length()); //清空stringBuilder
                stringBuilder.append(contentEt.getText().toString());
                msgList.add(new Msg(stringBuilder.toString(), Msg.TYPE.SENT)); //手機顯示自己傳的訊息

                String message = name + ":#" + contentEt.getText().toString();   //加name傳!!!!!!!!!
                socketThread.sendMessage(message);
               


                //如果有新消息，則設置適配器的長度（通知適配器，有新的數據被插入），並讓 RecyclerView 定位到最後一行
                int newSize = msgList.size() - 1;
                adapter.notifyItemInserted(newSize);
                msgRecyclerView.scrollToPosition(newSize);

                //清空輸入框的內容
                contentEt.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketThread.disconnect();
    }

    //右上方3點選單(動態增加選單項目)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        for (int i=0; i<client_name.size(); i++){
            menu.add(i,i,i,"<名字>" + client_name.get(i) );
        }

        return true;
    }

    // 按下手機返回鍵出現確認離開視窗
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){   //確定按下退出鍵and防止重複按下退出鍵
            dialog();
        }
        return false;
    }


    //按返回鍵顯示是否要離開的選項
    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this); //創建訊息方塊
        builder.setMessage("確定要離開？");
        builder.setTitle("離開");
        builder.setPositiveButton("確認", new DialogInterface.OnClickListener()  {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //dismiss為關閉dialog,Activity還會保留dialog的狀態
                MainActivity2.this.finish();//關閉activity
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()  {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        builder.create().show();

    }

}
