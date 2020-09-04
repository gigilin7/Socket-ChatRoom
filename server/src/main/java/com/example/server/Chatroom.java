package com.example.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chatroom extends JFrame implements Server.OnServiceListener, ActionListener {
    private JLabel clientLabel;
    private JList clientList;
    private JLabel historyLabel;
    private JScrollPane jScrollPane;
    private JTextArea historyContentLabel;
    private JTextField messageText;
    private JButton sendButton;
    private Server server;
    private StringBuffer buffers;


    public Chatroom() {
        buffers = new StringBuffer();
        clientLabel = new JLabel("Client List");//客户列表
        clientLabel.setBounds(0, 0, 100, 30);
        clientList = new JList<>();
        clientList.setBounds(0, 30, 100, 270);
        historyLabel = new JLabel("Chat Record");//聊天紀錄
        historyLabel.setBounds(100, 0, 500, 30);

        historyContentLabel = new JTextArea();
        jScrollPane = new JScrollPane(historyContentLabel);
        jScrollPane.setBounds(100, 30, 510, 230);
        //分別設置水平和垂直滾動條自動出現
        jScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        historyContentLabel.setEditable(false);

        messageText = new JTextField();
        messageText.setBounds(100, 270, 440, 30);
        sendButton = new JButton("Send");//發送
        sendButton.setBounds(540, 270, 70, 30);


        sendButton.addActionListener(this);
        this.setLayout(null);

        add(clientLabel);
        add(clientList);
        add(historyLabel);
        add(jScrollPane);
        add(messageText);
        add(sendButton);

        //設置窗體
        this.setTitle("Chat Room");//聊天室   //窗體標籤
        this.setSize(650, 400);//窗體大小 //原本:600，330
        this.setLocationRelativeTo(null);//在屏幕中間顯示(居中顯示)
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//退出關閉JFrame
        this.setVisible(true);//顯示窗體
        this.setResizable(false);

        server = new Server();
        server.setOnServiceListener(this);
        server.start();
    }

    @Override
    public void onClientChanged(List<Client> clients) {
        // TODO Auto-generated method stub
        clientList.setListData(clients.toArray());

    }


    @Override
    public void onNewMessage(String message, Client client) {
        // TODO Auto-generated method stub
        String[] strings = message.split("#");
        buffers.append(strings[0] + "\n" + strings[1] + "\n");
        historyContentLabel.setText(buffers.toString()); //伺服器顯示手機傳的內容
        historyContentLabel.setCaretPosition(historyContentLabel.getDocument().getLength());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if (e.getSource() == sendButton) {
            server.sendMessage("Server:#" + messageText.getText());
            buffers.append("Server:" + "\n");
            buffers.append(messageText.getText() + "\n");
            historyContentLabel.setText(buffers.toString()); //伺服器顯示自己傳的內容

            messageText.setText("");
        }
    }


}
