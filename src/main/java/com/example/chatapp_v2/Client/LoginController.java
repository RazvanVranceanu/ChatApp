package com.example.chatapp_v2.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button login_button;
    @FXML
    private Button register_button;
    @FXML
    private TextField text_login;

    private Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = new Client(new Socket("localhost", 1234));

            login_button.setOnKeyPressed(event -> {
                        if (event.getCode().equals(KeyCode.ENTER)) {
                            login_button.fire();
                        }
                    }
            );

        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Error connecting to server.");
        }
    }

    /**
     * Function called when pushing the login button.
     * It takes the text from the text field and sends it to the server.
     */
    @FXML
    public void pressLoginButton(){
        String usernameToSend = text_login.getText();
        if(!usernameToSend.isEmpty()){
            // "/Login" signals the server that the text represents an username for the login.
            client.sendRequestUsername("Login/" + usernameToSend);
            text_login.clear();
            String messageReceived = client.receiveRequestMessage();

            //display the register request message received from the server
            boolean response = displayLoginMessage(messageReceived);
            //if the login succeeded, enter the main application
            if(response)
                enterChatApp();

        }
        else{
            //Show an alert if something went wrong.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle(":(");
            alert.setHeaderText("Please enter an username");
            alert.showAndWait();
        }
    }

    /**
     * Function used for entering the app itself, after the login phase.
     */
    private void enterChatApp(){
        try {
            //create a enw controller
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MenusController.fxml"));
            MenusController menusController = new MenusController(client);
            loader.setController(menusController);
            BorderPane execRoot = loader.load();
            Scene executorScene = new Scene(execRoot, 650, 400); //v1 = height
            Stage executorStage = new Stage();
            executorStage.setScene(executorScene);

            //set the according stylesheet
            executorScene.getStylesheets().add(getClass().getResource("MenuStyle.css").toString());
            executorStage.setTitle(this.client.getCurrentUsername());
            executorStage.show();

            //close the prev window
            Stage stage = (Stage) login_button.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which displays a different popup according to the type of message received upon trying to log in
     * @param messageReceived - String - message received from the server.
     * @return true, if the login was successful, false, otherwise
     */
    private boolean displayLoginMessage(String messageReceived) {
        if(messageReceived.contains("successfully")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle(":)");
            alert.setHeaderText("Logged in successfully!");
            alert.setContentText(messageReceived);
            alert.showAndWait();
            return true;
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle("something went wrong...");
            alert.setHeaderText("Login failed!");
            alert.setContentText(messageReceived);
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Method called when pressing the register button.
     */
    @FXML
    public void pressRegisterButton(){
        String usernameToSend = text_login.getText();
        if(!usernameToSend.isEmpty()){
            //send the text together with the tag "/Register", in order to signal the server that the username should be
            //registered
            client.sendRequestUsername("Register/" + usernameToSend);
            text_login.clear();
            String messageReceived = client.receiveRequestMessage();

            //display the register request message received from the server
            displayRegisterMessage(messageReceived);

        }
        else{
            //show an alert if registration failed
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle(":(");
            alert.setHeaderText("Please enter an username");
            alert.showAndWait();
        }
    }

    /**
     * Method which displays a different popup according to the type of message received upon trying to register
     * @param messageReceived - String - message from the server.
     */
    private void displayRegisterMessage(String messageReceived) {
        if(messageReceived.contains("successfully")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle("YAY");
            alert.setHeaderText("Registered successfully!");
            alert.setContentText(messageReceived);
            alert.showAndWait();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toString());
            alert.setTitle("Bad news");
            alert.setHeaderText("Registered failed!");
            alert.setContentText(messageReceived);
            alert.showAndWait();
        }
    }
}