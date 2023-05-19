package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.webSocket.WB;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;


@RestController
public class Controller {

    public Controller(){
    }

    @GetMapping("test")
    public String test1(){
        return "hello World";
    }

    @PostMapping("getPublicKey")
    public String setPublicKey(@PathParam("id") String id, @PathParam("targetId") String targetId){

        System.out.println(" getPublicKey id:" + id + "   targetId:" + targetId);

        JSONObject jsonObject = new JSONObject();
        if(WB.webSocketMap.containsKey(id) && WB.webSocketMap.containsKey(targetId)){
            jsonObject.put("msg", "200");
            jsonObject.put("publickey", WB.webSocketMap.get(targetId).publicKey);
            jsonObject.put("target", targetId);
        } else {
            jsonObject.put("msg", "404");
        }

        return jsonObject.toJSONString();
    }



    @PostMapping("targetAlive")
    public String targetAlive(@PathParam("id") String id, @PathParam("targetId") String targetId){

        System.out.println(" targetAlive id:" + id + "   targetId:" + targetId);

        if(WB.webSocketMap.containsKey(id) && WB.webSocketMap.containsKey(targetId)){
            return "200";
        } else {
            return "404";
        }
    }


    @PostMapping("checkId")
    public String checkId(@PathParam("id") String id){

        System.out.println(" checkId id:" + id );

        if(!WB.webSocketMap.containsKey(id)){
            return "200";
        }
        return "886";
    }

}
