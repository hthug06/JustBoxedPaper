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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BoxedCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("boxed")
                .then(Commands.literal("create")
                        .then(Commands.argument("box name", ArgumentTypes.signedMessage())
                                .executes(BoxedCommand::createBox))
                );
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
            final CreateWorld createWorld = new CreateWorld(boxName.content());

            @Override
            public void run() {
                player.sendActionBar(Component.text("Creating a box...", NamedTextColor.GOLD));
                if (createWorld.isFinished()) {
                    cancel();
                    player.sendPlainMessage("Finished Creating a box");
                    player.teleportAsync(createWorld.getLocation());
                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), 0L, 20L);

        return Command.SINGLE_SUCCESS;
    }

}
