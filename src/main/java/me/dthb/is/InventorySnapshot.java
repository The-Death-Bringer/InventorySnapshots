package me.dthb.is;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.google.common.collect.ObjectArrays;
import me.dthb.is.utils.ExpUtils;
import me.dthb.is.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

public class InventorySnapshot {

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Component reason;
    private final String location;
    private final long timestamp;
    private final UUID owner;
    private final int exp;
    private final long id;

    public InventorySnapshot(Player player, Component component) {
        PlayerInventory playerInventory = player.getInventory();
        Location loc = player.getLocation();
        String format = "%s, %,.2f, %,.2f, %,.2f";

        inventory = ObjectArrays.concat(playerInventory.getStorageContents(), playerInventory.getExtraContents(), ItemStack.class);
        location = String.format(format, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        armor = playerInventory.getArmorContents();
        exp = ExpUtils.getTotalExperience(player);
        timestamp = System.currentTimeMillis();
        owner = player.getUniqueId();
        reason = component;

        long id1;
        try {
            id1 = DB.executeInsert("INSERT INTO inventory.Snapshots (who, deathTime, reason, location, inventory, armor, exp) VALUES (?, ?, ?, ?, ?, ?, ?);",
                    owner.toString(),
                    timestamp,
                    GsonComponentSerializer.gson().serialize(reason),
                    location,
                    InventorySave.toBase64(inventory),
                    InventorySave.toBase64(armor),
                    exp);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            id1 = -1;
        }
        id = id1;
    }

    public InventorySnapshot(DbRow row) {
        reason = GsonComponentSerializer.gson().deserialize(row.getString("reason"));
        inventory = InventorySave.fromBase64(row.getString("inventory"));
        armor = InventorySave.fromBase64(row.getString("armor"));
        owner = UUID.fromString(row.getString("who"));
        location = row.getString("location");
        timestamp = row.getLong("deathTime");
        exp = row.getInt("exp");
        id = row.getInt("id");
    }

    public ItemStack[] inventory() {
        return inventory;
    }

    public ItemStack[] armor() {
        return armor;
    }

    public long timestamp() {
        return timestamp;
    }

    public int totalExp() {
        return exp;
    }

    public long id() {
        return id;
    }

    public void restoreTo(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        if(inventory.length == 37) {
            ItemStack[] storage = Arrays.copyOfRange(inventory, 0, 36);
            ItemStack[] offHand = Arrays.copyOfRange(inventory, 36, 37);
            playerInventory.setStorageContents(storage);
            playerInventory.setExtraContents(offHand);
        } else {
            playerInventory.setStorageContents(inventory);
        }

        ExpUtils.setTotalExperience(player, exp);
        playerInventory.setArmorContents(armor);

        Component msg = Component.text("Your inventory and experience have been restored", NamedTextColor.GREEN);
        player.sendMessage(msg);

    }

    public ItemBuilder asDisplay() {
        LocalDateTime ldt = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("America/Los_Angeles")).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        String date = formatter.format(ldt);
        return ItemBuilder.start(Material.CHEST)
                .name("")
                .lore("<gray>Date: <gold>" + date)
                .lore(Component.text("Reason: ", NamedTextColor.GRAY).append(reason))
                .lore("<gray>Location: <gold>" + location)
                .lore("<gray>Experience: <gold>" + exp);
    }

}
