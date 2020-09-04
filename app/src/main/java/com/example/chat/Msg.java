package com.example.chat;

public class Msg {
    /**
     * 内容
     */
    private String content;

    /**
     * 類型
     */
    private TYPE type;

    public enum TYPE{
        /**
         * 接收
         */
        RECEIVED,
        /**
         * 發送
         */
        SENT
    }


    public Msg(String content,TYPE type){
        this.content = content;
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
