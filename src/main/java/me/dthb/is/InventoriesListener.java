package me.dthb.is;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

public class InventoriesListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory inventory = player.getInventory();

        if (!inventory.isEmpty())
            new InventorySnapshot(player, event.deathMessage());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        if (!inventory.isEmpty())
            new InventorySnapshot(event.getPlayer(), Component.text("Player quit"));
    }

}
