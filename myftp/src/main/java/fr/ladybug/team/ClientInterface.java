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


public class ClientInterface extends Application {
    private static Scanner scanner = new Scanner(System.in);

    private static Socket server;
    private static OutputStream outputStream;
    private static InputStream inputStream;

//    public static void main(String[] args) {
//        printHelpMessage();
//
//        var serverAddress = askAddress();
//        Client client = null;
//        try {
//            client = new Client(serverAddress);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        while (true) {
//            String[] command = askCommand().split("\\s+");
//            if (command[0].equals("exit")) {
//                try {
//                    client.shutdown();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
//
//
//
//            byte[] response = new byte[0];
//            try {
//                response = client.makeQuery(new Query(getIdByName(command[0]), command[1]));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            switch (command[0]) {
//                case "list": {
//                    try {
//                        ResponseList responseList = ResponseList.fromBytes(response);
//                        for (int i = 0; i < responseList.filenames.length; i++) {
//                            var filename = responseList.filenames[i];
//                            System.out.println(filename + " " + responseList.isDirectory[i]);
//                        }
//                        System.out.println("got response");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//                case "get": {
//                    try {
//                        ResponseGet responseGet = ResponseGet.fromBytes(response);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//                default: {
//                    throw new RuntimeException("Internal consistency fail. Should not normally happen");
//                }
//            }
//        }
//
//        System.out.println("Goodbye!");
//    }

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
        var listView = new ListView<FileView>(dataSupplier);
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
                        System.out.println("Wanna save");
                        client.saveFile(currentView.getFilename());
                    }
                }
            }
        });

        StackPane pane = new StackPane();
        pane.getChildren().add(listView);

        Scene scene = new Scene(pane, 300, 600);
        primaryStage.setScene(scene);
        primaryStage.show();


    }
}