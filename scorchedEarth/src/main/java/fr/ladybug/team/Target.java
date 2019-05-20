package fr.ladybug.team;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Target {
    private Cell myCell;
    private TargetView view;

    public Target(Cell myCell, TargetView view) {
        this.myCell = myCell;
        myCell.addOnDestroyListener(this::destroy);
        view.getRectangle().setX(myCell.getX() * Model.CELL_WIDTH);
        view.getRectangle().setY(myCell.getY() * Model.CELL_HEIGHT);
        this.view = view;
    }

    public TargetView getView() {
        return view;
    }

    public void destroy(Cell where) {
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.INFORMATION, "You beat them!");
            alert.setTitle("Victory!");
            alert.showAndWait();
            Platform.exit();
        });
    }
}
