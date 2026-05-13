package rpg.model.EnvironmentDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Tavern extends DungeonRoom {

    public Tavern(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
    }

    @Override
    public Group draw(boolean isDevMode) {
        Group tavernGroup = new Group();

        Rectangle walls = new Rectangle(getX(), getY(), getWidth(), getHeight());
        walls.setFill(Color.web("#3b2f1e"));
        walls.setStroke(Color.web("#7a5c3a"));
        walls.setStrokeWidth(4);

        Polygon roof = new Polygon();
        roof.getPoints().addAll(
                (double) getX() - 10, (double) getY(),
                (double) getX() + getWidth() / 2.0, (double) getY() - 40,
                (double) getX() + getWidth() + 10, (double) getY()
        );
        roof.setFill(Color.web("#8b4513"));
        roof.setStroke(Color.web("#5c2e00"));
        roof.setStrokeWidth(3);

        int floorHeight = 40;
        Rectangle floor = new Rectangle(getX(), getY() + getHeight() - floorHeight, getWidth(), floorHeight);
        floor.setFill(Color.web("#6b4226"));

        Ellipse sign = new Ellipse(getX() + getWidth() / 2.0, getY() + 30, 60, 18);
        sign.setFill(Color.web("#d4a556"));
        sign.setStroke(Color.web("#8b6914"));
        sign.setStrokeWidth(2);

        Text signText = new Text(getX() + getWidth() / 2.0 - 30, getY() + 35, "⚔ Таверна");
        signText.setFill(Color.web("#3b1f00"));
        signText.setFont(Font.font("Serif", FontWeight.BOLD, 12));

        Text infoText = new Text(getX() + 10, getY() + 60, getName() + " | Ворогів: " + getEnemies().size());
        infoText.setFill(Color.WHEAT);
        infoText.setFont(Font.font("Serif", FontWeight.NORMAL, 13));

        Arc window = new Arc(getX() + getWidth() / 2.0, getY() + getHeight() / 2.0, 20, 25, 0, 180);
        window.setType(ArcType.CHORD);
        window.setFill(Color.web("#ffd700", 0.3));
        window.setStroke(Color.web("#8b6914"));
        window.setStrokeWidth(2);

        tavernGroup.getChildren().addAll(walls, roof, floor, sign, signText, infoText, window);

        return tavernGroup;
    }
}
