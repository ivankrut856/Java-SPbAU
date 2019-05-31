package fr.ladybug.team.models;

import fr.ladybug.team.views.TargetView;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/** Class representing the target of the game. It should be destroyed to win */
public class Target {
    /** The cell in which the target is placed */
    private Cell holdingCell;
    private TargetView view;

    public Target(Cell holdingCell, TargetView view) {
        this.holdingCell = holdingCell;
        holdingCell.addOnDestroyListener(this::destroy);
        view.getRectangle().setX(holdingCell.getX() * Model.CELL_WIDTH);
        view.getRectangle().setY(holdingCell.getY() * Model.CELL_HEIGHT);
        this.view = view;
    }

    public TargetView getView() {
        return view;
    }

    /**
     * The callback method. It is called when the host cell is blown up
     * @param where the cell from where it was place on the blow up moment
     */
    public void destroy(Cell where) {
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.INFORMATION, "You beat them!");
            alert.setTitle("Victory!");
            alert.showAndWait();
            Platform.exit();
        });
    }
}
