package rpg.model.EnvironmentDir;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import rpg.model.EnemyDir.SkeletonWarrior;

import java.util.ArrayList;
import java.util.List;

public class DungeonRoom {
    private int x;
    private int y;
    private int width;
    private int height;
    private String name;

    // Агрегація: Кімната містить список мікрооб'єктів (ворогів)
    private List<SkeletonWarrior> enemies;


    public DungeonRoom(int x, int y, int width, int height, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.enemies = new ArrayList<>(); // Ініціалізуємо порожній список при створенні
    }

    // Метод для додавання ворога в цю кімнату
    public void addEnemy(SkeletonWarrior enemy) {
        this.enemies.add(enemy);
    }
    public void removeEnemy(SkeletonWarrior enemy) { this.enemies.remove(enemy);}

    // Геттер для списку ворогів (знадобиться нам для кліків та руху)
    public List<SkeletonWarrior> getEnemies() {
        return enemies;
    }

    // Макрооб'єкт впливає на всі свої мікрооб'єкти (Вимога 16)
    public void performDarkRitual() {
        // Кидаємо 20-гранний кубик (d20) від 1 до 20
        int d20Roll = (int) (Math.random() * 20) + 1;

        // Виводимо результат кидка в консоль (щоб було видно логіку гри)
        System.out.println("Кімната проводить Темний ритуал! На d20 випало: " + d20Roll);

        for (SkeletonWarrior enemy : enemies) {
            // Лікуємо скелетів на ту кількість ХП, що випала на кубику
            int newHp = enemy.getHP() + d20Roll;

            // Перевіряємо, щоб лікування не перевищило максимальне здоров'я
            if (newHp > enemy.getMaxHP()) {
                newHp = enemy.getMaxHP();
            }
            enemy.setHP(newHp);

            // Критичний успіх! Якщо випало 20, магія кімнати повністю лагодить зброю
            if (d20Roll == 20) {
                System.out.println("Критичний успіх! Зброя " + enemy.getName() + " повністю відновлена!");
                enemy.getWeapon().setDurability(100);
            }
        }
    }

    // Метод малювання кімнати (і всіх ворогів у ній)
    public Group draw(boolean isDevMode) {
        Group roomGroup = new Group();

        // 1. Задня стіна кімнати (темна)
        Rectangle backgroundWall = new Rectangle(x, y, width, height);
        backgroundWall.setFill(Color.web("#1e1e1e"));
        backgroundWall.setStroke(Color.web("#111111"));
        backgroundWall.setStrokeWidth(5);

        // 2. Підлога (світліша смуга в самому низу кімнати, товщиною 40 пікселів)
        int floorHeight = 40;
        Rectangle floor = new Rectangle(x, y + height - floorHeight, width, floorHeight);
        floor.setFill(Color.web("#3a3a3a"));

        // 3. Текст
        Text infoText = new Text(x + 10, y + 25, name + " | Ворогів: " + enemies.size());
        infoText.setFill(Color.LIGHTGRAY);

        roomGroup.getChildren().addAll(backgroundWall, floor, infoText);

        return roomGroup;
    }

    public int getX() { return x; }
    public void setX(int x) {this.x = x; }

    public int getY() { return y; }
    public void setY(int y) {this.y = y; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }
}