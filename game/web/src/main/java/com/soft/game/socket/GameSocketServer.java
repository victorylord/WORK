/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.socket;



import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.alibaba.fastjson.JSON;
import com.soft.game.model.BetRequest;
import com.soft.game.model.BetStakEeffectRequest;
import com.soft.game.service.BetService;
import com.soft.game.socket.message.MessageOperateType;
import com.soft.game.socket.message.OperateMessage;
import com.soft.game.socket.message.SocketMessageOpType;
import com.soft.game.utils.SpringContextHolder;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/websocket")
public class GameSocketServer {

    @OnOpen
    public void onOpen(Session session) {
    }

    /**
     * 客户端关闭
     *
     * @param session session
     */
    @OnClose
    public void onClose(Session session) {

    }

    /**
     * 发生错误
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误：" + throwable.getMessage(), session.getId());
    }

    /**
     * 收到客户端发来消息
     *
     * @param message 消息对象
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        /** 前端发送空消息, do nothing */
        if (StringUtils.isBlank(message)) {
            return;
        }
        /** ping <->  pong(heartbeat) */
        OperateMessage opMsg = JSON.parseObject(message, OperateMessage.class);
        if (SocketMessageOpType.PING.getCode().equals(opMsg.getOpType())) {
            try {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(JSON.toJSONString(
                            new MessageOperateType(SocketMessageOpType.PONG.getCode())));
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return;
        }
        if (SocketMessageOpType.CHANGEBETMONEY.getCode().equals(opMsg.getOpType())) {
            try {
                if (session.isOpen()) {
                    BetService betService = SpringContextHolder.getBean(BetService.class);
                    session.getAsyncRemote().sendText(JSON.toJSONString(betService.bet(
                            JSON.parseObject(opMsg.getMessage().toString(), BetRequest.class))));
                }
            } catch (Exception e) {
                // ignore
            }
            return;
        }
        if (SocketMessageOpType.BET.getCode().equals(opMsg.getOpType())) {
            try {
                if (session.isOpen()) {
                    BetService betService = SpringContextHolder.getBean(BetService.class);
                    BetRequest betRequest = JSON.parseObject(opMsg.getMessage().toString(), BetRequest.class);
                    session.getAsyncRemote().sendText(JSON.toJSONString(
                            betService.bet(betRequest)));
                }
            } catch (Exception e) {
                log.error("投注失败，错误：{}", e.getMessage());
            }
            return;
        }
        if (SocketMessageOpType.BETSTAKEEFFECT.getCode().equals(opMsg.getOpType())) {
            try {
                if (session.isOpen()) {
                    BetService betService = SpringContextHolder.getBean(BetService.class);
                    session.getAsyncRemote().sendText(JSON.toJSONString(betService.betStakEeffect(
                            JSON.parseObject(opMsg.getMessage().toString(), BetStakEeffectRequest.class))));
                }
            } catch (Exception e) {
                // ignore
            }
            return;
        }
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
