package at.ac.hcw.chat.client.controller;

import at.ac.hcw.chat.client.network.ChatClient;
import at.ac.hcw.chat.common.Message;
import at.ac.hcw.chat.common.Protocol;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatController {

    @FXML private Label usernameLabel;
    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private ListView<String> userListView;

    private ChatClient client;
    private String username;

    // tracks open private windows: partnerUsername → controller
    private final Map<String, PrivateChatController> privateWindows = new HashMap<>();

    public void start(ChatClient client, String username) {
        this.client = client;
        this.username = username;

        usernameLabel.setText(username);
        setupDoubleClick();

        client.listen(
                raw -> Platform.runLater(() -> handleIncoming(raw)),
                error -> Platform.runLater(() -> showSystemMessage(error))
        );
    }

    private void setupDoubleClick() {
        userListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = userListView.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.equals(username)) {
                    openPrivateWindow(selected);
                }
            }
        });
    }

    @FXML
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isBlank()) return;

        // if user types @username:text in main chat, open private window and route there
        if (Protocol.isPrivateCommand(text)) {
            int colon = text.indexOf(":");
            String target = text.substring(1, colon).trim();
            openPrivateWindow(target);
        }

        client.send(text);
        messageField.clear();
    }

    // ── Incoming message routing ─────────────────────────────

    private void handleIncoming(String raw) {
        Message message = Protocol.parse(raw);

        if (message.isUserList()) {
            updateUserList(message.text());
        } else if (message.isSystem()) {
            handleSystemMessage(message.text());
        } else if (message.isPrivate()) {
            routePrivateMessage(message);
        } else {
            showChatMessage(message);
        }
    }

    private void handleSystemMessage(String text) {
        showSystemMessage(text);

        // detect "X left the room" → mark their private window offline
        for (String partner : privateWindows.keySet()) {
            if (text.startsWith(partner + " left")) {
                privateWindows.get(partner).markOffline();
            }
        }
    }

    private void routePrivateMessage(Message message) {
        // "[Private from X]|text"  → partner is X
        // "[Private to X]|text"    → partner is X (echo of our own send)
        String partner = extractPartner(message.sender());
        if (partner == null) return;

        PrivateChatController window = openPrivateWindow(partner);
        window.addMessage(message.sender(), message.text());
    }

    private String extractPartner(String sender) {
        // "[Private from X]" → X
        if (sender.startsWith("[Private from ")) {
            return sender.substring("[Private from ".length(), sender.length() - 1).trim();
        }
        // "[Private to X]" → X
        if (sender.startsWith("[Private to ")) {
            return sender.substring("[Private to ".length(), sender.length() - 1).trim();
        }
        return null;
    }

    // ── Private window management ────────────────────────────

    private PrivateChatController openPrivateWindow(String partner) {
        if (privateWindows.containsKey(partner)) {
            Stage existing = getStageForPartner(partner);
            if (existing != null) existing.toFront();
            return privateWindows.get(partner);
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/ac/hcw/chat/client/fxml/private-chat-view.fxml")
            );

            Scene scene = new Scene(loader.load(), 480, 400);
            scene.getStylesheets().add(
                    getClass().getResource("/at/ac/hcw/chat/client/css/style.css").toExternalForm()
            );

            PrivateChatController controller = loader.getController();
            controller.start(client, username, partner);

            Stage stage = new Stage();
            stage.setTitle("Private — " + partner);
            stage.setScene(scene);
            stage.setMinWidth(340);
            stage.setMinHeight(300);
            stage.show();

            // tag stage so we can bring it to front later
            stage.setUserData(partner);

            privateWindows.put(partner, controller);

            // clean up map when window is closed
            stage.setOnCloseRequest(e -> privateWindows.remove(partner));

            return controller;

        } catch (IOException e) {
            throw new RuntimeException("Could not open private chat window.", e);
        }
    }

    private Stage getStageForPartner(String partner) {
        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .filter(s -> partner.equals(s.getUserData()))
                .findFirst()
                .orElse(null);
    }

    // ── User list ────────────────────────────────────────────

    private void updateUserList(String raw) {
        List<String> newUsers = Protocol.parseUserList(raw);

        // detect users who went offline
        List<String> current = List.copyOf(userListView.getItems());
        for (String old : current) {
            if (!newUsers.contains(old) && privateWindows.containsKey(old)) {
                privateWindows.get(old).markOffline();
            }
        }

        userListView.getItems().setAll(newUsers);
    }

    // ── UI helpers ───────────────────────────────────────────

    private void showChatMessage(Message message) {
        boolean mine = message.sender().equals(username);

        Label senderLabel = new Label(message.sender());
        senderLabel.getStyleClass().add("sender-label");

        Label bubble = new Label(message.text());
        bubble.setWrapText(true);
        bubble.getStyleClass().add(mine ? "bubble-mine" : "bubble-other");

        VBox content = new VBox(2, senderLabel, bubble);
        content.setMaxWidth(500);

        HBox row = new HBox(content);
        row.setPadding(new Insets(4, 16, 4, 16));
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messagesBox.getChildren().add(row);
    }

    private void showSystemMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("system-message");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(4, 16, 4, 16));

        messagesBox.getChildren().add(row);
    }
}
