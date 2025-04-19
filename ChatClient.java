import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private String username;
    private String currentChatUser = "Group Chat"; // Default to group chat
    private JLabel chatHeader;

    // Store messages separately for each user
    private Map<String, StringBuilder> chatHistoryMap = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient().createGUI());
    }

    private void createGUI() {
        JFrame frame = new JFrame("Chat App");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // === Left Panel (User List) ===
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellHeight(40);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(180, 0));
        frame.add(userScrollPane, BorderLayout.WEST);

        // Add "Group Chat" as default
        userListModel.addElement("Group Chat");
        chatHistoryMap.put("Group Chat", new StringBuilder());

        // === Top Panel (Chat Header) ===
        chatHeader = new JLabel(" Chat with Group");
        chatHeader.setFont(new Font("Arial", Font.BOLD, 18));
        chatHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(chatHeader, BorderLayout.NORTH);

        // === Center Panel (Chat Area) ===
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        frame.add(chatScrollPane, BorderLayout.CENTER);

        // === Bottom Panel (Input + Send) ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // === Action Listeners ===
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // === Switch chat on user selection ===
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentChatUser = userList.getSelectedValue();
                chatHeader.setText(" Chat with " + currentChatUser);
                showChat(currentChatUser);
            }
        });

        frame.setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Prompt for username
            String serverMessage = in.readLine();
            username = JOptionPane.showInputDialog(serverMessage);
            out.println(username);

            // Start message reader
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        // Check if private message
                        String sender;
                        String displayMessage;
                        if (message.contains(" (private): ")) {
                            sender = message.split(" \\(private\\): ")[0];
                            displayMessage = sender + " (private): " + message.split(" \\(private\\): ")[1];

                            chatHistoryMap.putIfAbsent(sender, new StringBuilder());
                            //chatHistoryMap.putIfAbsent(currentChatUser, new StringBuilder()); // Ensure current user has a chat history
                            chatHistoryMap.get(sender).append(displayMessage).append("\n");
                            //chatHistoryMap.get(currentChatUser).append(displayMessage).append("\n");

                            if (currentChatUser.equals(sender)) {
                                showChat(sender);
                            }
                            if (!userListModel.contains(sender)) {
                                userListModel.addElement(sender);
                            }

                        } else {
                            // Group message
                            sender = message.split(":")[0].trim();
                            chatHistoryMap.get("Group Chat").append(message).append("\n");

                            if (currentChatUser.equals("Group Chat")) {
                                showChat("Group Chat");
                            }

                            if (!sender.equals(username) && !userListModel.contains(sender)) {
                                userListModel.addElement(sender);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server.");
        }
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            if (!currentChatUser.equals("Group Chat")) {
                out.println("@" + currentChatUser + " " + msg); // Private message format
                String selfMsg = "Me (to " + currentChatUser + "): " + msg;
                chatHistoryMap.putIfAbsent(currentChatUser, new StringBuilder());
                chatHistoryMap.get(currentChatUser).append(selfMsg).append("\n");
                showChat(currentChatUser);
            } else {
                out.println(msg);
                String selfMsg = "Me: " + msg;
                chatHistoryMap.get("Group Chat").append(selfMsg).append("\n");
                showChat("Group Chat");
            }
            inputField.setText("");
        }
    }

    private void showChat(String user) {
        chatArea.setText(chatHistoryMap.getOrDefault(user, new StringBuilder()).toString());
    }
}

