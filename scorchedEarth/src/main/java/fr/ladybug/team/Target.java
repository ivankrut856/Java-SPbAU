package fr.ladybug.team;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Target {
    private ImagePattern texture;
    private Cell myCell;
    private Rectangle rectangle;

    public Target(Image texture, Cell myCell) {
        this.texture = new ImagePattern(texture);

        this.myCell = myCell;
        myCell.addOnDestroyListener(this::destroy);

        rectangle = new Rectangle(myCell.getAssociatedNode().getX(), myCell.getAssociatedNode().getY(), myCell.getAssociatedNode().getWidth(), myCell.getAssociatedNode().getHeight());
        rectangle.setFill(this.texture);
    }

    public Rectangle getRectangle() {
        return rectangle;
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
