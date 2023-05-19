package com.tencent.scaxz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.json.JSONException;


import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    public static MainActivity mainActivity;

    Button connectButton, sendButton;
    EditText  idEdittext, infoEdittext, targetEdittext;

    ListView outListView;
    MsgListAdapter msgListAdapter;
    ArrayList<JSONObject> msgList;

    private Client client;

    private ClientService.MyBinder ServerBinder;   //定义Binder引用
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            //重写方法来获取Binder对象
            ServerBinder = (ClientService.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(MainActivity.this, ClientService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mainActivity = MainActivity.this;
        bindService();

        outListView = findViewById(R.id.outListView);
        msgList = new ArrayList<>();

        msgListAdapter = new MsgListAdapter(MainActivity.this, msgList);
        outListView.setAdapter(msgListAdapter);

        idEdittext = findViewById(R.id.user_id);
        infoEdittext = findViewById(R.id.infoTextview);
        targetEdittext = findViewById(R.id.target_id);


        connectButton = findViewById(R.id.connect_button);
        connectButton.setPadding(1,1,1,1);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(connectButton.getText().equals("已连接") && client.isOpen()){
                    Toast.makeText(MainActivity.this, "长按代号以断开连接.", Toast.LENGTH_SHORT).show();
                }

                String user_id = idEdittext.getText().toString();
                if(user_id.length() >= 8) {

                    if(checkId(user_id)) {
                        ServerBinder.createClient(user_id);
                        ServerBinder.connect();
                        client = ServerBinder.getClient();
                        connectButton.setText("已连接");
                        idEdittext.setFocusable(false);
                    } else {
                        Toast.makeText(MainActivity.this, "这个id不可用！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                   if(user_id.length()==0)
                       Toast.makeText(MainActivity.this, "请输入你的id！", Toast.LENGTH_SHORT).show();
                   else
                       Toast.makeText(MainActivity.this, "id太短！", Toast.LENGTH_SHORT).show();
                }

            }
        });

        idEdittext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ServerBinder.close();
                connectButton.setText("连接");
                Toast.makeText(MainActivity.this, "已断开连接.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });



        /**发送消息**/
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client == null || client.isClosed() || !client.isOpen()){
                    Toast.makeText(MainActivity.this, "未连接！", Toast.LENGTH_SHORT).show();
                    return;
                }

                String msg = infoEdittext.getText().toString();
                String target = targetEdittext.getText().toString();
                if(target.length() < 1){
                    Toast.makeText(MainActivity.this, "请输入目标代号！", Toast.LENGTH_SHORT).show();
                }else if(msg.length() > 0 ) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cmd", "msg");
                    jsonObject.put("setter", client.id);
                    jsonObject.put("target", target);
                    jsonObject.put("msg", msg);

                    boolean r = client.sendMsg(jsonObject);

                    if(r){
                        //已发送
                        //outTextview.setText(outTextview.getText().toString() + "\n我: " + msg );
                        JSONObject msgToListView = new JSONObject();
                        msgToListView.put("setter","local");
                        msgToListView.put("msg", msg);

                        addMsgToListView(msgToListView);

                        infoEdittext.setText("");
                    }

                }else
                    Toast.makeText(MainActivity.this, "消息不能为空！", Toast.LENGTH_SHORT).show();
            }
        });

        try {

            RsaUtils.test();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void addMsgToListView(JSONObject msg){

        msgList.add(msg);

        /**更新**/
        msgListAdapter.notifyDataSetChanged();

        /**滚动到底部**/
        outListView.setSelection(msgListAdapter.getCount()-1);
    }

    /**检查id是否重复**/
    private boolean checkId(String id){
        String url = "http://43.139.218.18:35810/checkId";
        final String[] r = new String[1];
        r[0] = "";

        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        r[0] = HttpUtils.doPost(url, "id=" + id).replace("\n", "");
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        setMsgToHandler(404, "网络错误3");
                    }
                }
            }).start();
            countDownLatch.await(4000, TimeUnit.MILLISECONDS);

            if(r[0].equals("200"))
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            setMsgToHandler(404, "网络错误4");
            return false;
        }

        return false;
    }


    private void setMsgToHandler(int what, String msg){
        Message message = new Message();
        message.what = what;
        message.obj = msg;
        handler.sendMessage(message);
    }


    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case 1:
                    /**收到消息**/
                    JSONObject jsonObject = (JSONObject) JSON.parse((String) message.obj);
                    String setter = jsonObject.getString("setter");
                    String msg = jsonObject.getString("msg");
                    try {
                        msg = RsaUtils.decryptByPrivateKey(msg, RsaUtils.PRIVATE_KEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "解密失败，未知错误!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    //outTextview.setText(outTextview.getText().toString() + "\n" + setter + ": " + msg );

                    JSONObject msgToListView = new JSONObject();
                    msgToListView.put("setter", setter);
                    msgToListView.put("msg", msg);

                    addMsgToListView(msgToListView);

                    break;

                case 404:
                    Toast.makeText(MainActivity.this, message.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, message.obj + " ", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });





}