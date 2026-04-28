package rpg.model.PlayerDir;

import java.util.Random;

public class Dice {
    private static final Random random = new Random();

    // Чи активований артефакт "Святий кубик" на наступний хід
    private static boolean isHolyDiceActive = false;

    // Звичайний кидок 20-гранного кубика
    public static int roll20() {
        if (isHolyDiceActive) {
            isHolyDiceActive = false; // Артефакт діє лише один раз
            System.out.println("Використано Святий Кубик! Випадає 20!");
            return 20;
        }
        return random.nextInt(20) + 1; // Генерує число від 1 до 20
    }

    // Метод для активації артефакту
    public static void activateHolyDice() {
        isHolyDiceActive = true;
    }
}