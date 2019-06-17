package wifidirect.wifidirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SendReceive extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SendReceive(Socket skt) {
        socket = skt;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (socket != null) {
            try {
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    MainActivity.handler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Write(final byte[] bytes) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
