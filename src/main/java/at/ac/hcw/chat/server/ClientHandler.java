package at.ac.hcw.chat.server;

import at.ac.hcw.chat.common.Protocol;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final ClientManager clients;

    private PrintWriter out;
    private BufferedReader in;

    private String username;
    private boolean loggedIn = false;

    public ClientHandler(Socket socket, ClientManager clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            login();
            listenForMessages();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            logout();
        }
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private void login() throws IOException {
        username = in.readLine();

        if (username == null || username.isBlank()) {
            sendSystem("Username cannot be empty.");
            throw new IOException("Empty username.");
        }

        username = username.trim();

        if (!clients.add(username, out)) {
            sendSystem("Username '" + username + "' is already taken.");
            throw new IOException("Duplicate username: " + username);
        }

        loggedIn = true;
        clients.broadcastUserList();
        clients.broadcast(Protocol.formatMessage("[System]", username + " joined the room."));
        System.out.println("[JOIN] " + username);
    }

    private void listenForMessages() throws IOException {
        String line;

        while ((line = in.readLine()) != null) {
            if (Protocol.isPrivateCommand(line)) {
                handlePrivateMessage(line);
            } else {
                clients.broadcast(Protocol.formatMessage(username, line));
                System.out.println("[MSG] " + username + ": " + line);
            }
        }
    }

    private void handlePrivateMessage(String raw) {
        int colon = raw.indexOf(":");
        String target = raw.substring(1, colon).trim();
        String text = raw.substring(colon + 1).trim();

        if (target.isBlank() || text.isBlank()) {
            sendSystem("Invalid private message format. Use @username:message");
            return;
        }

        if (!clients.hasUser(target)) {
            sendSystem("User '" + target + "' is offline.");
            return;
        }

        clients.sendTo(target, Protocol.formatMessage("[Private from " + username + "]", text));
        out.println(Protocol.formatMessage("[Private to " + target + "]", text));
    }

    private void logout() {
        if (loggedIn) {
            clients.remove(username);
            clients.broadcastUserList();
            clients.broadcast(Protocol.formatMessage("[System]", username + " left the room."));
            System.out.println("[LEFT] " + username);
        }

        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    private void sendSystem(String text) {
        out.println(Protocol.formatMessage("[System]", text));
    }
}
