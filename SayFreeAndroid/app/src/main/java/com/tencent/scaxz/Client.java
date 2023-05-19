package com.tencent.scaxz;

import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Client extends WebSocketClient {

   public final static String SEND_PUBLIC_KEY_URL = "http://43.139.218.18:35810/getPublicKey";
   public final static String TARGET_ALIVE_URL = "http://43.139.218.18:35810/targetAlive";

   public String id;

   public HashMap<String, String> targetPublicKeyMap;

   public Client(URI serverUri) {
      super(serverUri, new Draft_6455());
      targetPublicKeyMap = new HashMap<>();
      try {
         RsaUtils.createKeys();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   public Client(String url){
      super(URI.create(url), new Draft_6455());
      targetPublicKeyMap = new HashMap<>();
      try {
         RsaUtils.createKeys();
      } catch (Exception e) {
         e.printStackTrace();
      }
      sendMsgToMainActivity(770,"已连接");
   }

   @Override
   public void onOpen(ServerHandshake handshakedata) {
      Log.e("JWebSocketClient", "onOpen()");
   }

   @Override
   public void onMessage(String message) {
      Log.e("JWebSocketClient", "onMessage()  " + message);
      JSONObject jsonObject = (JSONObject) JSON.parse(message);
      String cmd = jsonObject.getString("cmd");
      String setter = jsonObject.getString("setter");
      String msg = jsonObject.getString("msg");

      switch (cmd){
         case "msg":
            sendMsgToMainActivity(1, message);
          break;

         default: break;
      }
   }



   public boolean sendMsg(JSONObject jsonObject){
      String cmd = jsonObject.getString("cmd");
      String setter = jsonObject.getString("setter");
      String msg = jsonObject.getString("msg");
      String target = jsonObject.getString("target");

      /**发信息给target**/
      String postData = "id=" + id + "&targetId=" + target;
      final String[] targetIsAlive = new String[1];
      targetIsAlive[0] = "";
      try {
         CountDownLatch countDownLatch = new CountDownLatch(1);
         new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  targetIsAlive[0] = HttpUtils.doPost(TARGET_ALIVE_URL, postData).replace("\n", "");
                  countDownLatch.countDown();
               } catch (Exception e) {
                  e.printStackTrace();
                  sendMsgToMainActivity(404, "网络错误0！");
               }
            }
         }).start();
         countDownLatch.await(4000, TimeUnit.MILLISECONDS);

      } catch (Exception e) {
         e.printStackTrace();
         sendMsgToMainActivity(404, "网络错误1！");
         return false;
      }

      if(!targetIsAlive[0].equals("200")){
         sendMsgToMainActivity(404, "目标不在线！");
         return false;
      }

      /**本地有目标的publickey，就直接加密发送**/
      if(targetPublicKeyMap.containsKey(target)){
         try {
            msg = RsaUtils.encrypt(msg, targetPublicKeyMap.get(target));
            jsonObject.put("msg", msg);
            send(jsonObject.toJSONString());//发送
            return true;
         } catch (Exception e) {
            e.printStackTrace();
            sendMsgToMainActivity(404, "可能是网络错误！请重试！");
            return false;
         }

      } else {
         /**本地无目标的publickey，请求获取目标publickey**/
         final String[] r = {""};
         try {
            /**请求获取目标publickey**/
            CountDownLatch countDownLatch = new CountDownLatch(1);
            new Thread(new Runnable() {
               @Override
               public void run() {
                  try {
                     r[0] =  HttpUtils.doPost(SEND_PUBLIC_KEY_URL, postData);
                     countDownLatch.countDown();
                  } catch (Exception e) {
                     e.printStackTrace();
                     sendMsgToMainActivity(404, "网络错误2！");
                  }
               }
            }).start();
            countDownLatch.await(4000, TimeUnit.MILLISECONDS);
//            r =  HttpUtils.doPost(SEND_PUBLIC_KEY_URL, postData);
            JSONObject jsonObject1 = (JSONObject) JSON.parse(r[0]);
            String msg_r = jsonObject1.getString("msg");

            if (msg_r.equals("200")){
               targetPublicKeyMap.put(jsonObject1.getString("target"), jsonObject1.getString("publickey"));
               msg = RsaUtils.encrypt(msg, jsonObject1.getString("publickey"));
               jsonObject.put("msg", msg);

               send(jsonObject.toJSONString());//发送
               return true;
            } else {
               sendMsgToMainActivity(404, "目标不在线或网络错误！请重试！");
               return false;
            }
         } catch (Exception e) {
            e.printStackTrace();
            sendMsgToMainActivity(404, "未知错误！请重试！");
            return false;
         }
      }


   }

   @Override
   public void onClose(int code, String reason, boolean remote) {
      Log.e("JWebSocketClient", "onClose()");
   }

   @Override
   public void onError(Exception ex) {
      Log.e("JWebSocketClient", "onError()");
   }

   @Override
   public void connect() {
      RsaUtils.createKeys();
      if(RsaUtils.PUBLIC_KEY != null) {
         super.connect();

         new Thread(new Runnable() {
            @Override
            public void run() {
               while (true){
                  try {
                     Thread.sleep(100);
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
                  if(isOpen()){
                     sendPublicKeyToServer(RsaUtils.PUBLIC_KEY);
                     break;
                  }
               }
            }
         }).start();

      }
   }

   /**上传publickey到服务器**/
   private void sendPublicKeyToServer(String key){
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("cmd", "setPublicKey");
      jsonObject.put("msg", key);
      send(jsonObject.toJSONString());
   }


   private void sendMsgToMainActivity(int what, String json_msg){
      Message message = new Message();
      message.what = what;
      message.obj = json_msg;
      MainActivity.mainActivity.handler.sendMessage(message);
   }

}