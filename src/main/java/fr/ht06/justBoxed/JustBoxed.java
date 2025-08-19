package fr.ht06.justBoxed;

import fr.ht06.justBoxed.BoxedWorld.BoxedWorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustBoxed extends JavaPlugin {

    public static BoxedWorldManager boxedWorldManager = new BoxedWorldManager();

    public static JustBoxed getInstance() {
        return getPlugin(JustBoxed.class);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
