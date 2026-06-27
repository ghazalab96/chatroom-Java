package at.ac.hcw.chat.client.controller;

import at.ac.hcw.chat.client.network.ChatClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private Label errorLabel;

    @FXML
    private void connect() {
        String host = hostField.getText().trim();
        String username = usernameField.getText().trim();

        if (host.isBlank() || username.isBlank()) {
            showError("Host and username are required.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Port must be a number.");
            return;
        }

        try {
            ChatClient client = new ChatClient();
            client.connect(host, port, username);
            openChatRoom(client, username);
        } catch (Exception e) {
            showError("Could not connect to server.");
        }
    }

    private void openChatRoom(ChatClient client, String username) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/at/ac/hcw/chat/client/fxml/chat-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/at/ac/hcw/chat/client/css/style.css").toExternalForm()
        );

        ChatController chatController = loader.getController();
        chatController.start(client, username);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Chat Room — " + username);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
