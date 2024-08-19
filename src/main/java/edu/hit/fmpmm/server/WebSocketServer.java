package edu.hit.fmpmm.server;

import jakarta.annotation.PreDestroy;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.ReentrantLock;

@ServerEndpoint("/websocket/{userId}")
@Component
public class WebSocketServer {
//    private final ReentrantLock lock = new ReentrantLock();
    private static int onlineCount = 0;

    private static final ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        this.session = session;
        webSocketMap.put(userId, this);
        onlineCount++;
        System.out.println("有客户端（" + userId + "）连接");
    }

    @OnClose
    public void onClose() {
        webSocketMap.remove(userId);
        onlineCount--;
        System.out.println("有客户端（" + userId + "）断开");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @PreDestroy
    public void onDestroy() {  // 没有用
        System.out.println("停止WebSocket...");
        for (WebSocketServer webSocketServer : webSocketMap.values()) {
            closeSessionSilently(webSocketServer.session);
        }
    }

    private synchronized void closeSessionSilently(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                System.err.println("Failed to close WebSocket session: " + e.getMessage());
            }
        }
    }

    // 服务器主动推送到所有用户
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    // 服务器主动推送到指定用户
    public static void sendMessage(String message, String userId) throws IOException {
        if (!message.isEmpty() && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        } else {
            System.out.println("用户 " + userId + "不在线");
        }
    }
}
