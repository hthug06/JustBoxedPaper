package fr.ht06.justBoxed.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.ht06.justBoxed.BoxedWorld.BoxedWorld;
import fr.ht06.justBoxed.JustBoxed;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminBoxedCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("adminboxed")
                .then(Commands.literal("unload")
                        .then(Commands.argument("box name", ArgumentTypes.signedMessage())
                                .suggests((ctx, builder) -> {
                                    List<World> boxedWorld = Bukkit.getWorlds();
                                    boxedWorld.remove(Bukkit.getWorld("world"));
                                    boxedWorld.remove(Bukkit.getWorld("world_nether"));
                                    boxedWorld.remove(Bukkit.getWorld("world_the_end"));

                                    boxedWorld.stream()
                                            .map(world -> world.getName().replace("boxed_world/", ""))
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .executes(AdminBoxedCommand::unloadBox))
                )
                .then(Commands.literal("load")
                        .then(Commands.argument("box name", ArgumentTypes.signedMessage())
                                .suggests((ctx, builder) -> {
                                    List<String> unloadedWorlds = new ArrayList<>();
                                    //System.out.println(Arrays.toString(JustBoxed.getInstance().getDataFolder().getAbsolutePath()));
                                    for (File file : new File(Bukkit.getWorldContainer() + "/boxed_world/").listFiles()) {
                                        if (Bukkit.getWorld(file.getName()) == null) {
                                            unloadedWorlds.add(file.getName());
                                        }
                                    }

                                    unloadedWorlds.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(AdminBoxedCommand::loadBox))
                );
    }

    private static int loadBox(CommandContext<CommandSourceStack> ctx) {
        final SignedMessageResolver boxName = ctx.getArgument("box name", SignedMessageResolver.class);
        CommandSender sender = ctx.getSource().getSender(); // Retrieve the command sender

        // If the player executed the command themselves
        if (Arrays.stream(new File(Bukkit.getWorldContainer() + "/boxed_world/").listFiles()).map(File::getName).anyMatch(boxName.content()::equals)) {
            BoxedWorld box = new BoxedWorld("boxed_world/" + boxName.content());
            JustBoxed.boxedWorldManager.loadBox(box, sender);
            sender.sendMessage(Component.text(box.getWorldName() + " loaded"));
            JustBoxed.boxedWorldManager.addBox(box);

        } else {
            sender.sendMessage(Component.text("A box with this name don't exist"));
        }

        sender.sendMessage(Bukkit.getWorlds().toString());

        return Command.SINGLE_SUCCESS;
    }

    private static int unloadBox(CommandContext<CommandSourceStack> ctx) {
        final SignedMessageResolver boxName = ctx.getArgument("box name", SignedMessageResolver.class);
        CommandSender sender = ctx.getSource().getSender(); // Retrieve the command sender

        // If the player executed the command themselves
        BoxedWorld box = JustBoxed.boxedWorldManager.getBoxByName(boxName.content());
        if (box != null) {
            JustBoxed.boxedWorldManager.unloadBox(box);
            sender.sendMessage(Component.text(box.getWorldName() + " unloaded"));
        } else {
            sender.sendMessage(Component.text("A box with this name don't exist"));
        }

        sender.sendMessage(Bukkit.getWorlds().toString());


        return Command.SINGLE_SUCCESS;
    }

}
