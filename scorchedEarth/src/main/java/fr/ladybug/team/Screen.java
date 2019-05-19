package fr.ladybug.team;

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

import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Screen extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final long UPDATE_RATE = TimeUnit.MILLISECONDS.toNanos(50);

    public static final int CELL_WIDTH = 10;
    public static final int CELL_HEIGHT = 10;
    public static final int CELL_COLUMNS_COUNT = 80;
    public static final int CELL_ROWS_COUNT = 60;

    private Image redCrossImage;
    private Image questionMarkImage;
    private Image[] cellStatesTexture = new Image[4];
    private Image[] tankGunTextures = new Image[3];
    private Image tankBodyTexture;
    private Image targetTexture;


    private List<Cell> cellControllers;
    private Tank tankController;

    private Group group;

    @Override
    public void start(Stage stage) throws Exception {
        group = new Group();
        final Scene scene = new Scene(group, CELL_WIDTH * CELL_COLUMNS_COUNT, CELL_HEIGHT * CELL_ROWS_COUNT);
        loadImages();

        tankController = getTankController();

        List<Rectangle> cells = getCells();

        Target target = null;
        cellControllers = new ArrayList<>();
        for (int i = 0; i < CELL_ROWS_COUNT; i++) {
            for (int j = 0; j < CELL_COLUMNS_COUNT; j++) {
                var status = FieldMap.getState(i, j) ? Cell.PresenceStatus.FULL : Cell.PresenceStatus.EMPTY;
                var currentCell = new Cell(i, j, status, cells.get(i * CELL_COLUMNS_COUNT + j),
                        TextureSet.getTextureSet(Arrays.asList(cellStatesTexture)));
                var isTarget = FieldMap.isTarget(i, j);
                if (isTarget) {
                    target = new Target(targetTexture, currentCell);
                }
                cellControllers.add(currentCell);
            }
        }
        if (target == null) {
            throw new IllegalStateException("There is no target on map");
        }


        group.getChildren().addAll(cells);
        group.getChildren().add(target.getRectangle());
        group.getChildren().add(tankController.getAssociatedBody());
        group.getChildren().add(tankController.getAssociatedGun());
        group.getChildren().add(getExitButton());
        group.getChildren().add(getHelpButton());

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= UPDATE_RATE) {
                    lastUpdate = now;
                    update();
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
            tankController.setDeltaX(1);
        }
        else if (e.getCode() == KeyCode.A) {
            tankController.setDeltaX(-1);
        }
        else if (e.getCode() == KeyCode.W) {
            tankController.rotateGun(10);
        }
        else if (e.getCode() == KeyCode.S) {
            tankController.rotateGun(-10);
        }
        else if (e.getCode() == KeyCode.R) {
            tankController.changeGun();
        }
        else if (e.getCode() == KeyCode.J) {
            System.out.println("FIRE!");
            tankController.fire();
        }
    }

    private void keyReleasedHandler(KeyEvent e) {
        if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.A) {
            tankController.setDeltaX(0);
        }
    }

    private void loadImages() {
        redCrossImage = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("redCross5.png")));
        questionMarkImage = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("questionMark.png")));
        for (int i = 0; i < 4; i++) {
            cellStatesTexture[i] = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream(String.format("cell-state-%d.png", i))));
        }
        for (int i = 0; i < 3; i++) {
            tankGunTextures[i] = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream(String.format("tank-part-gun%d.png", i))));
        }
        tankBodyTexture = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("tank-part-body.png")));
        targetTexture = new Image(Objects.requireNonNull(Screen.class.getClassLoader().getResourceAsStream("target.png")));
    }

    private void update() {
        System.out.println("Update tick");
        for (var currentCell : cellControllers) {
            currentCell.update();
        }
        tankController.update();
    }

    public Group getGroup() {
        return group;
    }

    private List<Rectangle> getCells() {
        List<Rectangle> cells = new ArrayList<>();
        for (int i = 0; i < CELL_ROWS_COUNT; i++) {
            for (int j = 0; j < CELL_COLUMNS_COUNT; j++) {
                var x = j * CELL_HEIGHT;
                var y = i * CELL_WIDTH;
                var cell = new Rectangle(CELL_WIDTH, CELL_HEIGHT, Color.color(0.5, 1, 0.5, ((double)i / CELL_ROWS_COUNT)));
                cell.setX(x);
                cell.setY(y);
                cell.setStrokeType(StrokeType.INSIDE);
//                cell.setStroke(Color.BLACK);

                cells.add(cell);
            }
        }
        return cells;
    }

    private Button getExitButton() {
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

    private Button getHelpButton() {
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

    private Tank getTankController() {
        var tankBodyTexturePattern = new ImagePattern(tankBodyTexture);
        Rectangle tankBody = new Rectangle(5 * CELL_WIDTH, 3 * CELL_WIDTH, tankBodyTexturePattern);
        Rectangle tankGun = new Rectangle(5, 12);
        return new Tank(0, CELL_ROWS_COUNT - 8, tankBody, tankGun,
                TextureSet.getTextureSet(Arrays.asList(tankGunTextures)), this);
    }

    private Point2D centerOfRectangle(Rectangle rectangle) {
        return new Point2D(rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
    }

    private boolean confirmOutOfScene(Rectangle rectangle) {
        return rectangle.getX() > CELL_COLUMNS_COUNT * CELL_WIDTH || rectangle.getX() < 0 || rectangle.getY() > CELL_ROWS_COUNT * CELL_HEIGHT;
    }

    public void onTankFire(Tank.Projectile projectile) {
        group.getChildren().add(projectile.getRectangle());
    }

    public void onProjectileDestroy(Tank.Projectile projectile) {
        group.getChildren().remove(projectile.getRectangle());
        Point2D centerOfProjectile = centerOfRectangle(projectile.getRectangle());
        for (var cell : cellControllers) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;
            Point2D centerOfCell = centerOfRectangle(cell.getAssociatedNode());
            double distance = centerOfProjectile.distance(centerOfCell);
            if (distance <= projectile.getExplosionRange() * 10) {
                cell.destroy();
            }
        }
    }

    public int getTankGroundLevel(Tank tank) {
        int min = 100;
        for (int x = tank.getX(); x < tank.getX() + 5; x++) {
            int y = tank.getY();
            while (cellControllers.get(y * CELL_COLUMNS_COUNT + x).getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                y++;
            min = Math.min(min, y - tank.getY() - 2);
        }
        return min;
    }

    public boolean checkTankCrush(Tank tank) {
        var rectangle = tank.getAssociatedBody();
        rectangle.setX(tank.getX() * CELL_WIDTH);
        rectangle.setY(tank.getY() * CELL_HEIGHT);

        for (var cell : cellControllers) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;

            if (rectangle.contains(centerOfRectangle(cell.getAssociatedNode()))) {
                return true;
            }
        }
        return false;
    }

    public boolean checkProjectileCrush(Tank.Projectile projectile) {
        var rectangle = projectile.getRectangle();
        if (confirmOutOfScene(rectangle))
            return true;

        for (var cell : cellControllers) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;

            if (rectangle.intersects(cell.getAssociatedNode().getLayoutBounds())) {
                return true;
            }
        }
        return false;
    }

}
