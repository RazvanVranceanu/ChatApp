package com.example.chatapp_v2.Client;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String currentUsername = null;

    static boolean exit = false;
    private Thread t;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error while creating client.");
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public Thread.State getStateThread(){
        return t.getState();
    }

    public String getCurrentUsername(){
        return this.currentUsername;
    }

    /**
     * Sends to the server a username that should be added in the friends list. This method is called by the controller
     * in order to send friend requests to the server.
     * @param usernameToSend - String - the username that is wished to be added as a friend
     */
    public void sendAddFriendUsername(String usernameToSend){
        try {
            bufferWriteString(usernameToSend);
        } catch (IOException e) {
            System.out.println("Error while sending the friend username");
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    //The string received must be of for "Login/username" or "Register/username"
    //This way the server knows how to process the message sent
    public void sendRequestUsername(String usernameToSend) {
        try{
            if(usernameToSend.startsWith("Login/"))
                currentUsername = usernameToSend.replaceFirst("Login/", "");
            System.out.println(currentUsername);
            bufferWriteString(usernameToSend);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error sending message to the server.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public String receiveRequestMessage() {
        String message = null;
        try {
            message = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        return message;
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader != null)
                bufferedReader.close();
            if(bufferedWriter != null)
                bufferedWriter.close();
            if(socket != null)
                socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Gets the unread messages from a certain user from the server.
     * @param friendUsername - String - the user from which the requester expects messages
     * @return - List<String> with the respective messages.
     */
    public List<String> getUnreadMessagesWith(String friendUsername){
        List<String> unreadMessages = new ArrayList<>();
        String serverMsg = null;

        try{
//            System.out.println("Iau mesajele necitite");
            bufferWriteString(currentUsername);
            bufferWriteString(friendUsername);

            serverMsg = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(Objects.equals(serverMsg, "#101Sending/")){
            try{
                String msg = bufferedReader.readLine();
                if(Objects.equals(msg, "#102AllDone/")){
                    break;
                }
                unreadMessages.add(msg);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while receiving unread messages.");
            }
        }
        return unreadMessages;
    }

    /**
     * Sends a message to the server. Used in sending the server the codes that precede other actions.
     * @param s - String - the code that has to be sent
     */
    public void sendCode(String s) {
        try {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Method that writes in the bufferedWriter a string that has to be sent to the server.
     * @param s - String - the string that is sent to the server.
     * @throws IOException - from writing in the buffer
     */
    public void bufferWriteString(String s) throws IOException {
        bufferedWriter.write(s);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void sendMessageToServer(String messageToSend, String friendsUsername) {
        try {
            //send the sender username
            bufferWriteString(currentUsername);
            //send the recipient username
            bufferWriteString(friendsUsername);
            //send the message
            bufferWriteString(messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessageFrom(VBox vBox){
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(socket.isConnected() && !exit){
                    try{
                        String sender = bufferedReader.readLine();
                        if(Objects.equals(sender, "#EMPTYCHANNEL#"))
                            break;
                        String message = bufferedReader.readLine();
                        ChatController.addLabel(message, vBox);
                        saveMessageInChat(message, sender);
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        });
        t.start();
    }


    public void saveMessageInChat(String messageToSend, String friendsUsername) {
        try {
            String path = currentUsername + "/" + "To" + friendsUsername + "Chat.txt";
            File myFile = new File(path);
            myFile.getParentFile().mkdirs();
            myFile.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(myFile, true));
            bw.write(messageToSend);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


}