# 🗨️ Java Chat Application

A simple real-time chat application built using **Java Socket Programming** and **Swing GUI**. It supports both **group chat** and **private messaging** between multiple users.

---

## 📌 Features

- ✅ Real-time messaging between multiple clients
- ✅ Group chat functionality
- ✅ Private 1-to-1 messaging using `@username` command
- ✅ User-friendly GUI built with Java Swing
- ✅ Server handles multiple clients with multithreading

---

## 🏗️ Project Structure

<b>Chatpplication</b><br>
├── ChatServer.java // The main server that handles incoming connections<br>
├── ChatClient.java // Swing-based GUI client for chatting<br>
├── ClientHandler.java // Server-side thread for handling individual clients<br>

---

## 🚀 How to Run

### 1. Start the Server

```bash
javac ChatServer.java
java ChatServer

```

### 2. Start Clients(in separate terminals or multiple times)

```bash
javac ChatClient.java
java ChatClient

```

Each client will be prompted to enter a username.

## 💬 Usage

Group Chat: Just type and send a message

Private Chat: Type @username Your message to send privately

Switch Users: Click usernames on the left panel to view private chat history

## 📚 Technologies Used

Java (JDK 8+)

Swing (for GUI)

Sockets (java.net)

Multithreading
