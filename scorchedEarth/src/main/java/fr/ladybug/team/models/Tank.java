package fr.ladybug.team.models;

import fr.ladybug.team.views.ProjectileView;
import fr.ladybug.team.views.TankBodyView;
import fr.ladybug.team.views.TankGunView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static fr.ladybug.team.models.Model.CELL_WIDTH;
import static fr.ladybug.team.views.TankGunView.*;

/** Class representing the Tank in the game. Performing all tank related calculation */
public class Tank {
    private int x;
    private int y;
    private int deltaX = 0;

    private TankBodyView body;
    private TankGunView gun;
    private boolean destroyed = false;

    private int gunRotationAngle = 0;

    private Ammo ammo = Ammo.BALL;

    private Model currentGame;
    /** The list with all actual projectiles */
    private List<Projectile> projectiles = new ArrayList<>();

    public Tank(int x, int y, TankBodyView body, TankGunView gun, Model currentGame) {
        this.x = x;
        this.y = y;
        this.body = body;
        this.gun = gun;

        this.currentGame = currentGame;
    }

    private void updatePosition() {
        int oldX = x;
        int oldY = y;

        x += deltaX;

        int groundLevel = currentGame.getTankGroundLevel(this);

        if (-1 <= groundLevel && groundLevel <= 0) {
            y += groundLevel - 1;
        } else if (groundLevel > 1) {
            y++;
        }

        if (currentGame.checkTankCollision(this)) {
            x = oldX;
            y = oldY;
        }
    }

    /** Methods actualizes the state of the tank */
    public void update() {
        if (destroyed)
            return;
        updatePosition();

        body.getRectangle().setX(x * CELL_WIDTH);
        body.getRectangle().setY(y * Model.CELL_HEIGHT);

        if (currentGame.confirmOutOfTheWorld(body.getRectangle()))
            destroy();

        gun.chooseTexture(ammo.value);
        gun.getRectangle().setRotate(0);
        gun.getRectangle().setX(x * CELL_WIDTH + 22);
        gun.getRectangle().setY(y * Model.CELL_HEIGHT);
        gun.getRectangle().setRotate(gunRotationAngle);

        for (var projectile : projectiles) {
            projectile.update();
        }
    }

    /** Performs gun rotation of the tank
     * @param angle the angle in degrees (angle > 0 means rotation clockwise)
     */
    public void rotateGun(int angle) {
        gunRotationAngle += angle;
        if (gunRotationAngle > 90)
            gunRotationAngle = 90;
        else if (gunRotationAngle < -90)
            gunRotationAngle = -90;
    }

    /** Changes or reloads gun */
    public synchronized void changeGun() {
        ammo = ammo.change();
    }

    /** Fires the chosen ammo */
    public synchronized void fire() {
        if (ammo == Ammo.EMPTY)
            return;

        Projectile current = new Projectile(Math.toRadians(-gunRotationAngle + 90),
                x + GUN_TEXTURE_X_OFFSET / CELL_WIDTH, y,
                ammo,
                new ProjectileView(new Rectangle(TANK_GUN_TEXTURE_DEFAULT_WIDTH, TANK_GUN_TEXTURE_DEFAULT_HEIGHT), gun.getTextures().get(ammo.value)),
                (projectile -> {
                    currentGame.onProjectileDestroy(projectile);
                    Platform.runLater(() -> projectiles.remove(projectile));
                }),
                (projectile) -> currentGame.checkProjectileCrush(projectile));
        ammo = Ammo.EMPTY;
        projectiles.add(current);
        currentGame.onTankFire(current);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public TankGunView getGun() {
        return gun;
    }

    public TankBodyView getBody() {
        return body;
    }

    /** Ammo type class. Represents different ammo and changing strategy */
    public enum Ammo {
        EMPTY(0, 0, 0),
        BALL(1, 10, 2),
        FIREBALL(2, 5, 4);

        private int value;
        private double startSpeed;
        private double explosionRange;

        Ammo(int value, double startSpeed, double explosionRange) {
            this.value = value;
            this.startSpeed = startSpeed;
            this.explosionRange = explosionRange;
        }

        public double startSpeed() {
            return startSpeed;
        }

        public double explosionRange() {
            return explosionRange;
        }

        /** Return next ammo type in order (or first if there is empty now) */
        public Ammo change() {
            switch (this) {
                case FIREBALL:
                case EMPTY:
                    return BALL;
                case BALL:
                default:
                    return FIREBALL;

            }
        }

    }

    /** On tank destroy method */
    public void destroy() {
        destroyed = true;
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.INFORMATION, "You died!");
            alert.setTitle("Dead!");
            alert.showAndWait();
            Platform.exit();
        });
    }
}
