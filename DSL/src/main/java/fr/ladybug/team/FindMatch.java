package fr.ladybug.team;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;


public class FindMatch extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    int n;
    private Button[][] buttons;
    private Board board;
    @Override
    public void start(Stage primaryStage) {

        boolean firstTime = true;
        while (true) {
            TextInputDialog welcomeDialog = new TextInputDialog(firstTime ? "4" : "N must be positive even number");
            welcomeDialog.setTitle("Welcome");
            welcomeDialog.setHeaderText("Enter N which will be the size of the field N*N");
            welcomeDialog.setContentText("Even number N:");

            Optional<String> result = welcomeDialog.showAndWait();
            try {
                if (!result.isPresent()) {
                    Platform.exit();
                    break;
                }
                var sizeCandidate = Integer.parseInt(result.get());
                if (sizeCandidate % 2 != 0 || sizeCandidate <= 0) {
                    firstTime = false;
                }
                else {
                    n = sizeCandidate;
                    break;
                }
            }
            catch (NumberFormatException ignore) {
                firstTime = false;
            }
        }


        board = new Board(n, this::stateUpdate);
        board.fillCellsRandomly();

        var pane = new GridPane();
        var columns = new ArrayList<ColumnConstraints>();
        for (int i = 0; i < n; i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(100f / (double)n);
            columns.add(column);
        }
        var rows = new ArrayList<RowConstraints>();
        for (int i = 0; i < n; i++) {
            var row = new RowConstraints();
            row.setPercentHeight(100f / (double)n);
            row.setFillHeight(true);
            rows.add(row);
        }
        pane.getColumnConstraints().addAll(columns);
        pane.getRowConstraints().addAll(rows);
        pane.setMinSize(250, 250);
        pane.setHgap(20);
        pane.setVgap(20);
        pane.setPadding(new Insets(10, 10, 10, 10));

        buttons = new Button[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                buttons[i][j] = new Button("?");
                final int ii = i;
                final int jj = j;
                buttons[i][j].setMaxHeight(Double.MAX_VALUE);
                buttons[i][j].setMaxWidth(Double.MAX_VALUE);
                pane.add(buttons[i][j], i, j);
                buttons[i][j].setOnAction(value -> {
                    if (board.isInProgress())
                        return;
                    board.onClick(ii, jj);
                });
            }
        }


        Scene scene = new Scene(pane);
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(200);
        primaryStage.setTitle("Find match");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void stateUpdate() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                buttons[i][j].setText(board.getText(i, j));
                buttons[i][j].setDisable(!board.checkHidden(i, j));
            }
        }

        if (board.hasWon()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("I have a great message for you!");
            alert.setContentText("You won");
            alert.showAndWait();

            Platform.exit();
        }
    }
}