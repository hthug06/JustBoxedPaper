package fr.ht06.justBoxed.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.World.CreateWorld;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;

public class BoxedCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("boxed")
                .then(Commands.literal("create")
                        .then(Commands.argument("box name", ArgumentTypes.signedMessage())
                                .executes(BoxedCommand::createBox))
                )
                .then(Commands.literal("test")
                        .then(Commands.argument("box name", ArgumentTypes.signedMessage())
                                .executes(BoxedCommand::test)));
    }

    private static int test(CommandContext<CommandSourceStack> ctx) {
        final SignedMessageResolver boxName = ctx.getArgument("box name", SignedMessageResolver.class);
        CommandSender sender = ctx.getSource().getSender(); // Retrieve the command sender
        Entity executor = ctx.getSource().getExecutor(); // Retrieve the command executor, which may or may not be the same as the sender

        if (!(executor instanceof Player player)) {
            sender.sendPlainMessage("Only players can create a box!");
            return Command.SINGLE_SUCCESS;
        }

//        player.teleportAsync(new Location(Bukkit.getWorld("boxed_world" + boxName.content()), 0, 100,0));
        Arrays.stream(new File(Bukkit.getWorldContainer() + "/boxed_world/").listFiles()).forEach(path -> {
            player.sendPlainMessage(path.getName());
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int createBox(CommandContext<CommandSourceStack> ctx) {
        final SignedMessageResolver boxName = ctx.getArgument("box name", SignedMessageResolver.class);
        CommandSender sender = ctx.getSource().getSender(); // Retrieve the command sender
        Entity executor = ctx.getSource().getExecutor(); // Retrieve the command executor, which may or may not be the same as the sender

        if (!(executor instanceof Player player)) {
            sender.sendPlainMessage("Only players can create a box!");
            return Command.SINGLE_SUCCESS;
        }


        if (sender != executor) {
            // If the command was executed by a different sender (Like using /execute)
            player.sendPlainMessage("A player need to create a box on his own");
            return Command.SINGLE_SUCCESS;
        }

        // If the player executed the command themselves
        player.sendPlainMessage("Creating a box for you...");

        new BukkitRunnable() {
            final CreateWorld createWorld = new CreateWorld(boxName.content(), player);

            @Override
            public void run() {
//                player.sendActionBar(Component.text("Creating a box...", NamedTextColor.GOLD));
                if (createWorld.isFinished()) {
                    cancel();
//                    player.sendPlainMessage("Finished Creating a box");
                    Location location = createWorld.getLocation();
                    double x = location.x();
                    double z = location.z();
                    //later, iter -64 - 320 to find the log block and then re-iter to the highest block (to not spawn on top of a mountain)
                    Location finalLoc = new Location(Bukkit.getWorld("boxed_world/" + boxName.content()), location.x(), Bukkit.getWorld("boxed_world/" + boxName.content()).getHighestBlockYAt((int) x, (int) z), z);
                    player.teleportAsync(finalLoc);
                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), 0L, 20L);

        return Command.SINGLE_SUCCESS;
    }

}
