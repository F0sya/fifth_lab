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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rpg.model.EnemyDir.SkeletonArcher;
import rpg.model.EnemyDir.SkeletonMage;
import rpg.model.EnemyDir.SkeletonWarrior;
import rpg.model.EnvironmentDir.DungeonRoom;
import rpg.model.EnvironmentDir.Tavern;
import rpg.model.EnvironmentDir.MonsterDungeon;
import rpg.model.EnvironmentDir.PuzzleDungeon;
import javafx.collections.FXCollections;
import rpg.model.EnvironmentDir.World;
import rpg.model.PlayerDir.Dice;
import rpg.model.PlayerDir.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Random;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    private boolean isDeveloperMode = false;
    private Group root = new Group();
    private Text statusText = new Text(10, 20, "");

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

        Tavern tavern = new Tavern(50, 50, 1500, 500, "Таверна \"Золотий Кубик\"");
        MonsterDungeon monsterDungeon = new MonsterDungeon(1700, 50, 1500, 500, "Підземелля з монстрами");
        PuzzleDungeon puzzleDungeon = new PuzzleDungeon(3350, 50, 1500, 500, "Підземелля з загадками");

        world.addRoom(tavern);
        world.addRoom(monsterDungeon);
        world.addRoom(puzzleDungeon);

        int floorY = tavern.getY() + tavern.getHeight() - 5;

        SkeletonWarrior warrior = new SkeletonWarrior();
        warrior.setX(600);
        warrior.setY(floorY);

        SkeletonArcher archer = new SkeletonArcher();
        archer.setX(2200);
        archer.setY(floorY);

        SkeletonMage mage = new SkeletonMage();
        mage.setX(3900);
        mage.setY(floorY);

        tavern.addEnemy(warrior);
        monsterDungeon.addEnemy(archer);
        puzzleDungeon.addEnemy(mage);

        player = new Player("Авантюрист", 150, floorY);

        scene.setOnKeyPressed(event -> {
            List<SkeletonWarrior> allEnemies = world.getAllEnemies();


            if (event.getCode() == KeyCode.F2) {
                isDeveloperMode = !isDeveloperMode;
                if (!isDeveloperMode) {
                    for (SkeletonWarrior enemy : allEnemies) enemy.setActive(false);
                }
                updateUI();
                return;
            }

            if (isDeveloperMode) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    for (SkeletonWarrior enemy : allEnemies) enemy.setActive(false);
                }
                else if (event.getCode() == KeyCode.DELETE) {
                    for (DungeonRoom room : world.getRooms()) {
                        room.getEnemies().removeIf(SkeletonWarrior::isActive);
                    }
                    world.getFreeEnemies().removeIf(SkeletonWarrior::isActive);
                }
                else if (event.isControlDown() && event.getCode() == KeyCode.C) {
                    List<SkeletonWarrior> newClones = new ArrayList<>();
                    for (SkeletonWarrior enemy : allEnemies) {
                        if (enemy.isActive()) {
                            SkeletonWarrior clone = enemy.clone();
                            clone.setX(clone.getX() + 30);
                            clone.setY(clone.getY() + 30);
                            clone.setActive(false);
                            newClones.add(clone);
                        }
                    }

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
                    for (DungeonRoom room : world.getRooms()) room.performDarkRitual();
                }
                else if (event.getCode() == KeyCode.INSERT) {
                    showCreateDialog();
                }
                else if (event.getCode() == KeyCode.U) {
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
                    for (SkeletonWarrior e : toBind) world.removeFreeEnemy(e);
                }

                updateUI();
                return;
            }


            if (inCombat) {
                return;
            }


            boolean moved = false;
            if (event.getCode() == KeyCode.W) { player.setY(player.getY() - 15); moved = true; }
            else if (event.getCode() == KeyCode.S) { player.setY(player.getY() + 15); moved = true; }
            else if (event.getCode() == KeyCode.A) { player.setX(player.getX() - 15); moved = true; }
            else if (event.getCode() == KeyCode.D) { player.setX(player.getX() + 15); moved = true; }

            if (moved) {
                for (SkeletonWarrior enemy : world.getAllEnemies()) {
                    if (enemy.getHP() > 0 && Math.abs(player.getX() - enemy.getX()) < 60) {
                        inCombat = true;
                        currentTarget = enemy;
                        System.out.println("БІЙ ПОЧАТО З " + enemy.getName());
                        break;
                    }
                }
                updateUI();
            }
        });

        updateUI();
        primaryStage.setTitle("Dungeon RPG: Лабораторна робота №4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateUI() {
        root.getChildren().clear();


        Group cameraGroup = new Group();
        cameraGroup.getTransforms().clear();

        if (isDeveloperMode) {

            DungeonRoom currentRoom = world.getRooms().get(0);
            for (DungeonRoom room : world.getRooms()) {
                if (player.getX() >= room.getX() && player.getX() <= room.getX() + room.getWidth()) {
                    currentRoom = room;
                    break;
                }
            }

            double scaleFactor = 800.0 / (currentRoom.getWidth() + 100);

            cameraGroup.getTransforms().add(new Scale(scaleFactor, scaleFactor, 0, 0));

            double targetX = currentRoom.getX() - 50;
            double targetY = currentRoom.getY() - 100;
            cameraGroup.getTransforms().add(new Translate(-targetX, -targetY));

        } else {
            cameraX = player.getX() - 400;
            cameraY = player.getY() - 450;

            cameraGroup.getTransforms().add(new Translate(-cameraX, -cameraY));
        }

        cameraGroup.getChildren().add(world.draw(isDeveloperMode));

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
            cameraGroup.getChildren().add(enemyGraphic);
        }

        cameraGroup.getChildren().add(player.draw(isDeveloperMode));

        buildStatusBar();

        Rectangle statusBg = new Rectangle(0, 0, 800, 30);
        statusBg.setFill(Color.web("#000000", 0.7));
        statusBg.setVisible(isDeveloperMode);

        root.getChildren().addAll(cameraGroup, statusBg, statusText);

        if (inCombat && !isDeveloperMode) {
            root.getChildren().add(createCombatMenu());
        }
    }

    private void buildStatusBar() {
        if (!isDeveloperMode) {
            statusText.setText("Натисніть F2 для Developer Mode");
            statusText.setFill(Color.GRAY);
            statusText.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
            return;
        }

        List<SkeletonWarrior> activeEnemies = world.getAllEnemies().stream()
                .filter(SkeletonWarrior::isActive)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("[DEV] ");

        if (activeEnemies.isEmpty()) {
            sb.append("Немає активних мікрооб'єктів. ");
        } else {
            sb.append("Активних: ").append(activeEnemies.size()).append(" → ");
            for (int i = 0; i < activeEnemies.size(); i++) {
                SkeletonWarrior e = activeEnemies.get(i);
                String roomName = "вільний";
                for (DungeonRoom room : world.getRooms()) {
                    if (room.getEnemies().contains(e)) {
                        roomName = room.getName();
                        break;
                    }
                }
                sb.append(e.getName())
                        .append(" (").append(e.getHP()).append("HP, ")
                        .append(roomName).append(")");
                if (i < activeEnemies.size() - 1) sb.append("; ");
            }
            sb.append(" ");
        }
        sb.append("| INS=створити DEL=видалити ESC=деактивувати U=вилучити B=прив'язати Ctrl-C=клон");

        statusText.setText(sb.toString());
        statusText.setFill(Color.web("#ff4444"));
        statusText.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
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

        Label roomLabel = new Label("Прив'язати до макрооб'єкта:");
        ComboBox<String> roomBox = new ComboBox<>();
        roomBox.getItems().add("(вільний)");
        for (DungeonRoom room : world.getRooms()) {
            roomBox.getItems().add(room.getName());
        }
        String currentRoomName = "(вільний)";
        for (DungeonRoom room : world.getRooms()) {
            if (room.getEnemies().contains(enemy)) {
                currentRoomName = room.getName();
                break;
            }
        }
        roomBox.setValue(currentRoomName);

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

            String selectedRoom = roomBox.getValue();
            for (DungeonRoom room : world.getRooms()) {
                room.removeEnemy(enemy);
            }
            world.removeFreeEnemy(enemy);

            if ("(вільний)".equals(selectedRoom)) {
                world.addFreeEnemy(enemy);
            } else {
                for (DungeonRoom room : world.getRooms()) {
                    if (room.getName().equals(selectedRoom)) {
                        room.addEnemy(enemy);
                        break;
                    }
                }
            }

            updateUI();
            dialog.close();
        });

        vbox.getChildren().addAll(
                new Label("Ім'я:"), nameField,
                new Label("Поточне здоров'я (HP):"), hpField,
                new Label("Максимальне здоров'я (Max HP):"), maxHpField,
                new Label("Міцність зброї (0-100):"), durabilityField,
                roomLabel, roomBox,
                saveBtn
        );

        Scene dialogScene = new Scene(vbox, 300, 420);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    private void showCreateDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Створення нового мікрооб'єкта");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label typeLabel = new Label("Оберіть клас мікрооб'єкта:");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Скелет-воїн (Базовий)",
                "Скелет-лучник",
                "Скелет-маг"
        ));
        typeBox.setValue("Скелет-воїн (Базовий)");

        Label nameLabel = new Label("Ім'я:");
        TextField nameField = new TextField("Новий скелет");

        Label hpLabel = new Label("Здоров'я (HP):");
        TextField hpField = new TextField("100");

        CheckBox activeCheckBox = new CheckBox("Зробити активним одразу");

        Label roomLabel = new Label("Розмістити у макрооб'єкті:");
        ToggleGroup roomToggle = new ToggleGroup();
        VBox radioBox = new VBox(5);
        RadioButton freeRadio = new RadioButton("Вільний (без макрооб'єкта)");
        freeRadio.setToggleGroup(roomToggle);
        radioBox.getChildren().add(freeRadio);

        for (DungeonRoom room : world.getRooms()) {
            RadioButton rb = new RadioButton(room.getName());
            rb.setToggleGroup(roomToggle);
            rb.setUserData(room);
            radioBox.getChildren().add(rb);
        }
        if (radioBox.getChildren().size() > 1) {
            ((RadioButton) radioBox.getChildren().get(1)).setSelected(true);
        } else {
            freeRadio.setSelected(true);
        }

        Button createBtn = new Button("Створити");
        createBtn.setOnAction(e -> {
            SkeletonWarrior newEnemy;
            String selectedType = typeBox.getValue();

            if ("Скелет-лучник".equals(selectedType)) {
                newEnemy = new SkeletonArcher();
            } else if ("Скелет-маг".equals(selectedType)) {
                newEnemy = new SkeletonMage();
            } else {
                newEnemy = new SkeletonWarrior();
            }

            newEnemy.setName(nameField.getText());
            try {
                int hp = Integer.parseInt(hpField.getText());
                if (hp < 1) hp = 1;
                newEnemy.setHP(hp);
                newEnemy.setMaxHP(hp);
            } catch (NumberFormatException ex) {
                System.out.println("Некоректне значення ХП, використано базове.");
            }

            newEnemy.setActive(activeCheckBox.isSelected());

            RadioButton selectedRadio = (RadioButton) roomToggle.getSelectedToggle();
            DungeonRoom targetRoom = null;
            if (selectedRadio != null && selectedRadio.getUserData() instanceof DungeonRoom) {
                targetRoom = (DungeonRoom) selectedRadio.getUserData();
            }

            if (targetRoom != null) {
                newEnemy.setX(targetRoom.getX() + targetRoom.getWidth() / 2);
                newEnemy.setY(targetRoom.getY() + targetRoom.getHeight() - 5);
                targetRoom.addEnemy(newEnemy);
            } else {
                DungeonRoom firstRoom = world.getRooms().get(0);
                newEnemy.setX(firstRoom.getX() + firstRoom.getWidth() / 2);
                newEnemy.setY(firstRoom.getY() + firstRoom.getHeight() - 5);
                world.addFreeEnemy(newEnemy);
            }

            updateUI();
            dialog.close();
        });

        vbox.getChildren().addAll(
                typeLabel, typeBox,
                nameLabel, nameField,
                hpLabel, hpField,
                activeCheckBox,
                roomLabel, radioBox,
                createBtn
        );

        Scene dialogScene = new Scene(vbox, 350, 480);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void playDiceAnimation(Consumer<Integer> onResult) {
        Polygon d20Shape = new Polygon();
        d20Shape.getPoints().addAll(
                0.0, -40.0,
                35.0, -20.0,
                35.0, 20.0,
                0.0, 40.0,
                -35.0, 20.0,
                -35.0, -20.0
        );
        d20Shape.setFill(Color.CRIMSON);
        d20Shape.setStroke(Color.GOLD);
        d20Shape.setStrokeWidth(3);

        Text diceText = new Text("20");
        diceText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        diceText.setFill(Color.WHITE);

        StackPane dicePane = new StackPane(d20Shape, diceText);

        dicePane.setLayoutX(400 - 35);
        dicePane.setLayoutY(300 - 40);

        root.getChildren().add(dicePane);

        Timeline timeline = new Timeline();
        int rollFrames = 20;
        int finalResult = Dice.roll20();
        Random random = new Random();

        for (int i = 0; i < rollFrames; i++) {
            int currentFrame = i;
            KeyFrame frame = new KeyFrame(Duration.millis(50 * i), e -> {
                if (currentFrame < rollFrames - 1) {
                    diceText.setText(String.valueOf(random.nextInt(20) + 1));
                    dicePane.setRotate(dicePane.getRotate() + 15);
                } else {
                    diceText.setText(String.valueOf(finalResult));
                    dicePane.setRotate(0);
                }
            });
            timeline.getKeyFrames().add(frame);
        }

        KeyFrame endFrame = new KeyFrame(Duration.millis(50 * rollFrames + 500), e -> {
            root.getChildren().remove(dicePane);

            onResult.accept(finalResult);

        });

        timeline.getKeyFrames().add(endFrame);
        timeline.play();
    }

    private HBox createCombatMenu() {
        HBox menu = new HBox(30);
        menu.setAlignment(Pos.CENTER);


        menu.setLayoutX(150);
        menu.setLayoutY(500);

        String btnStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 3; -fx-font-size: 20px; -fx-font-family: 'Courier New'; -fx-padding: 10 30 10 30;";

        Button attackBtn = new Button("⚔️ АТАКА");
        attackBtn.setStyle(btnStyle);

        Button blockBtn = new Button("🛡️ БЛОК");
        blockBtn.setStyle(btnStyle);

        Button fleeBtn = new Button("🏃 ВТЕЧА");
        fleeBtn.setStyle(btnStyle);

        attackBtn.setOnAction(e -> {
            playDiceAnimation(roll -> {
                System.out.println("Викинуто на атаку: " + roll);
                int damage = roll + player.getStrength();
                currentTarget.setHP(currentTarget.getHP() - damage);

                if (currentTarget.getHP() <= 0) {
                    System.out.println("Ворога знищено!");
                    currentTarget.setHP(0);
                    currentTarget.setActive(false);
                    inCombat = false;
                } else {
                    System.out.println("Хід ворога... (ще не реалізовано)");
                }
                updateUI();
            });
        });

        fleeBtn.setOnAction(e -> {
            playDiceAnimation(roll -> {
                if (roll > 10) {
                    System.out.println("Ви успішно втекли!");
                    player.setX(player.getX() - 100);
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