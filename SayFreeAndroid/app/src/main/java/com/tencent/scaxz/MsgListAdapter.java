package com.tencent.scaxz;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;


import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


class MsgListAdapter extends BaseAdapter {

   private Context context;

   private ArrayList<JSONObject> msgList;

   public MsgListAdapter(Context context,  ArrayList<JSONObject> msgList){
      this.context = context;
      this.msgList = msgList;
   }

   @Override
   public int getCount() {
      return msgList.size();
   }

   @Override
   public Object getItem(int i) {
      return msgList.get(i);
   }

   @Override
   public long getItemId(int i) {
      return i;
   }

   @SuppressLint("ViewHolder")
   @Override
   public View getView(int i, View view, ViewGroup viewGroup) {
      LayoutInflater inflater = LayoutInflater.from(context);
      view = inflater.inflate(R.layout.msg_listview_entry, viewGroup, false);

      TextView msgTextView = view.findViewById(R.id.msg_textview_entry);

      JSONObject jsonObject = msgList.get(i);

      String setter = jsonObject.getString("setter");
      String msg =jsonObject.getString("msg");
      if(setter.equals("local")) {
         LinearLayout MsgLinearLayout = view.findViewById(R.id.msg_listview_entry_msgLayout);
         MsgLinearLayout.setGravity(Gravity.RIGHT);

         LinearLayout msgLayou_Bg = view.findViewById(R.id.msg_listview_entry_msgLayout_bg);
         msgLayou_Bg.setBackgroundResource(R.drawable.cricle_yuanjiao);
         msgTextView.setTextColor(Color.WHITE);

         LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
         LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
         LinearLayout linearLayout_left = view.findViewById(R.id.msg_listview_entry_padding_left);
         LinearLayout linearLayout_right = view.findViewById(R.id.msg_listview_entry_padding_right);
         linearLayout_left.setLayoutParams(param1);
         linearLayout_right.setLayoutParams(param2);
      } else {
         TextView setterTextview = view.findViewById(R.id.msg_Setter_textview_entry);
         setterTextview.setText(setter+":");
      }

      msgTextView.setText(msg);
//      System.out.println("s---------------------------------------------------------------msg=------------------------  " + jsonObject.toJSONString());
//      System.out.println("s---------------------------------------------------------------msg=------------------------  " + msg);

      return view;
   }


}
