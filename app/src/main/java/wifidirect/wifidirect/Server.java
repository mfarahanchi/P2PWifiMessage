package wifidirect.wifidirect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    Socket socket;
    ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
            MainActivity.sendReceive = new SendReceive(socket);
            MainActivity.sendReceive.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
