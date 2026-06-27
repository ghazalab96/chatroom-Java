package at.ac.hcw.chat.client.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public void connect(String host, int port, String username) throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(username.trim());
    }

    public void send(String message) {
        if (writer != null && !message.isBlank()) {
            writer.println(message.trim());
        }
    }

    public void listen(Consumer<String> onMessage, Consumer<String> onError) {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (IOException e) {
                onError.accept("Connection closed.");
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
