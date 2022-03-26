package me.dthb.is;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class InventorySnapshots extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getCommandMap().register("is", new InventoriesCommand());
        getServer().getPluginManager().registerEvents(new InventoriesListener(), this);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventory inventory = player.getInventory();

            if (!inventory.isEmpty())
                new InventorySnapshot(player, Component.text("Server closed"));
        }
    }

}
