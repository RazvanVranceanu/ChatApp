package com.example.chatapp_v2;

import com.example.chatapp_v2.Server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class MainServer {


    public static void main(String[] args) {
        try{
            Server server = new Server(new ServerSocket(1234));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating server.");
        }
    }
}
