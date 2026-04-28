package rpg;


import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rpg.model.EnemyDir.SkeletonArcher;
import rpg.model.EnemyDir.SkeletonMage;
import rpg.model.EnemyDir.SkeletonWarrior;
import rpg.model.EnvironmentDir.DungeonRoom; // Наш новий макрооб'єкт
import javafx.collections.FXCollections;
import rpg.model.EnvironmentDir.World;
import rpg.model.PlayerDir.Dice;
import rpg.model.PlayerDir.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.Random;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private boolean isDeveloperMode = false;
    private Group root = new Group();
    private Text statusText = new Text(10, 20, "");

    // Тепер у нас є Універсальний об'єкт замість однієї кімнати!
    private World world;

    private double cameraX = 0;
    private double cameraY = 0;

    private Player player;

    private boolean inCombat = false;
    private SkeletonWarrior currentTarget = null;

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(root, 800, 600);

        world = new World();



        DungeonRoom room1 = new DungeonRoom(50, 50, 1500, 500, "Склеп Забутих");
        DungeonRoom room2 = new DungeonRoom(1700, 50, 1500, 500, "Зал Очікування"); // Починається через 150px після першої
        DungeonRoom room3 = new DungeonRoom(3350, 50, 1500, 500, "Кімната Боса");   // Ще далі вправо

        world.addRoom(room1);
        world.addRoom(room2);
        world.addRoom(room3);

        // --- 2. Розкидаємо ворогів по великій кімнаті ---
        // Вираховуємо рівень підлоги один раз, щоб не дублювати код
        int floorY = room1.getY() + room1.getHeight() - 5;

        SkeletonWarrior warrior = new SkeletonWarrior();
        warrior.setX(600); // Стоїть далеченько
        warrior.setY(floorY);

        SkeletonArcher archer = new SkeletonArcher();
        archer.setX(1000); // Стоїть ще далі
        archer.setY(floorY);

        SkeletonMage mage = new SkeletonMage();
        mage.setX(1350); // Майже в самому кінці першої кімнати
        mage.setY(floorY);

        room1.addEnemy(warrior);
        room1.addEnemy(archer);
        room1.addEnemy(mage);
        player = new Player("Авантюрист", 150, floorY);

        scene.setOnKeyPressed(event -> {
            List<SkeletonWarrior> allEnemies = world.getAllEnemies();

            // --- 1. АБСОЛЮТНИЙ ПРІОРИТЕТ: Глобальний перемикач F2 ---
            if (event.getCode() == KeyCode.F2) {
                isDeveloperMode = !isDeveloperMode;
                if (!isDeveloperMode) {
                    for (SkeletonWarrior enemy : allEnemies) enemy.setActive(false); // Знімаємо виділення при виході
                }
                updateUI();
                return; // Виходимо, щоб інші клавіші не спрацювали
            }

            // --- 2. РЕЖИМ РОЗРОБНИКА (Керуємо світом, а не Героєм) ---
            if (isDeveloperMode) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    for (SkeletonWarrior enemy : allEnemies) enemy.setActive(false);
                }
                else if (event.getCode() == KeyCode.DELETE) {
                    // Видаляємо виділених ворогів звідусіль
                    for (DungeonRoom room : world.getRooms()) {
                        room.getEnemies().removeIf(SkeletonWarrior::isActive);
                    }
                    world.getFreeEnemies().removeIf(SkeletonWarrior::isActive);
                }
                else if (event.isControlDown() && event.getCode() == KeyCode.C) {
                    // Глибоке копіювання (Клонування) виділених об'єктів
                    List<SkeletonWarrior> newClones = new ArrayList<>();
                    for (SkeletonWarrior enemy : allEnemies) {
                        if (enemy.isActive()) {
                            SkeletonWarrior clone = enemy.clone();
                            clone.setX(clone.getX() + 30); // Зсуваємо копію, щоб було видно
                            clone.setY(clone.getY() + 30);
                            clone.setActive(false);
                            newClones.add(clone);
                        }
                    }
                    // Всі клони з'являються як "вільні" об'єкти (не прив'язані до кімнат)
                    for (SkeletonWarrior clone : newClones) {
                        world.addFreeEnemy(clone);
                    }
                }
                else if (event.getCode() == KeyCode.UP) {
                    for (SkeletonWarrior e : allEnemies) if (e.isActive()) e.move(0, -10);
                }
                else if (event.getCode() == KeyCode.DOWN) {
                    for (SkeletonWarrior e : allEnemies) if (e.isActive()) e.move(0, 10);
                }
                else if (event.getCode() == KeyCode.LEFT) {
                    for (SkeletonWarrior e : allEnemies) if (e.isActive()) e.move(-10, 0);
                }
                else if (event.getCode() == KeyCode.RIGHT) {
                    for (SkeletonWarrior e : allEnemies) if (e.isActive()) e.move(10, 0);
                }
                else if (event.getCode() == KeyCode.SPACE) {
                    // Темний ритуал (d20) для всіх кімнат
                    for (DungeonRoom room : world.getRooms()) room.performDarkRitual();
                }
                else if (event.getCode() == KeyCode.INSERT) {
                    // Меню створення нового скелета
                    showCreateDialog();
                }
                else if (event.getCode() == KeyCode.U) {
                    // Вилучення (Unbind) активних скелетів з кімнат у "вільні"
                    for (SkeletonWarrior enemy : allEnemies) {
                        if (enemy.isActive()) {
                            for (DungeonRoom room : world.getRooms()) {
                                if (room.getEnemies().contains(enemy)) {
                                    room.removeEnemy(enemy);
                                    world.addFreeEnemy(enemy);
                                    System.out.println(enemy.getName() + " вилучено з кімнати!");
                                }
                            }
                        }
                    }
                }
                else if (event.getCode() == KeyCode.B) {
                    // Прив'язка (Bind) активних "вільних" скелетів до кімнати, над якою вони стоять
                    List<SkeletonWarrior> toBind = new ArrayList<>();
                    for (SkeletonWarrior enemy : world.getFreeEnemies()) {
                        if (enemy.isActive()) {
                            for (DungeonRoom room : world.getRooms()) {
                                if (enemy.getX() >= room.getX() && enemy.getX() <= room.getX() + room.getWidth() &&
                                        enemy.getY() >= room.getY() && enemy.getY() <= room.getY() + room.getHeight()) {

                                    room.addEnemy(enemy);
                                    toBind.add(enemy);
                                    System.out.println(enemy.getName() + " прив'язано до кімнати!");
                                    break;
                                }
                            }
                        }
                    }
                    // Видаляємо прив'язаних зі списку вільних
                    for (SkeletonWarrior e : toBind) world.removeFreeEnemy(e);
                }

                updateUI();
                return; // В режимі F2 більше нічого не робимо (блокуємо рух Героя)
            }

            // --- 3. ГЕЙМПЛЕЙ: Якщо ми в бою (чекаємо натискання кнопок меню) ---
            if (inCombat) {
                return; // Рух заблоковано!
            }

            // --- 4. ЗВИЧАЙНА ГРА: Рух Гравця та Радар зіткнень ---
            boolean moved = false;
            if (event.getCode() == KeyCode.W) { player.setY(player.getY() - 15); moved = true; }
            else if (event.getCode() == KeyCode.S) { player.setY(player.getY() + 15); moved = true; }
            else if (event.getCode() == KeyCode.A) { player.setX(player.getX() - 15); moved = true; }
            else if (event.getCode() == KeyCode.D) { player.setX(player.getX() + 15); moved = true; }

            if (moved) {
                // Радар: перевіряємо, чи не підійшли ми впритул до живого ворога
                for (SkeletonWarrior enemy : world.getAllEnemies()) {
                    if (enemy.getHP() > 0 && Math.abs(player.getX() - enemy.getX()) < 60) {
                        inCombat = true; // ПОЧИНАЄМО БІЙ!
                        currentTarget = enemy;
                        System.out.println("БІЙ ПОЧАТО З " + enemy.getName());
                        break;
                    }
                }
                updateUI();
            }
        });

        updateUI();
        primaryStage.setTitle("Dungeon RPG: Курсова робота");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateUI() {
        root.getChildren().clear();

        // 1. Створюємо "коробку" для камери
        Group cameraGroup = new Group();
        cameraGroup.getTransforms().clear(); // Очищаємо всі старі зсуви та масштаби

        if (isDeveloperMode) {
            // --- РЕЖИМ БОГА (F2): Бачимо всю кімнату ---

            // Знаходимо кімнату, в якій зараз знаходиться гравець
            DungeonRoom currentRoom = world.getRooms().get(0);
            for (DungeonRoom room : world.getRooms()) {
                if (player.getX() >= room.getX() && player.getX() <= room.getX() + room.getWidth()) {
                    currentRoom = room;
                    break;
                }
            }

            // Наші кімнати гігантські (1500px), а екран лише 800px.
            // Тому ми "стискаємо" світ (масштаб залежить від розміру кімнати)
            double scaleFactor = 800.0 / (currentRoom.getWidth() + 100);

            // Додаємо масштаб (відштовхуючись від координати 0,0)
            cameraGroup.getTransforms().add(new Scale(scaleFactor, scaleFactor, 0, 0));

            // Зсуваємо світ так, щоб ця кімната рівнесенько стала по центру екрана
            double targetX = currentRoom.getX() - 50;
            double targetY = currentRoom.getY() - 50;
            cameraGroup.getTransforms().add(new Translate(-targetX, -targetY));

        } else {
            // --- ЗВИЧАЙНА ГРА: Камера слідкує за гравцем ---
            cameraX = player.getX() - 400;
            cameraY = player.getY() - 450;

            // Зсуваємо світ у протилежний бік від камери
            cameraGroup.getTransforms().add(new Translate(-cameraX, -cameraY));
        }

        // 2. Додаємо намальований світ у нашу камеру
        cameraGroup.getChildren().add(world.draw(isDeveloperMode));

        // 3. Відновлюємо можливість клікати мишкою по ворогах (для F2)
        for (SkeletonWarrior enemy : world.getAllEnemies()) {
            Group enemyGraphic = enemy.draw(isDeveloperMode);
            enemyGraphic.setOnMouseClicked(event -> {
                if (isDeveloperMode) {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        enemy.toggleActive();
                        updateUI();
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        showEditDialog(enemy);
                    }
                }
            });
            // Ворогів теж кладемо в камеру
            cameraGroup.getChildren().add(enemyGraphic);
        }

        // 4. Додаємо нашого Гравця в камеру (поверх ворогів і світу)
        cameraGroup.getChildren().add(player.draw(isDeveloperMode));

        // 5. Налаштовуємо текст статусу
        statusText.setText(isDeveloperMode ? "Developer Mode: ON (Клікай по ворогах, WASD вимкнено)" : "Developer Mode: OFF (Натисніть F2)");
        statusText.setFill(isDeveloperMode ? Color.RED : Color.GRAY);

        // 6. Додаємо все на головний екран (камеру та статичний текст)
        root.getChildren().addAll(cameraGroup, statusText);

        // 7. Якщо ми в бою, малюємо меню Undertale поверх усього (АЛЕ ховаємо його в F2)
        if (inCombat && !isDeveloperMode) {
            root.getChildren().add(createCombatMenu());
        }
    }

    private void showEditDialog(SkeletonWarrior enemy) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Редагування: " + enemy.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        TextField nameField = new TextField(enemy.getName());
        TextField hpField = new TextField(String.valueOf(enemy.getHP()));
        TextField maxHpField = new TextField(String.valueOf(enemy.getMaxHP()));
        TextField durabilityField = new TextField(String.valueOf(enemy.getWeapon().getDurability()));

        Button saveBtn = new Button("Зберегти");
        saveBtn.setOnAction(e -> {
            enemy.setName(nameField.getText());
            try {
                int inputMaxHp = Integer.parseInt(maxHpField.getText());
                int inputHp = Integer.parseInt(hpField.getText());
                int inputDurability = Integer.parseInt(durabilityField.getText());

                if (inputMaxHp < 1) inputMaxHp = 1;
                if (inputHp < 0) inputHp = 0;
                if (inputHp > inputMaxHp) inputHp = inputMaxHp;
                if (inputDurability < 0) inputDurability = 0;
                if (inputDurability > 100) inputDurability = 100;

                enemy.setMaxHP(inputMaxHp);
                enemy.setHP(inputHp);
                enemy.getWeapon().setDurability(inputDurability);

            } catch (NumberFormatException ex) {
                System.out.println("Введено некоректне число!");
            }

            updateUI();
            dialog.close();
        });

        vbox.getChildren().addAll(
                new Label("Ім'я:"), nameField,
                new Label("Поточне здоров'я (HP):"), hpField,
                new Label("Максимальне здоров'я (Max HP):"), maxHpField,
                new Label("Міцність зброї (0-100):"), durabilityField,
                saveBtn
        );

        Scene dialogScene = new Scene(vbox, 250, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    private void showCreateDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Створення нового мікрооб'єкта");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // 1. ComboBox (Випадаючий список для вибору класу нащадка)
        Label typeLabel = new Label("Оберіть клас мікрооб'єкта:");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Скелет-воїн (Базовий)",
                "Скелет-лучник",
                "Скелет-маг"
        ));
        typeBox.setValue("Скелет-воїн (Базовий)"); // Значення за замовчуванням

        // 2. TextField (Текстові поля для параметрів)
        Label nameLabel = new Label("Ім'я:");
        TextField nameField = new TextField("Новий скелет");

        Label hpLabel = new Label("Здоров'я (HP):");
        TextField hpField = new TextField("100");

        // 3. CheckBox (Галочка для статусу)
        CheckBox activeCheckBox = new CheckBox("Зробити активним одразу");

        // Керуючий елемент Button
        Button createBtn = new Button("Створити");
        createBtn.setOnAction(e -> {
            SkeletonWarrior newEnemy;
            String selectedType = typeBox.getValue();

            // Демонстрація створення об'єктів різних класів ієрархії
            if ("Скелет-лучник".equals(selectedType)) {
                newEnemy = new SkeletonArcher();
            } else if ("Скелет-маг".equals(selectedType)) {
                newEnemy = new SkeletonMage();
            } else {
                newEnemy = new SkeletonWarrior();
            }

            // Записуємо параметри з TextField
            newEnemy.setName(nameField.getText());
            try {
                int hp = Integer.parseInt(hpField.getText());
                if (hp < 1) hp = 1;
                newEnemy.setHP(hp);
                newEnemy.setMaxHP(hp);
            } catch (NumberFormatException ex) {
                System.out.println("Некоректне значення ХП, використано базове.");
            }

            // Записуємо стан з CheckBox
            newEnemy.setActive(activeCheckBox.isSelected());

            // Задаємо стартові координати (щоб він з'явився по центру кімнати)
            // Отримуємо першу кімнату з нашого світу
            DungeonRoom firstRoom = world.getRooms().get(0);

            // Задаємо координати по центру першої кімнати
            // З'являється по центру кімнати по X, але стоїть рівно на підлозі по Y
            newEnemy.setX(firstRoom.getX() + firstRoom.getWidth() / 2);
            newEnemy.setY(firstRoom.getY() + firstRoom.getHeight() - 20);

            // Додаємо створений об'єкт до цієї кімнати
            firstRoom.addEnemy(newEnemy);

            updateUI(); // Оновлюємо екран
            dialog.close(); // Закриваємо вікно
        });

        vbox.getChildren().addAll(
                typeLabel, typeBox,
                nameLabel, nameField,
                hpLabel, hpField,
                activeCheckBox,
                createBtn
        );

        Scene dialogScene = new Scene(vbox, 300, 350);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    // --- Анімація кидка d20 та Діалогове вікно ---
    private void playDiceAnimation(Consumer<Integer> onResult) {
        // 1. Створюємо фігуру кубика d20 (шестикутник)
        Polygon d20Shape = new Polygon();
        d20Shape.getPoints().addAll(
                0.0, -40.0,
                35.0, -20.0,
                35.0, 20.0,
                0.0, 40.0,
                -35.0, 20.0,
                -35.0, -20.0
        );
        d20Shape.setFill(Color.CRIMSON); // Червоний кубик
        d20Shape.setStroke(Color.GOLD);  // Золота окантовка
        d20Shape.setStrokeWidth(3);

        // 2. Текст із цифрою по центру
        Text diceText = new Text("20");
        diceText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        diceText.setFill(Color.WHITE);

        // Групуємо їх разом (StackPane автоматично центрує текст у фігурі)
        StackPane dicePane = new StackPane(d20Shape, diceText);

        // Ставимо кубик рівно по центру екрана (вікно 800х600)
        dicePane.setLayoutX(400 - 35);
        dicePane.setLayoutY(300 - 40);

        // Додаємо поверх усього на екран
        root.getChildren().add(dicePane);

        // 3. Налаштовуємо анімацію
        Timeline timeline = new Timeline();
        int rollFrames = 20; // Скільки разів зміниться цифра (тривалість анімації)
        int finalResult = Dice.roll20(); // Отримуємо справжній результат кидка
        Random random = new Random();

        for (int i = 0; i < rollFrames; i++) {
            int currentFrame = i;
            // Кожні 50 мілісекунд змінюємо цифру і трохи крутимо кубик
            KeyFrame frame = new KeyFrame(Duration.millis(50 * i), e -> {
                if (currentFrame < rollFrames - 1) {
                    diceText.setText(String.valueOf(random.nextInt(20) + 1));
                    dicePane.setRotate(dicePane.getRotate() + 15); // Крутимо
                } else {
                    // Фінальний кадр: зупиняємось на нашому результаті
                    diceText.setText(String.valueOf(finalResult));
                    dicePane.setRotate(0);
                }
            });
            timeline.getKeyFrames().add(frame);
        }

        // 4. Що робити ПІСЛЯ завершення анімації (чекаємо пів секунди)
        KeyFrame endFrame = new KeyFrame(Duration.millis(50 * rollFrames + 500), e -> {
            root.getChildren().remove(dicePane); // Прибираємо кубик з екрана

            onResult.accept(finalResult);

            // Тут в майбутньому ми будемо викликати логіку удару по скелету
        });

        timeline.getKeyFrames().add(endFrame);
        timeline.play(); // Запускаємо магію!
    }

    // --- Бойове UI (Undertale / Darkest Dungeon style) ---
    private HBox createCombatMenu() {
        HBox menu = new HBox(30); // Відстань між кнопками
        menu.setAlignment(Pos.CENTER);

        // Розміщуємо меню внизу екрана по центру (вікно 800х600)
        menu.setLayoutX(150);
        menu.setLayoutY(500);

        // CSS стиль для кнопок (Чорний фон, білий текст, товста біла рамка)
        String btnStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 3; -fx-font-size: 20px; -fx-font-family: 'Courier New'; -fx-padding: 10 30 10 30;";

        Button attackBtn = new Button("⚔️ АТАКА");
        attackBtn.setStyle(btnStyle);

        Button blockBtn = new Button("🛡️ БЛОК");
        blockBtn.setStyle(btnStyle);

        Button fleeBtn = new Button("🏃 ВТЕЧА");
        fleeBtn.setStyle(btnStyle);

        // --- ЛОГІКА КНОПОК ---
        attackBtn.setOnAction(e -> {
            // Запускаємо кубик!
            playDiceAnimation(roll -> {
                System.out.println("Викинуто на атаку: " + roll);
                int damage = roll + player.getStrength(); // Проста формула урону
                currentTarget.setHP(currentTarget.getHP() - damage);

                if (currentTarget.getHP() <= 0) {
                    System.out.println("Ворога знищено!");
                    currentTarget.setHP(0);
                    currentTarget.setActive(false);
                    inCombat = false; // Виходимо з бою
                } else {
                    System.out.println("Хід ворога... (ще не реалізовано)");
                }
                updateUI();
            });
        });

        fleeBtn.setOnAction(e -> {
            playDiceAnimation(roll -> {
                if (roll > 10) { // Шанс втекти 50%
                    System.out.println("Ви успішно втекли!");
                    player.setX(player.getX() - 100); // Відстрибуємо назад
                    inCombat = false;
                } else {
                    System.out.println("Втеча не вдалася! Хід ворога...");
                }
                updateUI();
            });
        });

        menu.getChildren().addAll(attackBtn, blockBtn, fleeBtn);
        return menu;
    }

    public static void main(String[] args) {
        launch(args);
    }
}