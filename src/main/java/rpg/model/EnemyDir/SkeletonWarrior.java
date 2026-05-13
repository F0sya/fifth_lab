package rpg.model.EnemyDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import rpg.model.ItemsDir.Weapon;

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


    public SkeletonWarrior(){
        this(100,1.2,"Скелет-воїн");
    }

    public SkeletonWarrior(int hp, double damageMultiplier, String name){
        this.hp = hp;
        this.maxHP = hp;
        this.damageMultiplier = damageMultiplier;
        this.name = name;
        this.isActive = false;
        this.weapon = new Weapon(100);    }

    public void takeDamage(int amount){
        this.hp -= amount;
        if (this.hp < 0){ this.hp = 0;}
    }

    public void heal(int amount){
        this.hp += amount;
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

    public Group draw(boolean isDevMode) {
        Group skeletonGroup = new Group();


        Rectangle body = new Rectangle(this.x - 10, this.y - 40, 20, 40);
        body.setFill(Color.DARKGRAY);


        Circle head = new Circle(this.x, this.y - 45, 10);
        head.setFill(Color.LIGHTGRAY);


        javafx.scene.shape.Line weapon = new javafx.scene.shape.Line(this.x, this.y - 20, this.x + 25, this.y - 20);
        weapon.setStroke(Color.DARKGRAY);
        weapon.setStrokeWidth(3);

        Text nameText = new Text(this.x - 25, this.y - 60, this.name);
        nameText.setFill(Color.WHITE);

        skeletonGroup.getChildren().addAll(body, head, weapon, nameText);

        if (isDevMode) {
            nameText.setText(this.name + " (" + this.hp + "HP)");
            nameText.setFill(Color.RED);

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
