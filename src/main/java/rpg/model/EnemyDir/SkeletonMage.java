package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Objects;
import rpg.model.EnvironmentDir.World;
import rpg.model.PlayerDir.Player;


public class SkeletonMage extends SkeletonArcher {
    private int mana;

    public SkeletonMage() {

        super(60, 2.5, "Скелет-маг");
        this.mana = 50;
    }

    public void castSpell() {
        if (mana >= 10) {
            mana -= 10;
            setInteractionText("Вогонь!", 10);
            System.out.println(getName() + " кастує магію! Мана: " + mana);
        } else {
            mana = 50;
            setInteractionText("Реген мані", 10);
        }
    }

    
    public void castSpell(SkeletonWarrior ally) {
        if (mana >= 10) {
            mana -= 10;
            setInteractionText("Лікування 🌟", 10);
            ally.heal(20);
            ally.setInteractionText("+20 HP", 10);
            System.out.println(getName() + " кастує Лікування на " + ally.getName() + "! Мана: " + mana);
        } else {
            mana = 50;
            setInteractionText("Реген мані", 10);
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
            setX(getX() + (dx > 0 ? 2 : -2));
        } else {
            if (wanderDirectionCooldown <= 0) {
                currentWanderAngle = (Math.random() < 0.5) ? 0 : Math.PI;
                wanderDirectionCooldown = 20 + (int) (Math.random() * 25);
            } else {
                wanderDirectionCooldown--;
            }
            move(1.0, currentWanderAngle);
        }

        
        setY((int) (480 + 35 * Math.sin(getX() * 0.04)));

        if (getX() < 50) setX(50);
        if (getX() > 5000) setX(5000);

        
        if (Math.random() < 0.15) {
            for (SkeletonWarrior other : world.getAllEnemies()) {
                if (other != this && other.getHP() < other.getMaxHP()) {
                    double dx = getX() - other.getX();
                    double dy = getY() - other.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 150) {
                        castSpell(other);
                        break;
                    }
                }
            }
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