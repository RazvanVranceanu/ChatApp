package com.example.chatapp_v2.Server;

import java.io.*;
import java.net.Socket;

public class ChatServices {

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    public ChatServices(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        this.socket = socket;
        this.bufferedReader = bufferedReader;
        this.bufferedWriter = bufferedWriter;
    }

    /**
     * Writes a messages in the socket buffer
     * @param msg - String - the message that is wished to sent
     * @throws IOException
     */
    public void writeInBuffer(String msg) throws IOException {
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    /**
     * Sends messages from a user to another user
     */
    public void sendUnreadMessages(){
        try {
            String sender = bufferedReader.readLine();
            String recipient = bufferedReader.readLine();

            /*
                create a file where you temporarily store the messages from a user
                to another that couldn't be sent (offline messaging).
             */
            String path = sender + "/" + "@" + recipient + ".txt";
            File myFile = new File(path);

            myFile.getParentFile().mkdirs();
            myFile.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(myFile));

            // #102AllDOne = that there are no unread messages to send.
            //these two tags are sent in order to let the Client know that there are/aren't messages to load
            if(myFile.length() == 0) {
                System.out.println("#102AllDone/");
                writeInBuffer("#102AllDone/");
            }

            else{
                //else, write the signal "#101Sending/" and send messages until the file is empty.
                System.out.println("#101Sending/");
                writeInBuffer("#101Sending/");

                String line;
                while((line = br.readLine()) != null){
                    writeInBuffer(line);
                }
                br.close();
                new FileWriter(myFile, false).close();
                writeInBuffer("#102AllDone/");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error receiving the usernames for the chat");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message received from an user to another one.
     * Depending on if the recipient is online or not, a different way of sending the message will be chosen.
     */
    public void sendMessageToSomeone() {
        //get usernames and the message
        try {
            String sender = bufferedReader.readLine();
            String recipient = bufferedReader.readLine();
            String message = bufferedReader.readLine();

//            System.out.println("Sender: " + sender + ", Recipient: " + recipient + ", Msg: " + message);

            //check if the recipient is online
            boolean res = ClientHandler.checkOnlineUser(recipient);
            //if it is: send it
            if(res) {
//                ClientHandler.sendMessageTo(sender, recipient, message);
                ClientHandler c = ClientHandler.getClientByName(recipient);
                BufferedWriter bw = c.getBufferedWriter();
                bw.write(sender);
                bw.newLine();
                bw.flush();

                bw.write(message);
                bw.newLine();
                bw.flush();
            }else {
                //else: put in the offline messages file
                String path = recipient + "/" + "@" + sender + ".txt";
                File myFile = new File(path);

                myFile.getParentFile().mkdirs();
                myFile.createNewFile();

                BufferedWriter bw = new BufferedWriter(new FileWriter(myFile, true));
                bw.write(message);
                bw.newLine();
                bw.flush();
                bw.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Writes back to the client that there are no more incoming messages, and that the threads for that chat can die.
     */
    public void kill() {
        try {
            writeInBuffer("#EMPTYCHANNEL#");
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }
}
