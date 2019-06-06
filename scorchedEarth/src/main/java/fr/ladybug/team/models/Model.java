package fr.ladybug.team.models;

import fr.ladybug.team.FieldMap;
import fr.ladybug.team.views.CellView;
import fr.ladybug.team.views.TankBodyView;
import fr.ladybug.team.views.TankGunView;
import fr.ladybug.team.views.TargetView;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fr.ladybug.team.views.TankBodyView.TANK_BODY_TEXTURE_DEFAULT_HEIGHT;
import static fr.ladybug.team.views.TankBodyView.TANK_BODY_TEXTURE_DEFAULT_WIDTH;
import static fr.ladybug.team.views.TankGunView.TANK_GUN_TEXTURE_DEFAULT_HEIGHT;
import static fr.ladybug.team.views.TankGunView.TANK_GUN_TEXTURE_DEFAULT_WIDTH;

/** Main game model holding together all interactive parts of the game world */
public class Model {
    public static final int CELL_WIDTH = 10;
    public static final int CELL_HEIGHT = 10;
    public static final int CELL_COLUMNS_COUNT = 80;
    public static final int CELL_ROWS_COUNT = 60;

    public static final int MOVE_SPEED = 1;
    public static final int SINGLE_ROTATION_ANGLE = 10;

    public static final int MINIMUM_GROUND_LEVEL = 100;

    private Tank tank;
    private List<Cell> cells;
    private Target target;
    private Group drawableGroup;

    public Model(Group drawableGroup) {
        this.drawableGroup = drawableGroup;

        cellsInit();
        tankInit();
        targetInit();
    }

    private void cellsInit() {
        var cellStatesTexture = new ImagePattern[4];
        for (int i = 0; i < 4; i++) {
            cellStatesTexture[i] = new ImagePattern(
                    new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(String.format("cell-state-%d.png", i)))));
        }

        cells = new ArrayList<>();
        for (int i = 0; i < CELL_ROWS_COUNT; i++) {
            for (int j = 0; j < CELL_COLUMNS_COUNT; j++) {
                var status = FieldMap.getState(i, j) != FieldMap.MapState.EMPTY ? Cell.PresenceStatus.FULL : Cell.PresenceStatus.EMPTY;
                var rectangle = new Rectangle(j * CELL_WIDTH, i * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                var currentCell = new Cell(j, i, status, new CellView(rectangle, Arrays.asList(cellStatesTexture)));
                cells.add(currentCell);
            }
        }
    }

    private void tankInit() {
        var tankBodyTexture = new ImagePattern(
                new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("tank-part-body.png"))));
        var tankBody = new TankBodyView(new Rectangle(TANK_BODY_TEXTURE_DEFAULT_WIDTH, TANK_BODY_TEXTURE_DEFAULT_HEIGHT), tankBodyTexture);

        var tankGunTextures = new ArrayList<ImagePattern>();
        for (int i = 0; i < 3; i++) {
            tankGunTextures.add(new ImagePattern(
                    new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(String.format("tank-part-gun%d.png", i))))));
        }
        var tankGun = new TankGunView(new Rectangle(TANK_GUN_TEXTURE_DEFAULT_WIDTH, TANK_GUN_TEXTURE_DEFAULT_HEIGHT), tankGunTextures);

        int startYPosition = CELL_ROWS_COUNT;
        while (FieldMap.getState(startYPosition - 1, 0) != FieldMap.MapState.EMPTY) {
            startYPosition--;
        }
        tank = new Tank(0, startYPosition - TANK_BODY_TEXTURE_DEFAULT_HEIGHT / CELL_HEIGHT, tankBody, tankGun, this);
    }

    private void targetInit() {
        var pin = this.getClass().getClassLoader().getResourceAsStream("target.png");
        var targetTexture = new ImagePattern(
                new Image(Objects.requireNonNull(pin)));
        Cell theCell = null;
        for (int i = 0; i < CELL_ROWS_COUNT; i++) {
            for (int j = 0; j < CELL_COLUMNS_COUNT; j++) {
                if (FieldMap.getState(i, j) == FieldMap.MapState.TARGET) {
                    theCell = cells.get(j + i * CELL_COLUMNS_COUNT);
                }
            }
        }
        target = new Target(Objects.requireNonNull(theCell), new TargetView(new Rectangle(CELL_WIDTH, CELL_HEIGHT), targetTexture));
    }

    /** Actualizes the states of the game objects */
    public void update() {
        for (var currentCell : cells) {
            currentCell.update();
        }
        tank.update();
    }

    public List<Cell> getCells() {
        return cells;
    }

    public Tank getTank() {
        return tank;
    }

    public Target getTarget() {
        return target;
    }

    /** Callback performing update subscription of the generated projectiles */
    public void onTankFire(Projectile projectile) {
        drawableGroup.getChildren().add(projectile.getView().getRectangle());
    }

    /** Callback performing update unsubscription of the generated projectiles and outer effects after destroy */
    public void onProjectileDestroy(Projectile projectile) {
        drawableGroup.getChildren().remove(projectile.getView().getRectangle());
        Point2D centerOfProjectile = centerOfRectangle(projectile.getView().getRectangle());
        for (var cell : cells) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;
            Point2D centerOfCell = centerOfRectangle(cell.getView().getRectangle());
            double distance = centerOfProjectile.distance(centerOfCell);
            if (distance <= projectile.getExplosionRange() * 10) {
                cell.destroy();
            }
        }
    }

    private boolean inRange(int x, int y) {
        return (x >= 0 && x < CELL_COLUMNS_COUNT && y >= 0 && y < CELL_ROWS_COUNT);
    }

    /** Calculates the tank ground level in the world */
    public int getTankGroundLevel(Tank tank) {
        int min = MINIMUM_GROUND_LEVEL;
        for (int x = tank.getX(); x < tank.getX() + (TANK_BODY_TEXTURE_DEFAULT_WIDTH / CELL_WIDTH); x++) {
            int y = tank.getY();
            while (y < CELL_ROWS_COUNT + MINIMUM_GROUND_LEVEL && (!inRange(x, y) || cells.get(y * CELL_COLUMNS_COUNT + x).getPresenceStatus() == Cell.PresenceStatus.EMPTY))
                y++;
            min = Math.min(min, y - tank.getY() - (TANK_BODY_TEXTURE_DEFAULT_HEIGHT / CELL_HEIGHT - 1));
        }
        return min;
    }

    /** Checks for collision of the tank */
    public boolean checkTankCollision(Tank tank) {
        var rectangle = tank.getBody().getRectangle();
        rectangle.setX(tank.getX() * CELL_WIDTH);
        rectangle.setY(tank.getY() * CELL_HEIGHT);

        for (var cell : cells) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;

            if (rectangle.contains(centerOfRectangle(cell.getView().getRectangle()))) {
                return true;
            }
        }
        return false;
    }

    /** Checks for collision of the projectiles */
    public boolean checkProjectileCrush(Projectile projectile) {
        var rectangle = projectile.getView().getRectangle();
        if (confirmOutOfTheWorld(rectangle))
            return true;

        for (var cell : cells) {
            if (cell.getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                continue;

            if (rectangle.intersects(cell.getView().getRectangle().getLayoutBounds())) {
                return true;
            }
        }
        return false;
    }

    private Point2D centerOfRectangle(Rectangle rectangle) {
        return new Point2D(rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
    }

    /** Checks for leaving the world */
    public boolean confirmOutOfTheWorld(Rectangle rectangle) {
        final int GAP = 100;
        return rectangle.getX() > CELL_COLUMNS_COUNT * CELL_WIDTH + GAP || rectangle.getX() < -GAP || rectangle.getY() > CELL_ROWS_COUNT * CELL_HEIGHT + GAP;
    }
}
