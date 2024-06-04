module com.example.lab1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires java.desktop;


    opens com.example.lab1 to javafx.fxml;
    exports com.example.lab1;
    exports com.example.lab1.Server;
    opens com.example.lab1.Server to javafx.fxml;
    exports com.example.lab1.Client;
    opens com.example.lab1.Client to javafx.fxml;
    exports com.example.lab1.Interaction;
    opens com.example.lab1.Interaction to javafx.fxml;
}