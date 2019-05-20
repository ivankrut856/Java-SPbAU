package fr.ladybug.team;

import fr.ladybug.team.models.Model;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static fr.ladybug.team.models.Model.*;

/**
 * Main application. Works with screen and render
 */
public class Screen extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /** Update is performed each 50ms */
    private final long UPDATE_RATE = TimeUnit.MILLISECONDS.toNanos(50);

    private Group group;
    private Model model;

    @Override
    /** Main entry point of the application */
    public void start(Stage stage) throws Exception {
        group = new Group();
        final Scene scene = new Scene(group, CELL_WIDTH * CELL_COLUMNS_COUNT, CELL_HEIGHT * CELL_ROWS_COUNT);
        model = new Model(group);

        group.getChildren().addAll(model.getCells().stream().map((cell) -> cell.getView().getRectangle()).collect(Collectors.toCollection(ArrayList::new)));
        group.getChildren().add(model.getTarget().getView().getRectangle());
        group.getChildren().add(model.getTank().getBody().getRectangle());
        group.getChildren().add(model.getTank().getGun().getRectangle());

        group.getChildren().add(getExitButton());
        group.getChildren().add(getHelpButton());

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= UPDATE_RATE) {
                    lastUpdate = now;
                    model.update();
                }
            }
        };
        timer.start();

        scene.setOnKeyPressed(this::keyPressedHandler);
        scene.setOnKeyReleased(this::keyReleasedHandler);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    private void keyPressedHandler(KeyEvent e) {
        System.out.println(e.getCode().toString());
        if (e.getCode() == KeyCode.D) {
            model.getTank().setDeltaX(1);
        }
        else if (e.getCode() == KeyCode.A) {
            model.getTank().setDeltaX(-1);
        }
        else if (e.getCode() == KeyCode.W) {
            model.getTank().rotateGun(10);
        }
        else if (e.getCode() == KeyCode.S) {
            model.getTank().rotateGun(-10);
        }
        else if (e.getCode() == KeyCode.R) {
            model.getTank().changeGun();
        }
        else if (e.getCode() == KeyCode.J) {
            model.getTank().fire();
        }
    }

    private void keyReleasedHandler(KeyEvent e) {
        if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.A) {
            model.getTank().setDeltaX(0);
        }
    }

    public Group getGroup() {
        return group;
    }

    /** Exit button init */
    private static Button getExitButton() {
        var redCrossImage = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("redCross5.png")));

        var button = new Button();
        button.setMinSize(2 * CELL_WIDTH, 2 * CELL_HEIGHT);
        button.setPrefSize(2 * CELL_WIDTH, 2 * CELL_HEIGHT);
        button.setLayoutX((CELL_COLUMNS_COUNT - 2) * CELL_WIDTH);
        button.setLayoutY(0);


        button.setGraphic(new ImageView(redCrossImage));
        button.setBackground(Background.EMPTY);

        button.setOnMouseClicked(event -> Platform.exit());
        return button;
    }

    /** Help button init */
    private static Button getHelpButton() {
        var questionMarkImage = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("questionMark.png")));

        var button = new Button();
        button.setMinSize(2 * CELL_WIDTH, 2 * CELL_HEIGHT);
        button.setPrefSize(2 * CELL_WIDTH, 2 * CELL_HEIGHT);
        button.setLayoutX((CELL_COLUMNS_COUNT - 4) * CELL_WIDTH);
        button.setLayoutY(0);

        button.setOnMouseClicked((event) -> {
            Platform.runLater(() -> {
                var helpMessage = "Left-Right: A-D\nGun control: W-S\nFire: J\nReload/Change: R";
                var alert = new Alert(Alert.AlertType.INFORMATION, helpMessage);
                alert.setTitle("Help");
                alert.showAndWait();
            });
        });

        button.setGraphic(new ImageView(questionMarkImage));
        button.setBackground(Background.EMPTY);

        return button;
    }

}
