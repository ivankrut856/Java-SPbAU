package fr.ladybug.team.models;

import fr.ladybug.team.models.Tank;
import fr.ladybug.team.views.ProjectileView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;
import java.util.function.Function;

/** Class representing projectiles of the tank's gun in the world. Performs some physics related to shots */
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

        if (ammo == Tank.Ammo.EMPTY)
            throw new RuntimeException("Unexpected error. Tank created EMPTY projectile");

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

    /** Empirical data of earth gravity in the game world */
    private static double EARTH_GRAVITY_DY = 0.2;

    private double toRotationAngle(double mathAngle) {
        return Math.toDegrees(-mathAngle + Math.PI / 2);
    }

    public ProjectileView getView() {
        return view;
    }

    /** Retunrs explosion range of the chosen ammo */
    public double getExplosionRange() {
        return ammo.explosionRange();
    }

    /** Updates physical properties and render in time */
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
