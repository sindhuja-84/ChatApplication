import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clients;
    private JTextArea logArea;

    public ClientHandler(Socket socket, Set<ClientHandler> clients, JTextArea logArea) {
        this.socket = socket;
        this.clients = clients;
        this.logArea = logArea;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            log("Error setting up streams: " + e.getMessage());
        }
    }

    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                log("Received: " + message);
                broadcast(message);
            }
        } catch (IOException e) {
            log("Client disconnected: " + socket.getInetAddress());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            clients.remove(this);
        }
    }

    void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }

    void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }
}
