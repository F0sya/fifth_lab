package rpg.model.PlayerDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Player {
    private String name;
    private int x, y;

    private int hp, maxHp;
    private int strength;
    private int agility;
    private int rhetoric;

    public Player(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxHp = 100;
        this.hp = this.maxHp;

        this.strength = 14;
        this.agility = 12;
        this.rhetoric = 10;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getRhetoric() { return rhetoric; }


    public Group draw(boolean isDevMode) {
        Group playerGroup = new Group();

        Rectangle body = new Rectangle(x - 10, y - 40, 20, 40);
        body.setFill(Color.ROYALBLUE);


        Arc hood = new Arc(x, y - 45, 12, 12, 0, 180);
        hood.setFill(Color.DARKBLUE);

        Circle face = new Circle(x, y - 43, 8);
        face.setFill(Color.PEACHPUFF);

        javafx.scene.shape.Line sword = new javafx.scene.shape.Line(x, y - 20, x + 30, y - 20);
        sword.setStroke(Color.GOLD);
        sword.setStrokeWidth(4);

        Text nameText = new Text(x - 25, y - 60, name);
        nameText.setFill(Color.WHITE);

        playerGroup.getChildren().addAll(body, hood, face, sword, nameText);

        if (isDevMode) {
            nameText.setText(name + " (" + hp + "/" + maxHp + " HP)");
            nameText.setFill(Color.GREENYELLOW);

            Text statsText = new Text(x + 15, y - 45, "STR:" + strength + " AGI:" + agility + " RHT:" + rhetoric);
            statsText.setFill(Color.GOLD);
            statsText.setFont(javafx.scene.text.Font.font(10));

            playerGroup.getChildren().add(statsText);
        }

        return playerGroup;
    }
}