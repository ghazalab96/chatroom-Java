package at.ac.hcw.chat.client.controller;

import at.ac.hcw.chat.client.network.ChatClient;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PrivateChatController {

    @FXML private Label titleLabel;
    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private Label offlineBanner;

    private ChatClient client;
    private String myUsername;
    private String partnerUsername;
    private boolean partnerOnline = true;

    public void start(ChatClient client, String myUsername, String partnerUsername) {
        this.client = client;
        this.myUsername = myUsername;
        this.partnerUsername = partnerUsername;

        titleLabel.setText("Private chat with " + partnerUsername);
        offlineBanner.setVisible(false);
        offlineBanner.setManaged(false);
    }

    @FXML
    private void sendMessage() {
        if (!partnerOnline) {
            addSystemMessage(partnerUsername + " has left the chat room.");
            return;
        }

        String text = messageField.getText().trim();
        if (text.isBlank()) return;

        client.send("@" + partnerUsername + ":" + text);
        messageField.clear();
    }

    public void addMessage(String sender, String text) {
        boolean mine = sender.equals(myUsername)
                || sender.startsWith("[Private to");

        Label senderLabel = new Label(mine ? "You" : partnerUsername);
        senderLabel.getStyleClass().add("sender-label");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.getStyleClass().add(mine ? "bubble-mine" : "bubble-other");

        VBox content = new VBox(2, senderLabel, bubble);
        content.setMaxWidth(400);

        HBox row = new HBox(content);
        row.setPadding(new Insets(4, 14, 4, 14));
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messagesBox.getChildren().add(row);
    }

    public void markOffline() {
        partnerOnline = false;

        offlineBanner.setVisible(true);
        offlineBanner.setManaged(true);
        offlineBanner.setText(partnerUsername + " left the chat room.");

        messageField.setDisable(true);
        messageField.setPromptText(partnerUsername + " is offline");
    }

    private void addSystemMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("system-message");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(4, 14, 4, 14));

        messagesBox.getChildren().add(row);
    }
}
