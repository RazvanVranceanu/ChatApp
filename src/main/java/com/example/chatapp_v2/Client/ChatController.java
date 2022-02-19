package com.example.chatapp_v2.Client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class ChatController implements Initializable {
    private final Client client;

    private final String friendsUsername;

    private final String pathToChat;

    @FXML
    private Button button_send;
    @FXML
    private ScrollPane sp_main;
    @FXML
    private VBox vbox_messages;
    @FXML
    private TextField tf_message;


    public ChatController(Client client, String friendsUsername) {
        this.client = client;
        this.friendsUsername = friendsUsername;
        this.pathToChat = client.getCurrentUsername() + "/" + "To" + friendsUsername + "Chat.txt";

        File myFile = new File(pathToChat);
        try {
            myFile.getParentFile().mkdirs();
            myFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //makes the chat display the last messages first
        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        getUnreadMessages(); //request the unread messages from the server
        loadFromFile(); //load the messages from the chat file

        client.receiveMessageFrom(vbox_messages);
    }


    /**
     * Takes the messages from the file where the conversation is stored and builds the GUI for it (the chat bubbles)
     */
    private void loadFromFile() {
        //My messages will be encoded with "#ME:" in front
        List<String> strings = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(pathToChat));
            String s;
            while((s = br.readLine()) != null){
                strings.add(s);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String message : strings) {
            if (!message.startsWith("#ME:")) {
                addFriendsBubble(message);
            } else {
                addMyBubble(message.replaceFirst("#ME:", ""));
            }
            System.out.println(message);
        }
    }

    /**
     * Adds a bubble for the message of the current user.
     * @param messageToSend - String - represents the message (current user) that is wished to be added to the chat
     */
    public void addMyBubble(String messageToSend){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);

        hBox.setPadding(new Insets(5, 5, 5, 10));
        Text text = new Text(messageToSend);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-color: rgb(239, 242, 255);" +
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #7761d9, #00d4df);" +
                "-fx-background-radius: 20px;"+
                "-fx-font-family: Verdana;" +
                "-fx-font-size: 15px");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(0.934, 0.945, 0.996));

        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);
    }

    /**
     * Adds a bubble for the message of the friend.
     * @param messageFromFriend - String - represents the message from the friend that is wished to be added to the chat
     */
    public void addFriendsBubble(String messageFromFriend){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromFriend);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                "-fx-background-radius: 20px;" +
                "-fx-font-family: Verdana;" +
                "-fx-font-size: 15px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);
        vbox_messages.getChildren().add(hBox);
    }

    /**
     * Adds a new bubble for the messages received. Made to be used in an outside object.
     * @param messageFromServer - String - the message received
     * @param vbox - Object of type VBVox - should be the vbox from the class ChatController
     */
    public static void addLabel(String messageFromServer, VBox vbox){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromServer);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" +
                "-fx-background-radius: 20px;" +
                "-fx-font-family: Verdana;" +
                "-fx-font-size: 15px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);

        //update GUI from application thread
        //utility cass
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vbox.getChildren().add(hBox);
            }
        });
    }

    /**
     * Gets the unread messages for the current chat. Also logs them to the file.
     */
    private void getUnreadMessages() {
        //this code is sent to the server to let it know that the user wants to initiate
        // the receiving of the unread messages procedure
        client.sendCode("#002requestUnread#");

        //gets the unread messages from the server
        List<String> unreadMessages = client.getUnreadMessagesWith(friendsUsername);

        //Writes the messages in the files
        try{
            PrintWriter logFile = new PrintWriter(new BufferedWriter(new FileWriter(pathToChat, true)));
            for(String msg: unreadMessages){
                logFile.write(msg + "\n");
            }
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when the send button is clicked. It takes the message written in the text label.
     */
    @FXML
    private void sendButtonClicked(){
        String messageToSend = tf_message.getText();
        if(!messageToSend.isEmpty()){
            addMyBubble(messageToSend);
            client.sendCode("#003sendMessage#");
            client.sendMessageToServer(messageToSend, friendsUsername);
            client.saveMessageInChat("#ME:" + messageToSend, friendsUsername);
            tf_message.clear();
        }
    }

}
