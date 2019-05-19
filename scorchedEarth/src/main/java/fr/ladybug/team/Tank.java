package fr.ladybug.team;

import javafx.application.Platform;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Tank {
    private int x;
    private int y;
    private int deltaX = 0;
//    private int deltaY = 0;
    private Rectangle associatedBody;
    private Rectangle associatedGun;

    private int gunRotationAngle = 0;

    private TextureSet gunTextures;
    private AmmoStatus ammoStatus = AmmoStatus.BALL;

    private Screen currentGame;
    private List<Projectile> projectiles = new ArrayList<>();

    public Tank(int x, int y, Rectangle associatedBody, Rectangle associatedGun, TextureSet gunTextures, Screen currentGame) {
        this.x = x;
        this.y = y;
        this.associatedBody = associatedBody;
        this.associatedGun = associatedGun;

        if (gunTextures.getPatterns().size() < 3)
            throw new IllegalArgumentException("Tank texture set wrong size");
        this.gunTextures = gunTextures;
        this.currentGame = currentGame;

        fillGun();
    }

    private void fillGun() {
        associatedGun.setFill(gunTextures.getPatterns().get(ammoStatus.value));
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
        fillGun();
        associatedBody.setX(x * Screen.CELL_WIDTH);
        associatedBody.setY(y * Screen.CELL_HEIGHT);

        associatedGun.setRotate(0);
        associatedGun.setX(x * Screen.CELL_WIDTH + 22);
        associatedGun.setY(y * Screen.CELL_HEIGHT);
        associatedGun.setRotate(gunRotationAngle);

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
        ammoStatus = ammoStatus.change();
    }

    public void fire() {
        if (ammoStatus == AmmoStatus.EMPTY)
            return;

        System.out.println(associatedGun.getY());
        Projectile current = new Projectile(Math.toRadians(-gunRotationAngle + 90),
                associatedGun.getX(), associatedGun.getY(),
                ammoStatus,
                gunTextures.getPatterns().get(ammoStatus.value),
                (projectile -> {
                    currentGame.onProjectileDestroy(projectile);
                    Platform.runLater(() -> projectiles.remove(projectile));
                }),
                (projectile) -> currentGame.checkProjectileCrush(projectile));
        ammoStatus = AmmoStatus.EMPTY;
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

//    public int getDeltaY() {
//        return deltaY;
//    }
//
//    public void setDeltaY(int deltaY) {
//        this.deltaY = deltaY;
//    }

    public Rectangle getAssociatedGun() {
        return associatedGun;
    }

    public Rectangle getAssociatedBody() {
        return associatedBody;
    }

    private enum AmmoStatus {
        EMPTY(0, 0, 0),
        BALL(1, 10, 2),
        FIREBALL(2, 5, 4);

        private int value;
        private double startSpeed;
        private double explosionRange;
        AmmoStatus(int value, double startSpeed, double explosionRange) {
            this.value = value;
            this.startSpeed = startSpeed;
            this.explosionRange = explosionRange;
        }
        AmmoStatus change() {
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

    public static class Projectile {
        private Projectile(double angle, double x, double y, AmmoStatus ammo, ImagePattern texture, Consumer<Projectile> onDestroy, Function<Projectile, Boolean> checkCrush) {
            this.x = x;
            this.y = y;
            this.ammo = ammo;
            var speed = ammo.startSpeed;
            this.dx = Math.cos(angle) * speed;
            this.dy = Math.sin(angle) * speed;
            this.onDestroy = onDestroy;
            this.checkCrush = checkCrush;

            view = new Rectangle(5, 12, texture);
            updateView();
        }

        private Consumer<Projectile> onDestroy;
        private Function<Projectile, Boolean> checkCrush;
        private Rectangle view;
        private double x;
        private double y;
        private double dx;
        private double dy;
        private AmmoStatus ammo;

        private static double EARTH_GRAVITY_DY = 0.2;

        private double toRotationAngle(double mathAngle) {
            return Math.toDegrees(-mathAngle + Math.PI/2);
        }

        public Rectangle getRectangle() {
            return view;
        }

        public double getExplosionRange() {
            return ammo.explosionRange;
        }

        private void update() {
            x += dx;
            y -= dy;
            dy -= EARTH_GRAVITY_DY;



            boolean crushed = checkCrush.apply(this);
            if (crushed) {
                onDestroy.accept(this);
            }

            updateView();
        }

        private void updateView() {
            view.setX(x);
            view.setY(y);
            view.setRotate(toRotationAngle(Math.atan2(dy, dx)));
            System.out.println(view.getX());
        }
    }
}
