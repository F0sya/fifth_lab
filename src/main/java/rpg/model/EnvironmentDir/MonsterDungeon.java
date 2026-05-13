package rpg.model.EnvironmentDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MonsterDungeon extends DungeonRoom {

    public MonsterDungeon(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
    }

    @Override
    public Group draw(boolean isDevMode) {
        Group dungeonGroup = new Group();

        Rectangle walls = new Rectangle(getX(), getY(), getWidth(), getHeight());
        walls.setFill(Color.web("#1a0a0a"));
        walls.setStroke(Color.web("#660000"));
        walls.setStrokeWidth(5);

        int floorHeight = 40;
        Rectangle floor = new Rectangle(getX(), getY() + getHeight() - floorHeight, getWidth(), floorHeight);
        floor.setFill(Color.web("#2a1515"));

        Circle skullLeft = new Circle(getX() + 30, getY() + 30, 12);
        skullLeft.setFill(Color.web("#c8c8c8"));
        skullLeft.setStroke(Color.web("#666666"));
        skullLeft.setStrokeWidth(2);

        Line bone1 = new Line(getX() + 18, getY() + 42, getX() + 42, getY() + 18);
        bone1.setStroke(Color.web("#aaaaaa"));
        bone1.setStrokeWidth(3);
        Line bone2 = new Line(getX() + 42, getY() + 42, getX() + 18, getY() + 18);
        bone2.setStroke(Color.web("#aaaaaa"));
        bone2.setStrokeWidth(3);

        Circle eyeLeft = new Circle(getX() + 26, getY() + 28, 3);
        eyeLeft.setFill(Color.RED);
        Circle eyeRight = new Circle(getX() + 34, getY() + 28, 3);
        eyeRight.setFill(Color.RED);

        Text infoText = new Text(getX() + 55, getY() + 35, getName() + " | Ворогів: " + getEnemies().size());
        infoText.setFill(Color.web("#ff4444"));
        infoText.setFont(Font.font("Serif", FontWeight.BOLD, 13));

        Ellipse bloodStain = new Ellipse(getX() + getWidth() / 2.0, getY() + getHeight() - 20, 40, 10);
        bloodStain.setFill(Color.web("#8b0000", 0.5));

        dungeonGroup.getChildren().addAll(walls, floor, skullLeft, bone1, bone2, eyeLeft, eyeRight, infoText, bloodStain);

        return dungeonGroup;
    }
}
