package rpg.model.ItemsDir;

public class Weapon implements Cloneable {
    private int durability; // Мутабельна змінна стану зброї

    public Weapon(int durability) {
        this.durability = durability;
    }

    public int getDurability() { return durability; }
    public void setDurability(int durability) { this.durability = durability; }

    // Глибинне копіювання самої зброї
    @Override
    public Weapon clone() {
        try {
            return (Weapon) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
