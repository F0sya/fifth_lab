package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Objects;
import rpg.model.EnvironmentDir.World;
import rpg.model.PlayerDir.Player;

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
            setInteractionText("Постріл! (" + arrowsCount + ")", 10);
            System.out.println(getName() + " стріляє! Стріл: " + arrowsCount);
        } else {
            arrowsCount = 10;
            setInteractionText("Перезарядка", 10);
        }
    }

    @Override
    public void updateAutomaticBehavior(World world, Player player, String moveMode) {
        decrementCooldown();

        if ("Stationary".equals(moveMode)) {
            return;
        }

        if ("Follow Player".equals(moveMode)) {
            double dx = player.getX() - getX();
            double dy = player.getY() - getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 200) {
                double angle = Math.atan2(dy, dx) + Math.PI;
                move(2.0, angle);
            } else if (dist > 350) {
                double angle = Math.atan2(dy, dx);
                move(2.0, angle);
            } else {
                if (Math.random() < 0.15) {
                    shootArrow();
                }
            }
        } else {
            if (wanderDirectionCooldown <= 0) {
                currentWanderAngle = (Math.random() < 0.5) ? 0 : Math.PI;
                wanderDirectionCooldown = 15 + (int) (Math.random() * 20);
            } else {
                wanderDirectionCooldown--;
            }
            move(1.2, currentWanderAngle);
            if (Math.random() < 0.05) {
                shootArrow();
            }
        }

        if (getX() < 50) setX(50);
        if (getX() > 5000) setX(5000);
        if (getY() < 50) setY(50);
        if (getY() > 600) setY(600);
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