package fr.ht06.justBoxed.World;

import fr.ht06.justBoxed.BoxedWorld.BoxedWorld;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.StructureSearchResult;

import java.util.Random;

public class CreateWorld {
    //Create world -> find stronghold -> find biome with log -> find log

    private final Random rand = new Random();
    String worldName;
    Player player;
    boolean structureFinded = false;
    boolean biomeFinded = false;
    boolean logFinded = false;
    boolean somethingHappen = false;
    private int needRestart = 0;
    private int lastNeedRestart = 1;
    private Location structureLocation = null;
    private Location biomeLocation = null;
    private Location logLocation = null;

    /// ## Create a boxed world, with different step
    /// - Create the world (async)
    /// - Search for a stronghold (so the player can finish the game if he want)
    /// - Search for the closest biome having trees
    /// - Search for a tree -> leave -> log <br>
    /// ### You now have the world with good coordinates that can be finished :)
    public CreateWorld(String worldNameBefore, Player player) {
        this.worldName = "boxed_world/" + worldNameBefore;
        this.player = player;

        new BukkitRunnable() {

            @Override
            public void run() {

                //queue like system
                if (somethingHappen) {
                    return;
                }

                //if something fail, restart with other parameter
                if (needRestart >= lastNeedRestart) {
                    structureFinded = false;
                    biomeFinded = false;
                    logFinded = false;

                    structureLocation = null;
                    biomeLocation = null;
                    logLocation = null;

                    lastNeedRestart += 1;
                }

                //Create the world
                if (Bukkit.getWorld(worldName) == null) {
                    Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Creating World", NamedTextColor.GOLD));
                    somethingHappen = true;
                    createWorld();
                    return;
                }



                //Launch the structure search (need strong hold so the player can finish the game)
                if (Bukkit.getWorld(worldName) != null && !structureFinded) {
                    Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Finding secret thing!", NamedTextColor.GOLD));
                    somethingHappen = true;
                    findStronghold();
                    return;
                }

                //Structure launch the Biome search
                if (structureFinded && !biomeFinded) {
                    Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Searching a good biome", NamedTextColor.GOLD));
                    somethingHappen = true;
                    findBiome();
                    return;
                }


                //search a log
                if (biomeFinded && !logFinded) {
                    Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Searching a good place for you to spawn", NamedTextColor.GOLD));
                    //if the biome containing wood is find, we can now search for a log
                    somethingHappen = true;
                    findLog();
                    return;
                }

                Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Everything done! Teleporting!", NamedTextColor.DARK_GREEN));

                //Creating internal thing
                JustBoxed.boxedWorldManager.addBox(new BoxedWorld(worldName));

                cancel();

            }
        }.runTaskTimer(JustBoxed.getInstance(), 0, 20);
    }

    public boolean isFinished() {
        return logLocation != null;
    }

    public Location getLocation() {
        return logLocation;
    }

    private void createWorld() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    new WorldCreator(worldName)
                            .generateStructures(true)
                            .createWorld();
                } catch (IllegalStateException ignored) {
                } //Because bukkit don't like async but it work :)

                cancel();
                somethingHappen = false;
                Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("World created", NamedTextColor.GREEN));
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());
    }

    private void findStronghold() {
        //This can never be null, because 'The first ring has 3 strongholds within 1,280–2,816 blocks of the origin (of the world).'
        World world = Bukkit.getWorld(worldName);
        StructureSearchResult structureSearchResult = world.locateNearestStructure(world.getSpawnLocation().add(3000 * needRestart, 0, 3000 * needRestart)
                , StructureType.STRONGHOLD, 2816, false);

        if (structureSearchResult == null) {
            needRestart += 1;
        } else {
            structureLocation = structureSearchResult.getLocation();
            structureFinded = true;
        }
        Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Secret thing found", NamedTextColor.GREEN));

        somethingHappen = false;
    }

    private void findBiome() {
        new BukkitRunnable() {
            final World world = Bukkit.getWorld(worldName);

            @Override
            public void run() {

                //all the biome we search for
                BiomeSearchResult biomeSearch = world.locateNearestBiome(structureLocation.add(rand.nextInt(-250, 250), 0, rand.nextInt(-250, 250)),
                        1000,
                        Biome.BIRCH_FOREST, Biome.CHERRY_GROVE, Biome.DARK_FOREST, Biome.FLOWER_FOREST,
                        Biome.FOREST, Biome.JUNGLE, Biome.OLD_GROWTH_BIRCH_FOREST, Biome.OLD_GROWTH_SPRUCE_TAIGA,
                        Biome.OLD_GROWTH_SPRUCE_TAIGA/*, Biome.PALE_GARDEN, Biome.TAIGA*/, Biome.OLD_GROWTH_PINE_TAIGA);

                //if didn't find a biome in a 20000 block radius (yes, it's possible...), restart everything
                if (biomeSearch == null) {
                    needRestart += 1;
                    cancel();
                    return;
                }
                Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("Biome Found", NamedTextColor.GREEN));


                //else, this step is finished
                biomeLocation = biomeSearch.getLocation();

                biomeFinded = true;
                //Async for less / no lag
                somethingHappen = false;
                cancel();
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());
    }

    private void findLog() {
        //Leaves spawn in more quantity than log, so find leave then log is easier and more accurate
        final Location[] leaves = {null};


        new BukkitRunnable() {
            final World world = Bukkit.getWorld(worldName);

            @Override
            public void run() {

                for (int x = biomeLocation.getBlockX() - 100; x <= biomeLocation.getBlockX() + 100; x++) {
                    for (int z = biomeLocation.getBlockZ() - 100; z <= biomeLocation.getBlockZ() + 100; z++) {
                        Location currentLocation = new Location(world, x, world.getHighestBlockYAt(x, z) - 1, z);

                        if (world.getBlockAt(currentLocation).getType() != Material.WATER ||
                                world.getBlockAt(currentLocation).getType() != Material.AIR) {
                            if (world.getBlockAt(currentLocation).getType().name().toUpperCase().contains("LEAVE")
                                    || world.getBlockAt(currentLocation).getType().name().toUpperCase().contains("LOG")) {
                                leaves[0] = currentLocation;
                                cancel();
                                return;
                            }
                        }
                    }
                }
                needRestart += 1;
                cancel();
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());

        Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("This place look nice for you, but we need further investigation...", NamedTextColor.YELLOW));


        //restart everything...
        if (needRestart == lastNeedRestart) {
            somethingHappen = false;
            return;
        }

        //Now find a log
        //check every second if a leave or a log is find, then start searching
        new BukkitRunnable() {
            final World world = Bukkit.getWorld(worldName);

            @Override
            public void run() {

                if (leaves[0] == null) {
                    cancel();
                    return;
                }


                for (int y = 0; y < 5; y++) {
                    for (int x = -5; x < 5; x++) {
                        for (int z = -5; z < 5; z++) {
                            Location currentLocation = leaves[0].clone().add(x, y, z);

                            if (world.getBlockAt(currentLocation).getType() != Material.AIR) {
                                if (world.getBlockAt(currentLocation).getType().name().toUpperCase().contains("LOG")) {
                                    logLocation = currentLocation;
                                    logFinded = true;
                                    Bukkit.getPlayer(player.getUniqueId()).sendActionBar(Component.text("This place is definitely nice for you !", NamedTextColor.GREEN));


                                    cancel();
                                    return;
                                }
                            }
                        }
                    }
                }

                if (logLocation == null) {
                    needRestart += 1;
                } else {
                    logFinded = true;
                }

                cancel();

            }
        }.runTaskTimerAsynchronously(JustBoxed.getInstance(), 20, 20);
        somethingHappen = false;
    }
}
