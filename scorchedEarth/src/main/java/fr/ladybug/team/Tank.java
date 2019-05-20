package fr.ladybug.team;

import javafx.application.Platform;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class Tank {
    private int x;
    private int y;
    private int deltaX = 0;

    private TankBodyView body;
    private TankGunView gun;

    private int gunRotationAngle = 0;

    private Ammo ammo = Ammo.BALL;

    private Model currentGame;
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

        if (-1 <= groundLevel && groundLevel <= 0 /* should go up */) {
            y += groundLevel - 1;
        } else if (groundLevel > 1) {
            y++;
        }

        if (currentGame.checkTankCrush(this)) {
            x = oldX;
            y = oldY;
        }
    }

    public void update() {
        updatePosition();

        body.getRectangle().setX(x * Model.CELL_WIDTH);
        body.getRectangle().setY(y * Model.CELL_HEIGHT);

        gun.chooseTexture(ammo.value);
        gun.getRectangle().setRotate(0);
        gun.getRectangle().setX(x * Model.CELL_WIDTH + 22);
        gun.getRectangle().setY(y * Model.CELL_HEIGHT);
        gun.getRectangle().setRotate(gunRotationAngle);

        for (var projectile : projectiles) {
            projectile.update();
        }
    }

    public void rotateGun(int angle) {
        gunRotationAngle += angle;
        if (gunRotationAngle > 90)
            gunRotationAngle = 90;
        else if (gunRotationAngle < -90)
            gunRotationAngle = -90;
    }

    public void changeGun() {
        ammo = ammo.change();
    }

    public void fire() {
        if (ammo == Ammo.EMPTY)
            return;

        System.out.println("Fire at " + (x + 2.4) + ": " + y);
        Projectile current = new Projectile(Math.toRadians(-gunRotationAngle + 90),
                x + 2.4, y,
                ammo,
                new ProjectileView(new Rectangle(5, 12), gun.getTexture()),
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

        Ammo change() {
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
}
