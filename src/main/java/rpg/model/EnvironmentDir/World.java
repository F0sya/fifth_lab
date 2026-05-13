package rpg.model.EnvironmentDir;

import javafx.scene.Group;
import rpg.model.EnemyDir.SkeletonWarrior;

import java.util.ArrayList;
import java.util.List;

public class World {
    private List<DungeonRoom> rooms;
    private List<SkeletonWarrior> freeEnemies;

    public World() {
        rooms = new ArrayList<>();
        freeEnemies = new ArrayList<>();
    }

    public void addRoom(DungeonRoom room) { rooms.add(room); }
    public List<DungeonRoom> getRooms() { return rooms; }

    public void addFreeEnemy(SkeletonWarrior enemy) { freeEnemies.add(enemy); }
    public void removeFreeEnemy(SkeletonWarrior enemy) { freeEnemies.remove(enemy); }
    public List<SkeletonWarrior> getFreeEnemies() { return freeEnemies; }


    public List<SkeletonWarrior> getAllEnemies() {
        List<SkeletonWarrior> all = new ArrayList<>(freeEnemies);
        for (DungeonRoom room : rooms) {
            all.addAll(room.getEnemies());
        }
        return all;
    }


    public Group draw(boolean isDevMode) {
        Group worldGroup = new Group();


        for (DungeonRoom room : rooms) {
            worldGroup.getChildren().add(room.draw(isDevMode));
        }


        for (SkeletonWarrior enemy : freeEnemies) {
            worldGroup.getChildren().add(enemy.draw(isDevMode));
        }

        return worldGroup;
    }
}