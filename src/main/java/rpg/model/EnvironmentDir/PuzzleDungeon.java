package rpg.model.EnvironmentDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PuzzleDungeon extends DungeonRoom {

    public PuzzleDungeon(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
    }

    @Override
    public Group draw(boolean isDevMode) {
        Group puzzleGroup = new Group();

        Rectangle walls = new Rectangle(getX(), getY(), getWidth(), getHeight());
        walls.setFill(Color.web("#0a0a2e"));
        walls.setStroke(Color.web("#4400aa"));
        walls.setStrokeWidth(5);

        int floorHeight = 40;
        Rectangle floor = new Rectangle(getX(), getY() + getHeight() - floorHeight, getWidth(), floorHeight);
        floor.setFill(Color.web("#1a1a3e"));

        double cx = getX() + getWidth() / 2.0;
        double cy = getY() + getHeight() / 2.0 - 10;
        Polygon rune = new Polygon();
        rune.getPoints().addAll(
                cx, cy - 35,
                cx - 30, cy + 20,
                cx + 30, cy + 20
        );
        rune.setFill(Color.TRANSPARENT);
        rune.setStroke(Color.web("#bb77ff"));
        rune.setStrokeWidth(2);
        rune.getStrokeDashArray().addAll(8d, 4d);

        Arc magicCircle = new Arc(cx, cy, 40, 40, 0, 360);
        magicCircle.setType(ArcType.OPEN);
        magicCircle.setFill(Color.TRANSPARENT);
        magicCircle.setStroke(Color.web("#9944ff", 0.6));
        magicCircle.setStrokeWidth(2);

        Circle starCenter = new Circle(cx, cy, 6);
        starCenter.setFill(Color.web("#cc88ff"));
        starCenter.setStroke(Color.web("#ffaaff"));
        starCenter.setStrokeWidth(1);

        Text infoText = new Text(getX() + 10, getY() + 25, getName() + " | Ворогів: " + getEnemies().size());
        infoText.setFill(Color.web("#bb88ff"));
        infoText.setFont(Font.font("Serif", FontWeight.BOLD, 13));

        Line magicRay = new Line(getX() + 10, getY() + getHeight() - floorHeight, getX() + getWidth() - 10, getY() + getHeight() - floorHeight);
        magicRay.setStroke(Color.web("#7733cc", 0.4));
        magicRay.setStrokeWidth(2);

        puzzleGroup.getChildren().addAll(walls, floor, rune, magicCircle, starCenter, infoText, magicRay);

        return puzzleGroup;
    }
}
