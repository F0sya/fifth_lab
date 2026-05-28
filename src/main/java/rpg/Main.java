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
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import java.util.Objects;
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

    
    private boolean isSimulating = false;
    private String moveMode = "Wander";
    private String selectedRoomFilter = "Всі";
    private String selectedSortCriterion = "Ім'я";
    private ListView<String> enemyListView = new ListView<>();
    private ComboBox<String> moveModeBox;
    private Timeline gameLoop;
    private HBox mainLayout = new HBox();
    private VBox sidebar;

    @Override
    public void start(Stage primaryStage) {
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

        
        gameLoop = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            updateSimulation();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);

        Pane gameContainer = new Pane();
        gameContainer.setPrefSize(800, 600);
        gameContainer.setMinSize(800, 600);
        gameContainer.setMaxSize(800, 600);
        Rectangle clip = new Rectangle(800, 600);
        gameContainer.setClip(clip);
        gameContainer.getChildren().add(root);

        
        sidebar = createSidebar();
        sidebar.setVisible(isDeveloperMode);
        sidebar.setManaged(isDeveloperMode);

        mainLayout.getChildren().addAll(gameContainer, sidebar);
        mainLayout.setStyle("-fx-background-color: #121212;");

        Scene scene = new Scene(mainLayout, isDeveloperMode ? 1100 : 800, 600);

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

            
            if (event.getCode() == KeyCode.M) {
                if ("Wander".equals(moveMode)) {
                    moveMode = "Follow Player";
                    if (moveModeBox != null) moveModeBox.setValue("Переслідування (Follow)");
                } else if ("Follow Player".equals(moveMode)) {
                    moveMode = "Stationary";
                    if (moveModeBox != null) moveModeBox.setValue("Нерухомі (Stationary)");
                } else {
                    moveMode = "Wander";
                    if (moveModeBox != null) moveModeBox.setValue("Блукання (Wander)");
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
        primaryStage.setTitle("Dungeon RPG: Лабораторна робота №5");
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

        List<SkeletonWarrior> enemies = world.getAllEnemies();
        for (SkeletonWarrior enemy : enemies) {
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

        
        for (int i = 0; i < enemies.size(); i++) {
            SkeletonWarrior e1 = enemies.get(i);
            for (int j = i + 1; j < enemies.size(); j++) {
                SkeletonWarrior e2 = enemies.get(j);
                double dx = e1.getX() - e2.getX();
                double dy = e1.getY() - e2.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 60) {
                    javafx.scene.shape.Line link = new javafx.scene.shape.Line(e1.getX(), e1.getY() - 20, e2.getX(), e2.getY() - 20);
                    link.setStroke(Color.LIGHTBLUE);
                    link.setStrokeWidth(2);
                    link.getStrokeDashArray().addAll(5d, 5d);
                    cameraGroup.getChildren().add(link);
                }
            }
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

        if (sidebar != null) {
            sidebar.setVisible(isDeveloperMode);
            sidebar.setManaged(isDeveloperMode);
        }

        if (mainLayout.getScene() != null && mainLayout.getScene().getWindow() != null) {
            mainLayout.layout();
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            double currentSceneWidth = mainLayout.getScene().getWidth();
            double stageWidth = stage.getWidth();
            double decorWidth = stageWidth - currentSceneWidth;
            if (stageWidth > 0 && currentSceneWidth > 0) {
                stage.setWidth((isDeveloperMode ? 1100 : 800) + decorWidth);
            } else {
                stage.sizeToScene();
            }
        }

        refreshEnemyList();
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

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(300);
        sidebar.setMinWidth(300);
        sidebar.setMaxWidth(300);
        sidebar.setStyle("-fx-background-color: #232323; -fx-border-color: #444444; -fx-border-width: 0 0 0 2;");

        Label title = new Label("Панель запитів & Керування");
        title.setTextFill(Color.GOLD);
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 16));

        Label moveTitle = new Label("Автоматичний Рух");
        moveTitle.setTextFill(Color.WHITE);
        moveTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        moveModeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Блукання (Wander)",
                "Переслідування (Follow)",
                "Нерухомі (Stationary)"
        ));
        moveModeBox.setValue("Блукання (Wander)");
        moveModeBox.setPrefWidth(270);
        moveModeBox.setOnAction(e -> {
            String val = moveModeBox.getValue();
            if (val.startsWith("Блукання")) moveMode = "Wander";
            else if (val.startsWith("Переслідування")) moveMode = "Follow Player";
            else moveMode = "Stationary";
        });

        Button toggleMoveBtn = new Button("Запустити рух");
        toggleMoveBtn.setPrefWidth(270);
        toggleMoveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        toggleMoveBtn.setOnAction(e -> {
            isSimulating = !isSimulating;
            if (isSimulating) {
                toggleMoveBtn.setText("Зупинити рух");
                toggleMoveBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
                gameLoop.play();
            } else {
                toggleMoveBtn.setText("Запустити рух");
                toggleMoveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
                gameLoop.pause();
            }
        });

        Label queryTitle = new Label("Списки мікрооб'єктів");
        queryTitle.setTextFill(Color.WHITE);
        queryTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        ComboBox<String> roomFilterBox = new ComboBox<>();
        roomFilterBox.getItems().addAll("Всі", "Вільні скелети");
        for (DungeonRoom room : world.getRooms()) {
            roomFilterBox.getItems().add(room.getName());
        }
        roomFilterBox.setValue("Всі");
        roomFilterBox.setPrefWidth(270);
        roomFilterBox.setOnAction(e -> {
            selectedRoomFilter = roomFilterBox.getValue();
            refreshEnemyList();
        });

        Label sortTitle = new Label("Сортувати за:");
        sortTitle.setTextFill(Color.WHITE);
        sortTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList(
                "Ім'я",
                "Здоров'я (HP)",
                "Міцність зброї"
        ));
        sortBox.setValue("Ім'я");
        sortBox.setPrefWidth(270);
        sortBox.setOnAction(e -> {
            selectedSortCriterion = sortBox.getValue();
            refreshEnemyList();
        });

        enemyListView.setPrefHeight(200);
        enemyListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        Button searchBtn = new Button("🔍 Пошук за параметрами...");
        searchBtn.setPrefWidth(270);
        searchBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold;");
        searchBtn.setOnAction(e -> showSearchDialog());

        Label helpLabel = new Label("Керування:\n[Insert] Створити об'єкт\n[F2] Режим розробника (DEV)\n[M] Зміна характеру руху");
        helpLabel.setTextFill(Color.GRAY);
        helpLabel.setFont(Font.font("Courier New", 10));

        sidebar.getChildren().addAll(
                title,
                new Separator(),
                moveTitle,
                moveModeBox,
                toggleMoveBtn,
                new Separator(),
                queryTitle,
                roomFilterBox,
                sortTitle,
                sortBox,
                enemyListView,
                new Separator(),
                searchBtn,
                helpLabel
        );

        return sidebar;
    }

    private void refreshEnemyList() {
        if (world == null) return;
        List<SkeletonWarrior> list = new ArrayList<>();

        if ("Всі".equals(selectedRoomFilter)) {
            list.addAll(world.getAllEnemies());
        } else if ("Вільні скелети".equals(selectedRoomFilter)) {
            list.addAll(world.getFreeEnemies());
        } else {
            for (DungeonRoom room : world.getRooms()) {
                if (room.getName().equals(selectedRoomFilter)) {
                    list.addAll(room.getEnemies());
                    break;
                }
            }
        }

        if ("Ім'я".equals(selectedSortCriterion)) {
            list.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        } else if ("Здоров'я (HP)".equals(selectedSortCriterion)) {
            list.sort((e1, e2) -> Integer.compare(e2.getHP(), e1.getHP()));
        } else if ("Міцність зброї".equals(selectedSortCriterion)) {
            list.sort((e1, e2) -> Integer.compare(e2.getWeapon().getDurability(), e1.getWeapon().getDurability()));
        }

        enemyListView.getItems().clear();
        for (SkeletonWarrior enemy : list) {
            String typeName = "Воїн";
            if (enemy instanceof SkeletonMage) {
                typeName = "Маг";
            } else if (enemy instanceof SkeletonArcher) {
                typeName = "Лучник";
            }
            String roomName = (enemy.getOwnerRoomName() == null) ? "Вільний" : enemy.getOwnerRoomName();
            enemyListView.getItems().add(String.format("%s (%s) [HP: %d/%d, Зброя: %d%%] - %s",
                    enemy.getName(), typeName, enemy.getHP(), enemy.getMaxHP(),
                    enemy.getWeapon().getDurability(), roomName));
        }
    }

    private void showSearchDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Пошук мікрооб'єкта");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: #2e2e2e;");

        Label nameLabel = new Label("Шукати за ім'ям (частина імені):");
        nameLabel.setTextFill(Color.WHITE);
        TextField nameField = new TextField();

        Label hpLabel = new Label("Мінімальне здоров'я (HP):");
        hpLabel.setTextFill(Color.WHITE);
        TextField hpField = new TextField("0");

        Label typeLabel = new Label("Клас об'єкта:");
        typeLabel.setTextFill(Color.WHITE);
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Будь-який",
                "Скелет-воїн",
                "Скелет-лучник",
                "Скелет-маг"
        ));
        typeBox.setValue("Будь-який");

        Button searchBtn = new Button("Знайти");
        searchBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        Label resultLabel = new Label();
        resultLabel.setTextFill(Color.LIGHTGREEN);
        resultLabel.setWrapText(true);

        searchBtn.setOnAction(e -> {
            String searchName = nameField.getText().trim();
            int minHp = 0;
            try {
                minHp = Integer.parseInt(hpField.getText());
            } catch (NumberFormatException ex) {
                
            }

            String selectedType = typeBox.getValue();
            List<SkeletonWarrior> matches = new ArrayList<>();

            for (SkeletonWarrior enemy : world.getAllEnemies()) {
                boolean matchesName = searchName.isEmpty() || enemy.getName().toLowerCase().contains(searchName.toLowerCase());
                boolean matchesHp = enemy.getHP() >= minHp;
                boolean matchesType = "Будь-який".equals(selectedType);
                if (!matchesType) {
                    if ("Скелет-маг".equals(selectedType) && enemy instanceof SkeletonMage) {
                        matchesType = true;
                    } else if ("Скелет-лучник".equals(selectedType) && enemy instanceof SkeletonArcher && !(enemy instanceof SkeletonMage)) {
                        matchesType = true;
                    } else if ("Скелет-воїн".equals(selectedType) && !(enemy instanceof SkeletonArcher)) {
                        matchesType = true;
                    } else {
                        matchesType = false;
                    }
                }

                if (matchesName && matchesHp && matchesType) {
                    matches.add(enemy);
                }
            }

            if (matches.isEmpty()) {
                resultLabel.setText("Об'єктів не знайдено.");
                resultLabel.setTextFill(Color.RED);
            } else {
                StringBuilder sb = new StringBuilder("Знайдено об'єктів: " + matches.size() + "\n\n");
                for (SkeletonWarrior match : matches) {
                    String room = (match.getOwnerRoomName() == null) ? "не належить жодному макрооб'єкту" : "належить макрооб'єкту '" + match.getOwnerRoomName() + "'";
                    sb.append(String.format("• %s:\n  Координати: X=%d, Y=%d\n  Приналежність: %s\n\n",
                            match.getName(), match.getX(), match.getY(), room));
                }
                resultLabel.setText(sb.toString());
                resultLabel.setTextFill(Color.LIGHTGREEN);
            }
        });

        vbox.getChildren().addAll(
                nameLabel, nameField,
                hpLabel, hpField,
                typeLabel, typeBox,
                searchBtn,
                resultLabel
        );

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2e2e2e; -fx-border-color: #2e2e2e;");

        Scene dialogScene = new Scene(scrollPane, 400, 450);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void updateSimulation() {
        if (world == null) return;

        List<SkeletonWarrior> allEnemies = world.getAllEnemies();

        
        for (SkeletonWarrior enemy : allEnemies) {
            enemy.updateAutomaticBehavior(world, player, moveMode);
        }

        
        List<DungeonRoom> rooms = world.getRooms();

        for (SkeletonWarrior enemy : allEnemies) {
            DungeonRoom currentRoom = null;
            for (DungeonRoom room : rooms) {
                if (enemy.getX() >= room.getX() && enemy.getX() <= room.getX() + room.getWidth()) {
                    currentRoom = room;
                    break;
                }
            }

            String targetRoomName = (currentRoom == null) ? null : currentRoom.getName();
            String currentOwner = enemy.getOwnerRoomName();

            if (!Objects.equals(currentOwner, targetRoomName)) {
                if (currentOwner != null) {
                    for (DungeonRoom r : rooms) {
                        if (r.getName().equals(currentOwner)) {
                            r.getEnemies().remove(enemy);
                            break;
                        }
                    }
                } else {
                    world.getFreeEnemies().remove(enemy);
                }

                if (targetRoomName != null) {
                    currentRoom.addEnemy(enemy);
                    System.out.println(enemy.getName() + " зайшов у кімнату " + targetRoomName);
                    enemy.setInteractionText("Зайшов!", 15);
                } else {
                    world.addFreeEnemy(enemy);
                    System.out.println(enemy.getName() + " вийшов з кімнати " + currentOwner);
                    enemy.setInteractionText("Вийшов!", 15);
                }
            }
        }

        
        allEnemies = world.getAllEnemies();
        for (int i = 0; i < allEnemies.size(); i++) {
            SkeletonWarrior e1 = allEnemies.get(i);
            for (int j = i + 1; j < allEnemies.size(); j++) {
                SkeletonWarrior e2 = allEnemies.get(j);

                double dx = e1.getX() - e2.getX();
                double dy = e1.getY() - e2.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < 60) {
                    e1.interactWith(e2);
                    e2.interactWith(e1);
                }
            }
        }

        updateUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}