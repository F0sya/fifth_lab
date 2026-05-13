package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Objects;

public class SkeletonArcher extends SkeletonWarrior {
    private int arrowsCount;

    public SkeletonArcher() {
        this(80, 1.5, "Скелет-лучник");
    }

    public SkeletonArcher(int hp, double damageMultiplier, String name) {
        super(hp, damageMultiplier, name);
        this.arrowsCount = 10;
    }

    public void shootArrow() {
        if (arrowsCount > 0) {
            arrowsCount--;
            System.out.println(getName() + " стріляє! Стріл: " + arrowsCount);
        }
    }

    @Override
    public Group draw(boolean isDevMode) {
        Group archerGroup = super.draw(isDevMode);

        javafx.scene.shape.Arc bow = new javafx.scene.shape.Arc(getX() + 10, getY() - 20, 10, 20, 270, 180);
        bow.setFill(Color.TRANSPARENT);
        bow.setStroke(Color.SADDLEBROWN);
        bow.setStrokeWidth(3);

        archerGroup.getChildren().add(bow);
        return archerGroup;
    }
}