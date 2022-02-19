package com.example.chatapp_v2.Server;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable{

    //list of online users.
    public static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<ClientHandler>());

    private ConnectionServices services;
    private ChatServices chatServices;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.services = new ConnectionServices(this.socket, bufferedReader, bufferedWriter);
            this.chatServices = new ChatServices(this.socket, bufferedReader, bufferedWriter);

            clientHandlers.add(this);

        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Method to handle each request form the client that the thread is serving.
     * A different action will be performed according to the request given:
     * "#001FriendRequest#" - process a fired request.
     * "#002requestUnread#" - process a registration
     * "#003sendMessage" - sends a message to another user in the database
     * "#007KILL" - tell the client that there are no more incoming messages.
     */
    @Override
    public void run() {

        clientUsername = services.receiveRequests();

        while(socket.isConnected()){
            String code = "";
            try {
                code = bufferedReader.readLine();
                System.out.println(code);
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
            if(code != null)
                switch (code) {
                    case "#001FriendRequest#" -> services.receiveFriendRequests();
                    case "#002requestUnread#" -> chatServices.sendUnreadMessages();
                    case "#003sendMessage#" -> chatServices.sendMessageToSomeone();
                    case "#007KILL#" -> chatServices.kill();
                }
            else {
                closeEverything(socket, bufferedReader, bufferedWriter);
            };
        }
    }


    /**
     * Closes the socket and the buffers
     * @param socket - obj of type Socket
     * @param bufferedReader - obj of type BufferedReader
     * @param bufferedWriter - obj of type BufferedWriter.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if( bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * removes a specific client handler form the list of active users.
     */
    private void removeClientHandler() {
        clientHandlers.remove(this);
    }

    /**
     * Searches in the list of online users by a given username.
     * @param name - String - the username of the person looked for
     * @return True if found. False if not.
     */
    public static boolean checkOnlineUser(String name){
        for(ClientHandler c : clientHandlers)
            if(Objects.equals(c.clientUsername, name))
                return true;
        return false;
    }


    /**
     * Searches in the list of online clients the object with a specific name.
     * @param name - String - by which to search a client
     * @return obj of type ClientHandler with the name equal of the string given.
     */
    public static ClientHandler getClientByName(String name){
        ClientHandler client = null;
        for(ClientHandler c : clientHandlers)
            if(Objects.equals(c.clientUsername, name))
                client = c;
        return client;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }
}
