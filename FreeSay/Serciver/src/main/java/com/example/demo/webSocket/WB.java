package com.example.demo.webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ServerEndpoint("/sayfree/{userId}")
@Component
public class WB{

    static Logger log = Logger.getLogger("WB");
    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/

    private static int onlineCount = 0;

    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    public static ConcurrentHashMap<String,WB> webSocketMap = new ConcurrentHashMap<>();



    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userId*/
    private String userId = "";
    /**储存每个客户的publickey**/
    public String publicKey = "";


    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;

        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            webSocketMap.put(userId,this);
            //加入set中
        }else{
            webSocketMap.put(userId,this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }

        log.info("join:"+userId+"   number:" + getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.warning("用户:" +userId+" ,网络异常!!!!!!");
        }
    }




    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            //从set中删除
            subOnlineCount();
        }
        log.info("exit:"+userId+"    user_number:" + getOnlineCount());
    }




    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {

        //System.out.println(userId + "    " + message);

        JSONObject jsonObject = (JSONObject) JSON.parse(message);

        String cmd = jsonObject.getString("cmd");
        if(cmd.equals("setPublicKey")) {
            publicKey = jsonObject.getString("msg");
            return;
        }
//        } else if(cmd.equals("getPublicKey")){
//            String targetId = jsonObject.getString("target");
//            if(webSocketMap.containsKey(targetId)){
//                JSONObject r = new JSONObject();
//                r.put("msg", webSocketMap.get(targetId).publicKey);
//                r.put("cmd","targetPublicKey");
//                try {
//                    sendMessage(r.toJSONString());
//                } catch (IOException exception) {
//                    log.warning(" line 107 : 网络异常");
//                    exception.printStackTrace();
//                }
//            } else {
//                JSONObject rj = new JSONObject();
//                rj.put("error", "404");
//                try {
//                    sendMessage(rj.toJSONString());
//                } catch (IOException exception) {
//                    log.warning(" line 107 : 网络异常");
//                    exception.printStackTrace();
//                }
//            }
//            return;
//        }


        String targetId = jsonObject.getString("target");
        if (!webSocketMap.containsKey(targetId)){
            JSONObject rj = new JSONObject();
            rj.put("error", "404");
            try {
                sendMessage(rj.toJSONString());
            } catch (IOException exception) {
                log.warning(" line 107 : 网络异常");
                exception.printStackTrace();
            }
        } else {
            try {
                webSocketMap.get(targetId).sendMessage(jsonObject.toJSONString());
            }catch (Exception e){
                log.warning(" line 114 : 网络异常");
                e.printStackTrace();
            }
        }

    }




    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.warning("error:"+ userId + "   "  +  error.getMessage());
        error.printStackTrace();

        onClose();
    }


    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        if(StringUtils.isNotBlank(userId)&&webSocketMap.containsKey(userId)){
            webSocketMap.get(userId).sendMessage(message);
        }else{
           //log.warning("用户"+userId+",不在线！");
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WB.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WB.onlineCount--;
    }



}
