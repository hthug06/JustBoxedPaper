package fr.ht06.justBoxed.BoxedWorld;

import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoxedWorldManager {

    private List<BoxedWorld> boxes = new ArrayList<>();

    public BoxedWorldManager() {}

    public void addBox(BoxedWorld box) {
        boxes.add(box);
    }

    public void removeBox(BoxedWorld box) {
        boxes.remove(box);
    }

    public List<BoxedWorld> getBoxes() {
        return boxes;
    }

    public BoxedWorld getBoxByName(String name) {
        for (BoxedWorld box : boxes) {
            if (Arrays.stream(new File(Bukkit.getWorldContainer().toString() + "/boxed_world/").listFiles()).map(File::getName).anyMatch(name::equals)) {
                return box;
            }
        }
        return null;
    }

    public void loadBox(BoxedWorld box, CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    new WorldCreator(box.getWorldName()).createWorld();
                }catch (Exception ignored) {}
                sender.sendMessage(Component.text(box.getWorldName() + " loaded"));
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());


    }

    public void unloadBox(BoxedWorld box) {
        World toUnload = Bukkit.getWorld(box.getWorldName());
        if (toUnload == null) {
            boxes.removeIf(box1 -> boxes.contains(box1));
            return;
        }
        //define in the config where to send, for now send it to the basic world spawn
        for (Player player : toUnload.getPlayers()) {
            player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
        }

        Bukkit.unloadWorld(toUnload, true);
    }

}
