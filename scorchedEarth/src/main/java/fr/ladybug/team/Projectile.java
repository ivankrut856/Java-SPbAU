package fr.ladybug.team;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;
import java.util.function.Function;

public class Projectile {
    public Projectile(double angle, double x, double y, Tank.Ammo ammo, ProjectileView view, Consumer<Projectile> onDestroy, Function<Projectile, Boolean> checkCrush) {
        this.x = x;
        this.y = y;
        this.ammo = ammo;
        var speed = ammo.startSpeed();
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
        this.onDestroy = onDestroy;
        this.checkCrush = checkCrush;

        this.view = view;
        updateView();
    }

    private ProjectileView view;
    private Consumer<Projectile> onDestroy;
    private Function<Projectile, Boolean> checkCrush;
    private double x;
    private double y;
    private double dx;
    private double dy;
    private Tank.Ammo ammo;

    private static double EARTH_GRAVITY_DY = 0.2;

    private double toRotationAngle(double mathAngle) {
        return Math.toDegrees(-mathAngle + Math.PI/2);
    }

    public ProjectileView getView() {
        return view;
    }

    public double getExplosionRange() {
        return ammo.explosionRange();
    }

    public void update() {
        x += dx / 10;
        y -= dy / 10;
        dy -= EARTH_GRAVITY_DY;

        boolean crushed = checkCrush.apply(this);
        if (crushed) {
            onDestroy.accept(this);
        }

        updateView();
    }

    private void updateView() {
        view.getRectangle().setX(x * Model.CELL_WIDTH);
        view.getRectangle().setY(y * Model.CELL_HEIGHT);
        view.getRectangle().setRotate(toRotationAngle(Math.atan2(dy, dx)));
    }
}
