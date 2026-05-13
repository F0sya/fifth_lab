package rpg.model.PlayerDir;

import java.util.Random;

public class Dice {
    private static final Random random = new Random();

    private static boolean isHolyDiceActive = false;

    public static int roll20() {
        if (isHolyDiceActive) {
            isHolyDiceActive = false;
            System.out.println("Використано Святий Кубик! Випадає 20!");
            return 20;
        }
        return random.nextInt(20) + 1;
    }

    public static void activateHolyDice() {
        isHolyDiceActive = true;
    }
}