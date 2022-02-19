package com.example.chatapp_v2.Server;

import com.example.chatapp_v2.Server.Exceptions.LoginException;
import com.example.chatapp_v2.Server.Exceptions.RegisterException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ConnectionServices {

    private final Socket socket;
    private Connection connection;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    public ConnectionServices(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        this.socket = socket;
        this.bufferedReader = bufferedReader;
        this.bufferedWriter = bufferedWriter;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/usersChatApp", "root", "21122000v");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Processes the login and register requests.
     * @return the username of the user that logged in /  null if something went wrong in the proccess.
     */
    public String receiveRequests() {
        while (socket.isConnected()) {
            try {
                String username = bufferedReader.readLine();
                if (username != null) {
                    if (username.startsWith("Register/"))
                        receiveRegisterRequest(username.replaceFirst("Register/", ""));
                    else {
                        boolean res = receiveLoginRequest(username.replaceFirst("Login/", ""));
                        if (res)
                            return username.replaceFirst("Login/", "");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        return null;
    }

    /**
     * Processes the register requests and sens the user messages if the registration has succeeded or failed.
     * @param username - String - the username of the client that wishes to register
     */
    public void receiveRegisterRequest(String username) {
        try {
            checkRegisterRequestUsername(username);
            System.out.println("Username " + username + " registered successfully.");
            sendConnectionMessage("Username registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (RegisterException e) {
            System.out.println(e.getMessage());
            sendConnectionMessage("Account already registered on this username.");
        }

    }

    /**
     * Processes the friend requests and sens the user messages if the adding of the friend has succeeded or failed.
     * Checks if the user exists in the database.
     */
    public void receiveFriendRequests() {
        try{
            String usernameFromClient = bufferedReader.readLine();

            //check if the user exists in the database
            checkLoginRequestUsername(usernameFromClient);
            if(usernameFromClient != null)
                sendConnectionMessage("User exists.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error receiving username from client.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (LoginException e) {
            System.out.println(e.getMessage());
            sendConnectionMessage("There is no account with this username.");
        }
    }

    public boolean receiveLoginRequest(String username) {
        try {
            checkLoginRequestUsername(username);
            System.out.println("Username " + username + " logged in successfully.");
            sendConnectionMessage("Logged in successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
            return false;
        } catch (LoginException e) {
            System.out.println(e.getMessage());
            sendConnectionMessage("There is no account with this username.");
            return false;
        }
        return true; //login has been successful
    }


    /**
     * Method that checks if a Username is in the database, so at user can log in
     * @param username - String - username of the client,
     * @throws LoginException - exception thrown when the provided username is too long
     * @throws SQLException
     */
    private void checkLoginRequestUsername(String username) throws LoginException, SQLException {
        if (username != null) {
            if (username.length() >= 256)
                throw new LoginException("Username too long.\n");

            //check if the username is in database
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Username FROM Users;");

            boolean found = false;
            while (resultSet.next()) {
                if (username.equals(resultSet.getString("Username"))) {
                    found = true;
                    break;
                }
            }

            if (!found)
                throw new LoginException("There is no account with this username.\n");

        }
    }


    /**
     * Method that check if a username can be registered or not
     * @param username - String - the username of the client that wishes to register.
     * @throws RegisterException
     * @throws SQLException
     */
    private void checkRegisterRequestUsername(String username) throws RegisterException, SQLException {
        if (username != null) {
            if (username.length() >= 256)
                throw new RegisterException("Username too long.\n");

            //check if the username is in database
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Username FROM Users;");

            while (resultSet.next()) {
                if (username.equals(resultSet.getString("Username")))
                    throw new RegisterException("Username already used.\n");
            }

            //otherwise, add it to the database
            statement.executeUpdate("INSERT INTO Users(Username) VALUES ('" + username + "');");
        }
    }

    private void sendConnectionMessage(String messageToSend) {
        try {
            bufferedWriter.write(messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
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

}
