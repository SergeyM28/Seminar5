package org.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable{
    private final Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String name;

    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " connected to chat");
            broadcastMessage("Server: " + name + " connected to chat-chat");
        }
        catch (IOException e){
            closeEverythimg(socket, bufferedReader, bufferedWriter);
        }
    }

    private void closeEverythimg(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClient();
        try {
            if (bufferedWriter != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private  void removeClient(){
        clients.remove(this);
        System.out.println(name + " left chat");
        broadcastMessage("Server: " + name + " left chat-chat");
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }
            catch (IOException e){
                closeEverythimg(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private  void broadcastMessage(String message){
        for (ClientManager client : clients){
            try {
                if (!client.name.equals(name)) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
            catch (IOException e){
                closeEverythimg(socket, bufferedReader, bufferedWriter);
            }
        }
    }
}
