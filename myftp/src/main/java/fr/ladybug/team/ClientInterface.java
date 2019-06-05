package fr.ladybug.team;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;


public class ClientInterface extends Application {
    private static final int BASE_SCREEN_WIDTH = 300;
    private static final int BASE_SCREEN_HEIGHT = 600;
    private static final int MIN_SCREEN_HEIGHT = 200;
    private static final int MIN_SCREEN_WIDTH = 100;

    private static Scanner scanner = new Scanner(System.in);

    private static Socket server;
    private static OutputStream outputStream;
    private static InputStream inputStream;

    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    private ObservableList<FileView> dataSupplier;
    private Client client;

    /** {@inheritDoc} */
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("starting");
        TextInputDialog remoteAddressSupplier = new TextInputDialog("ip.ad.dr.re:port");
        remoteAddressSupplier.setTitle("MyFTP");
        remoteAddressSupplier.setHeaderText("Welcome to MyFTP");
        remoteAddressSupplier.setContentText("Please enter server's remote address");

        Client tmpClient = null;

        while (true) {
            var result = remoteAddressSupplier.showAndWait();

            if (!result.isPresent()) {
                Platform.exit();
                return;
            }

            String[] userInput = result.get().split(":", 2);
            try {
                if (userInput.length == 2) {
                    int port = Integer.parseInt(userInput[1]);
                    tmpClient = new Client(userInput[0], port);
                    break;
                }
            }
            catch (NumberFormatException ignore) {
            }
            catch (IOException e) {
                var alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Network");
                alert.setHeaderText("Incorrect host");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                continue;
            }
            var alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input");
            alert.setHeaderText("Incorrect");
            alert.setContentText("Please follow the following pattern: host:port");
            alert.showAndWait();
        }

        client = tmpClient;
        dataSupplier = FXCollections.observableArrayList();

        loadCurrentFolder();

        logger.info("initial load");

        var listView = new ListView<>(dataSupplier);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex == -1) {
                    return;
                }
                var currentView = listView.getItems().get(selectedIndex);
                if (currentView.isDirectory()) {
                    if (currentView == FileView.PARENT)
                        client.popDir();
                    else {
                        client.pushDirectory(currentView.getFileName());
                    }
                    loadCurrentFolder();
                }
                else {
                    if (currentView == FileView.LOADING)
                        return;

                    client.saveFile(currentView.getFileName(), (message) -> {
                        var alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("File download");
                        alert.setHeaderText("Status");
                        alert.setContentText(message);
                        alert.show();
                    });
                }
            }
        });

        StackPane pane = new StackPane();
        pane.getChildren().add(listView);

        Scene scene = new Scene(pane, BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(MIN_SCREEN_HEIGHT);
        primaryStage.setMinWidth(MIN_SCREEN_WIDTH);
        primaryStage.show();
    }

    private void loadCurrentFolder() {
        dataSupplier.clear();
        dataSupplier.addAll(FXCollections.observableArrayList(FileView.LOADING));
        client.load((List<FileView> views) -> {
            dataSupplier.clear();
            dataSupplier.addAll(views);
        }, (message) -> {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection");
            alert.setHeaderText("Status");
            alert.setContentText("Connection lost");
            Platform.exit();
        });
    }
}