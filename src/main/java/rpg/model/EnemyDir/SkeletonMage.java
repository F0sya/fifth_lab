package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Objects;


public class SkeletonMage extends SkeletonArcher {
    private int mana;

    public SkeletonMage() {

        super(60, 2.5, "Скелет-маг");
        this.mana = 50;
    }

    public void castSpell() {
        if (mana >= 10) {
            mana -= 10;
            System.out.println(getName() + " кастує магію! Мана: " + mana);
        }
    }

    @Override
    public Group draw(boolean isDevMode) {
        Group mageGroup = super.draw(isDevMode);

        javafx.scene.shape.Line staff = new javafx.scene.shape.Line(getX() + 15, getY() - 40, getX() + 15, getY());
        staff.setStroke(Color.PURPLE);
        staff.setStrokeWidth(3);

        Circle magicOrb = new Circle(getX() + 15, getY() - 45, 6);
        magicOrb.setFill(Color.MAGENTA);

        mageGroup.getChildren().addAll(staff, magicOrb);
        return mageGroup;
    }
}