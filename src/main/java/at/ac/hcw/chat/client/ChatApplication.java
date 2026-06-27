package at.ac.hcw.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ChatApplication.class.getResource("/at/ac/hcw/chat/client/fxml/login-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                ChatApplication.class
                        .getResource("/at/ac/hcw/chat/client/css/style.css")
                        .toExternalForm()
        );

        stage.setTitle("Chat Room");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
