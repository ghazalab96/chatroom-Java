package at.ac.hcw.chat.server;

import at.ac.hcw.chat.common.Protocol;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    private final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public boolean add(String username, PrintWriter writer) {
        if (hasUser(username)) return false;
        clients.put(username, writer);
        return true;
    }

    public void remove(String username) {
        clients.remove(username);
    }

    public boolean hasUser(String username) {
        return clients.containsKey(username);
    }

    public void sendTo(String username, String message) {
        PrintWriter writer = clients.get(username);
        if (writer != null) writer.println(message);
    }

    public void broadcast(String message) {
        clients.values().forEach(writer -> writer.println(message));
    }

    public void broadcastUserList() {
        List<String> usernames = clients.keySet().stream().sorted().toList();
        broadcast(Protocol.formatUserList(usernames));
    }
}
