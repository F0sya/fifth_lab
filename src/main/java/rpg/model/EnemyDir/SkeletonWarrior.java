package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import rpg.model.ItemsDir.Weapon;
import rpg.model.EnvironmentDir.World;
import rpg.model.PlayerDir.Player;

import java.util.Objects;

import java.awt.*;

public class SkeletonWarrior implements Cloneable {

    private int hp;
    private int maxHP;
    private double damageMultiplier;
    private String name;
    private boolean isActive;

    private Weapon weapon;

    private int x;
    private int y;

    private String ownerRoomName;
    private String interactionText = null;
    private int interactionTextCooldown = 0;
    protected int wanderDirectionCooldown = 0;
    protected double currentWanderAngle = 0;

    public SkeletonWarrior(){
        this(100,1.2,"Скелет-воїн");
    }

    public SkeletonWarrior(int hp, double damageMultiplier, String name){
        this.hp = hp;
        this.maxHP = hp;
        this.damageMultiplier = damageMultiplier;
        this.name = name;
        this.isActive = false;
        this.weapon = new Weapon(100);
        this.ownerRoomName = null;
    }

    public void takeDamage(int amount){
        this.hp -= amount;
        if (this.hp < 0){ this.hp = 0;}
    }

    public void heal(int amount){
        this.hp += amount;
        if (this.hp > this.maxHP) this.hp = this.maxHP;
    }

    public void battleCry(){
        System.out.println(name + " клацає кістками!");
    }

    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    // Static polymorphism (Method overloading)
    public void move(double speed, double angle) {
        this.x += (int) (speed * Math.cos(angle));
        this.y += (int) (speed * Math.sin(angle));
    }

    // Virtual function for automatic behavior
    public void updateAutomaticBehavior(World world, Player player, String moveMode) {
        decrementCooldown();

        if ("Stationary".equals(moveMode)) {
            return;
        }

        if ("Follow Player".equals(moveMode)) {
            double angle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
            move(2.5, angle);
        } else {
            // Wander: keep moving in the same direction, switch direction occasionally (left or right only)
            if (wanderDirectionCooldown <= 0) {
                currentWanderAngle = (Math.random() < 0.5) ? 0 : Math.PI;
                wanderDirectionCooldown = 15 + (int) (Math.random() * 20); // 1.5 to 3.5 seconds
            } else {
                wanderDirectionCooldown--;
            }
            move(1.5, currentWanderAngle);
        }

        // Keep inside reasonable bounds
        if (this.x < 50) this.x = 50;
        if (this.x > 5000) this.x = 5000;
        if (this.y < 50) this.y = 50;
        if (this.y > 600) this.y = 600;
    }

    public void interactWith(SkeletonWarrior other) {
        if (this.interactionTextCooldown == 0) {
            setInteractionText("Клац!", 15);
            battleCry();
        }
    }

    public void setInteractionText(String text, int cooldown) {
        this.interactionText = text;
        this.interactionTextCooldown = cooldown;
    }

    public void decrementCooldown() {
        if (this.interactionTextCooldown > 0) {
            this.interactionTextCooldown--;
            if (this.interactionTextCooldown == 0) {
                this.interactionText = null;
            }
        }
    }

    public String getInteractionText() {
        return this.interactionText;
    }

    public Group draw(boolean isDevMode) {
        Group skeletonGroup = new Group();

        boolean isFree = (ownerRoomName == null);

        Rectangle body = new Rectangle(this.x - 10, this.y - 40, 20, 40);
        body.setFill(Color.DARKGRAY);

        Circle head = new Circle(this.x, this.y - 45, 10);
        head.setFill(Color.LIGHTGRAY);

        javafx.scene.shape.Line weapon = new javafx.scene.shape.Line(this.x, this.y - 20, this.x + 25, this.y - 20);
        weapon.setStroke(Color.DARKGRAY);
        weapon.setStrokeWidth(3);

        Text nameText = new Text(this.x - 25, this.y - 60, this.name);
        nameText.setFill(Color.WHITE);

        if (isFree) {
            body.setFill(Color.DARKGRAY.deriveColor(0, 1, 1, 0.5));
            head.setFill(Color.LIGHTGRAY.deriveColor(0, 1, 1, 0.5));
            nameText.setText("~ " + this.name);
        }

        skeletonGroup.getChildren().addAll(body, head, weapon, nameText);

        if (interactionText != null && interactionTextCooldown > 0) {
            Text bubbleText = new Text(this.x - 30, this.y - 70, interactionText);
            bubbleText.setFill(Color.GOLD);
            bubbleText.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 12));
            skeletonGroup.getChildren().add(bubbleText);
        }

        if (isDevMode) {
            nameText.setText((isFree ? "~ " : "") + this.name + " (" + this.hp + "HP)");
            nameText.setFill(isFree ? Color.YELLOW : Color.RED);

            Rectangle hpBg = new Rectangle(this.x - 10, this.y + 5, 20, 4);
            hpBg.setFill(Color.BLACK);
            double hpPercentage = Math.max(0, Math.min(1.0, (double) this.hp / this.maxHP));
            Rectangle hpBar = new Rectangle(this.x - 10, this.y + 5, 20 * hpPercentage, 4);
            hpBar.setFill(Color.RED);

            Rectangle weaponBg = new Rectangle(this.x - 10, this.y + 12, 20, 4);
            weaponBg.setFill(Color.BLACK);
            double weaponPercentage = Math.max(0, Math.min(1.0, this.weapon.getDurability() / 100.0));
            Rectangle weaponBar = new Rectangle(this.x - 10, this.y + 12, 20 * weaponPercentage, 4);
            weaponBar.setFill(Color.ORANGE);

            skeletonGroup.getChildren().addAll(hpBg, hpBar, weaponBg, weaponBar);

            if (isFree) {
                javafx.scene.shape.Line freeLine = new javafx.scene.shape.Line(this.x - 20, this.y + 20, this.x + 20, this.y + 20);
                freeLine.setStroke(Color.YELLOW);
                freeLine.setStrokeWidth(2);
                freeLine.getStrokeDashArray().addAll(4d, 3d);
                skeletonGroup.getChildren().add(freeLine);
            }

            if (this.isActive()) {
                Rectangle selectionBox = new Rectangle(this.x - 30, this.y - 75, 65, 100);
                selectionBox.setFill(Color.TRANSPARENT);
                selectionBox.setStroke(Color.RED);
                selectionBox.setStrokeWidth(2);
                selectionBox.getStrokeDashArray().addAll(5d, 5d);
                skeletonGroup.getChildren().add(selectionBox);
            }
        }

        return skeletonGroup;
    }


    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getHP() {return hp;}
    public void setHP(int hp) {this.hp = hp;}

    public double getDamageMultiplier() {return damageMultiplier;}
    public void setDamageMultiplier(double damageMultiplier) {this.damageMultiplier = damageMultiplier;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public boolean isActive() {return isActive;}
    public void setActive(boolean active) {isActive = active;}

    public Weapon getWeapon() { return weapon; }

    public int getMaxHP() {return maxHP;}
    public void setMaxHP(int maxHP) { this.maxHP = maxHP;}

    public String getOwnerRoomName() { return ownerRoomName; }
    public void setOwnerRoomName(String ownerRoomName) { this.ownerRoomName = ownerRoomName; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkeletonWarrior that = (SkeletonWarrior) o;
        return hp == that.hp && Double.compare(that.damageMultiplier, damageMultiplier) == 0 && isActive == that.isActive && Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "SkeletonWarrior{name='" + name + "', hp=" + hp + ", dmg=" + damageMultiplier + ", active=" + isActive + '}';
    }

    @Override
    public SkeletonWarrior clone() {
        try {
            SkeletonWarrior cloned = (SkeletonWarrior) super.clone();
            cloned.weapon = this.weapon.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
