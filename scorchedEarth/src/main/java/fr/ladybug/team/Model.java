package fr.ladybug.team;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Model {
    public static final int CELL_WIDTH = 10;
    public static final int CELL_HEIGHT = 10;
    public static final int CELL_COLUMNS_COUNT = 80;
    public static final int CELL_ROWS_COUNT = 60;

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
                var status = FieldMap.getState(i, j) ? Cell.PresenceStatus.FULL : Cell.PresenceStatus.EMPTY;
                var rectangle = new Rectangle(j * CELL_WIDTH, i * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                var currentCell = new Cell(j, i, status, new CellView(rectangle, Arrays.asList(cellStatesTexture)));
                cells.add(currentCell);
            }
        }
    }

    private void tankInit() {
        var tankBodyTexture = new ImagePattern(
                new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("tank-part-body.png"))));
        var tankBody = new TankBodyView(new Rectangle(5 * CELL_WIDTH, 3 * CELL_HEIGHT), tankBodyTexture);

        var tankGunTextures = new ArrayList<ImagePattern>();
        for (int i = 0; i < 3; i++) {
            tankGunTextures.add(new ImagePattern(
                    new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(String.format("tank-part-gun%d.png", i))))));
        }
        var tankGun = new TankGunView(new Rectangle(5, 12), tankGunTextures);
        tank = new Tank(0, CELL_ROWS_COUNT - 8, tankBody, tankGun, this);
    }

    private void targetInit() {
        var pin = this.getClass().getClassLoader().getResourceAsStream("target.png");
        var targetTexture = new ImagePattern(
                new Image(Objects.requireNonNull(pin)));
        Cell theCell = null;
        for (int i = 0; i < CELL_ROWS_COUNT; i++) {
            for (int j = 0; j < CELL_COLUMNS_COUNT; j++) {
                if (FieldMap.isTarget(i, j)) {
                    theCell = cells.get(j + i * CELL_COLUMNS_COUNT);
                }
            }
        }
        target = new Target(Objects.requireNonNull(theCell), new TargetView(new Rectangle(CELL_WIDTH, CELL_HEIGHT), targetTexture));
    }

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

    public void onTankFire(Projectile projectile) {
        drawableGroup.getChildren().add(projectile.getView().getRectangle());
    }

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

    public int getTankGroundLevel(Tank tank) {
        int min = 100;
        for (int x = tank.getX(); x < tank.getX() + 5; x++) {
            int y = tank.getY();
            while (y < CELL_ROWS_COUNT && cells.get(y * CELL_COLUMNS_COUNT + x).getPresenceStatus() == Cell.PresenceStatus.EMPTY)
                y++;
            if (y == CELL_ROWS_COUNT)
                y = CELL_ROWS_COUNT + 10;
            min = Math.min(min, y - tank.getY() - 2);
        }
        return min;
    }

    public boolean checkTankCrush(Tank tank) {
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

    private boolean confirmOutOfTheWorld(Rectangle rectangle) {
        return rectangle.getX() > CELL_COLUMNS_COUNT * CELL_WIDTH || rectangle.getX() < 0 || rectangle.getY() > CELL_ROWS_COUNT * CELL_HEIGHT;
    }
}
