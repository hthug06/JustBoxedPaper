package fr.ht06.justBoxed;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustBoxed extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JustBoxed getInstance(){
        return getPlugin(JustBoxed.class);
    }
}
