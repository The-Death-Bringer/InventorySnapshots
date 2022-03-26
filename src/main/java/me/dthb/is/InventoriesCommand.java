package me.dthb.is;

import co.aikar.idb.DB;
import com.google.common.collect.Lists;
import me.dthb.is.gui.InventoriesInv;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InventoriesCommand extends Command {

    protected InventoriesCommand() {
        super("inventory", "Command to see inventory snapshots", "/inv", Lists.newArrayList("inv"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String string, @NotNull String[] args) {

        if (!sender.hasPermission("is.admin"))
            return false;

        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage(Component.text("Wrong usage", NamedTextColor.RED));
            return false;
        }

        if (args[0].equalsIgnoreCase("purge")) {
            sender.sendMessage(Component.text("Starting purge", NamedTextColor.GREEN));
            long limit = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
            DB.executeUpdateAsync("DELETE FROM inventory.Snapshots WHERE deathTime < " + limit + ";")
                    .thenAccept(total -> sender.sendMessage(Component.text("Done Purging", NamedTextColor.GREEN)))
                    .exceptionally(e -> {
                        sender.sendMessage(Component.text("Error purging", NamedTextColor.RED));
                        return null;
                    });
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if(target == null) {
            sender.sendMessage(Component.text("No player has been found", NamedTextColor.RED));
            return false;
        }

        sender.sendMessage(Component.text("Loading data...", NamedTextColor.GREEN));
        try {
            List<InventorySnapshot> snapshots = DB.getResults("SELECT * FROM inventory.Snapshots WHERE who = ?;", target.getUniqueId().toString())
                    .stream().map(InventorySnapshot::new).collect(Collectors.toList());

            InventoriesInv.INVENTORY(target, snapshots).open(player);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            sender.sendMessage(Component.text("There was an error loading the inventory data", NamedTextColor.RED));
        }

        return true;
    }

}
