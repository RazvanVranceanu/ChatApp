module com.example.chatapp_v2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.chatapp_v2 to javafx.fxml;
    exports com.example.chatapp_v2;
    exports com.example.chatapp_v2.Client;
    opens com.example.chatapp_v2.Client to javafx.fxml;
}