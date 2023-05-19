package com.tencent.scaxz;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HttpUtils {
   public static void doPost_okhttp() throws Exception{
      OkHttpClient client = new OkHttpClient();

      Request request = new Request.Builder()
              .url("http://127.0.0.1:35810/getPublicKey")
              .build();//创建Request 对象

      Response response = null;

      response = client.newCall(request).execute();

      if (response.isSuccessful()) {

         Log.d("tag","response.code() == " + response.code());

         Log.d("tag","response.message()==" + response.message());

         Log.d("tag","res==" + response.body().string());

         //TODO 此时在子线程，更新UI的操作需使用handler跳转到主线程

      }

   }


   public static String doPost(String url_str,String data) throws Exception{
      String r = "";
      URL url = new URL(url_str);
      //打开和url之间的连接
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      PrintWriter out = null;

      /**设置URLConnection的参数和普通的请求属性****start***/


      conn.setConnectTimeout(3000);

      /**设置URLConnection的参数和普通的请求属性****end***/
      //设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
      //最常用的Http请求无非是get和post，get请求可以获取静态页面，也可以把参数放在URL字串后面，传递给servlet，
      //post与get的 不同之处在于post的参数不是放在URL字串里面，而是放在http请求的正文内。
      conn.setDoOutput(true);
      conn.setDoInput(true);

      conn.setRequestMethod("POST");//GET和POST必须全大写
      /**GET方法请求*****start*/
      /**
       * 如果只是发送GET方式请求，使用connet方法建立和远程资源之间的实际连接即可；
       * 如果发送POST方式的请求，需要获取URLConnection实例对应的输出流来发送请求参数。
       * */
      conn.connect();


      /**GET方法请求*****end*/

      /***POST方法请求****start*/

      out = new PrintWriter(conn.getOutputStream());//获取URLConnection对象对应的输出流

      out.print(data);//发送请求参数即数据

      out.flush();//缓冲数据

      /**POST方法请求****end*/

      //获取URLConnection对象对应的输入流
      InputStream is = conn.getInputStream();
      //构造一个字符流缓存
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String str = "";
      while ((str = br.readLine()) != null) {
         str = new String(str.getBytes(), StandardCharsets.UTF_8);//解决中文乱码问题
         //System.out.println(str);
         r += str + "\n";
      }
      //关闭流
      is.close();
      //断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
      //固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些。
      conn.disconnect();
      //System.out.println("完整结束");

      return r;

   }
}
