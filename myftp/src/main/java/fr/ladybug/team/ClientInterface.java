package fr.ladybug.team;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;


public class ClientInterface extends Application {
    private static Scanner scanner = new Scanner(System.in);

    private static Socket server;
    private static OutputStream outputStream;
    private static InputStream inputStream;

    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    private static int getIdByName(String name) {
        switch (name) {
            case "list":
                return 1;
            case "get":
                return 2;
            default:
                return -1;
        }
    }

    private ObservableList<FileView> dataSupplier;

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("starting");
        TextInputDialog remoteAddressSupplier = new TextInputDialog();
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

            if (true /*TODO check the string*/) {
                tmpClient = new Client(result.get());
                break;
            }
        }

        final Client client = tmpClient;
        dataSupplier = FXCollections.observableArrayList();
        client.load(dataSupplier);

        logger.info("initial load");

        var listView = new ListView<>(dataSupplier);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                var currentView = listView.getItems().get(selectedIndex);
                if (currentView.isDirectory()) {
                    if (currentView == FileView.PARENT)
                        client.popDir();
                    else {
                        client.pushDir(currentView.getFilename());
                    }
                    client.load(dataSupplier);
                }
                else {
                    if (currentView == FileView.LOADING)
                        return;
                    client.saveFile(currentView.getFilename());
                }
            }
        });

        StackPane pane = new StackPane();
        pane.getChildren().add(listView);

        Scene scene = new Scene(pane, 300, 600);
//        scene.
        primaryStage.setScene(scene);
        primaryStage.show();


    }
}