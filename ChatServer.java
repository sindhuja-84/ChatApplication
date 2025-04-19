import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();
    private static Map<String, String> clientNames = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // First, get the username of the client
                out.println("Enter your username:");
                username = in.readLine();
                synchronized (clientWriters) {
                    clientWriters.put(username, out);
                    clientNames.put(username, username);
                }

                out.println("Welcome " + username + "! You can start chatting.");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        // Private message: @username message
                        String[] parts = message.split(" ", 2);
                        if (parts.length > 1) {
                            String recipient = parts[0].substring(1);  // Remove '@' symbol
                            String privateMessage = parts[1];

                            // Send the private message to the specified recipient
                            PrintWriter recipientOut = clientWriters.get(recipient);
                            if (recipientOut != null) {
                                recipientOut.println(username + " (private): " + privateMessage);
                            } else {
                                out.println("User " + recipient + " not found.");
                            }
                        }
                    } else {
                        // Broadcast message to all clients (group chat)
                        for (PrintWriter writer : clientWriters.values()) {
                            writer.println(username + ": " + message);
                        }
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(username);
                    clientNames.remove(username);
                }
            }
        }
    }
}
