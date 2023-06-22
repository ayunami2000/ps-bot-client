package me.ayunami2000.psbot;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ProxSockServer extends WebSocketServer {
    public HashMap<WebSocket,String> wsList=new HashMap<>();

    public ProxSockServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if(wsList.containsKey(conn))wsList.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if(wsList.containsKey(conn)){
            conn.close();
        }else {
            wsList.put(conn, message);
        }
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }
}