package com.tencent.scaxz;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;


import androidx.annotation.Nullable;

public class ClientService extends Service {

   public String user_id;

   public Client client;



   public MyBinder myBinder = new MyBinder();
   class MyBinder extends Binder {
      //自定义继承Binder类
      public int Add(int a,int b)
      {
         return a+b;
      }

      public void createClient(String id){
         user_id = id;
         client = new Client("ws://43.139.218.18:35810/sayfree/" + user_id);
         client.id = id;
      }

      public void connect(){
         client.connect();
      }

      public void close(){
         client.close();
      }

      public Client getClient(){ return client; }
      public String getUserId(){ return user_id; }
   }


   @Override
   public void onCreate() {
      super.onCreate();
      Log.e("Tag"," ---------------------------------Server onCreate()------------------------------------------------");
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return myBinder;
   }
}
