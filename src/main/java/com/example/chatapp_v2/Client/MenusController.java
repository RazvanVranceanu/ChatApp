package com.example.chatapp_v2.Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MenusController implements Initializable {

    private Client client;
    private String friendsFilePath;
    private String chatsFilePath;

    @FXML
    private Button add_chat;
    @FXML
    private Button add_friend;
    @FXML
    private TextField search_textField;
    @FXML
    private ListView<String> friend_list;
    @FXML
    private ListView<String> chat_list;


    /**
     * Constructor for this object.
     * @param client - of type Client
     */
    public MenusController(Client client) {
        this.client = client;
        this.friendsFilePath = client.getCurrentUsername() + "Friends.txt";
        this.chatsFilePath = client.getCurrentUsername() + "Chats.txt";

        File myFile = new File(friendsFilePath);
        File myChatsFile = new File(chatsFilePath);

        try {
            myFile.createNewFile();
            myChatsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        add_friend.setOnAction(actionEvent -> onAddFriendButtonClicked());
        add_chat.setOnAction(actionEvent -> onAddChatButtonClicked());

        friend_list.setItems(getStringList(this.friendsFilePath));
        chat_list.setItems(getStringList(this.chatsFilePath));

        //set when double-clicking to open a chat
        chat_list.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    String itemSelected = chat_list.getSelectionModel().getSelectedItem();
                    enterChat(itemSelected);
                }
            }
        });
    }


    /**
     * Method used when opening a conversation with a friend.
     * @param friendsUsername - String - the username of the friend in the opened conversation.
     */
    private void enterChat(String friendsUsername) {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ChatController.fxml"));

            ChatController chatController = new ChatController(client, friendsUsername);
            loader.setController(chatController);
            BorderPane execRoot = loader.load();
            Scene executorScene = new Scene(execRoot, 480, 395);
            Stage executorStage = new Stage();
            executorStage.setScene(executorScene);

            executorScene.getStylesheets().add(getClass().getResource("ChatStyle.css").toString());
            executorStage.setTitle("Chat to " + friendsUsername);
            executorStage.show();

            //when the chat closes, it ends the thread waiting for a message from the server.
            executorStage.setOnHiding(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            //send the server this message in order to end the current conversation opened.
                            client.sendCode("#007KILL#");
                        }
                    });
                }
            });

        }catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Reads strings from a given file
     * @param file - String - path name
     * @return A Fx collection of the array of strings read from the file as an observable arrayList.
     */
    private ObservableList<String> getStringList(String file){
        List<String> strings = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;
            while((s = br.readLine()) != null){
                strings.add(s);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FXCollections.observableArrayList(strings);
    }


    /**
     * @param whatToWrite - String - what needs to be written in the file
     * @param path - String - the path of the file to write to
     */
    private void logToFile(String whatToWrite, String path) {
        try{
            PrintWriter logFile = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
            logFile.write(whatToWrite + "\n");
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Updates the friends list (writes into the friends file and into the listView), with the text from the search
     * textField, when the Add Friend button is clicked.
     */
    void onAddFriendButtonClicked(){
        String friendUsername = search_textField.getText();
        if(!friendUsername.isEmpty()){
            //searches in the list of friends to guarantee the uniqueness of the relationship in the table
            for(String friend: getStringList(this.friendsFilePath))
                if(Objects.equals(friend, friendUsername)) {
                    sendErrorAlert("Oops...", "You are already friends with this user...");
                    search_textField.clear();
                    return;
                }

            //sends the friend's username to the server to check its existence
            client.sendCode("#001FriendRequest#");

            client.sendAddFriendUsername(friendUsername);
            String msg = client.receiveRequestMessage();
            if(Objects.equals(msg, "User exists.")){
                logToFile(friendUsername, this.friendsFilePath);
                friend_list.setItems(getStringList(this.friendsFilePath));
            }
            else{
                sendErrorAlert("No username", "There is no user with this username.");
            }
            search_textField.clear();
        }
        else{
            sendErrorAlert("No username", "Please enter a username.");
        }
    }

    
    /**
     * Method called when clicking the add button. Get the selected friend and opens a chat with him.
     */
    public void onAddChatButtonClicked(){
        String sendAtUsername = friend_list.getSelectionModel().getSelectedItem();
        if(sendAtUsername != null){
            //check if the chat already exists in the chat file
            for(String chat: getStringList(this.chatsFilePath))
                if(Objects.equals(chat, sendAtUsername)){
                    sendErrorAlert("Oops...", "This chat already exists!");
                    return ;
                }

            //add it to the list
            logToFile(sendAtUsername, this.chatsFilePath);
            chat_list.setItems(getStringList(this.chatsFilePath));
        }
        else{
            sendErrorAlert("No user selected", "Please select an user to chat with.");
        }
    }


    /**
     * Creates an error GUI element with the given title and header
     * @param title - String
     * @param headerText - String
     */
    public void sendErrorAlert(String title, String headerText){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }
}
