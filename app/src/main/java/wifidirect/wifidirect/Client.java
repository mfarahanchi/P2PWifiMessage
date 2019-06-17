package wifidirect.wifidirect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {
    Socket socket;
    String HostAddress;

    public Client(InetAddress HostAddr) {
        HostAddress = HostAddr.getHostAddress();
        socket = new Socket();
    }

    @Override
    public void run() {

        try {
            socket.connect(new InetSocketAddress(HostAddress, 8888),500);
            MainActivity.sendReceive = new SendReceive(socket);
            MainActivity.sendReceive.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
