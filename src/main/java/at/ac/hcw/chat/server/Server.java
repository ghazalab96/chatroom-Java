package at.ac.hcw.chat.server;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;

public class Server {

    private static final ClientManager clients = new ClientManager();

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        int port = askForPort();
        String ip = detectIP();

        if (ip == null) {
            System.err.println("No active network connection found.");
            return;
        }

        printStartupInfo(ip, port);
        startListening(port);
    }

    private static void startListening(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for clients...\n");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, clients).start();
            }
        } catch (IOException e) {
            System.err.println("Server stopped: " + e.getMessage());
        }
    }

    private static int askForPort() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter port number (e.g. 888): ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default port 888.");
            return 888;
        }
    }

    private static String detectIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback() || network.isVirtual()) continue;

                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) return address.getHostAddress();
                }
            }
        } catch (SocketException e) {
            System.err.println("Could not detect IP: " + e.getMessage());
        }
        return null;
    }

    private static void printStartupInfo(String ip, int port) {
        System.out.println();
        System.out.println("=== SERVER ONLINE ===");
        System.out.println("IP   : " + ip);
        System.out.println("PORT : " + port);
        System.out.println("=====================");
        System.out.println();
    }
}
