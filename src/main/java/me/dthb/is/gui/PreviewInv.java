package me.dthb.is.gui;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.dthb.is.InventorySnapshot;
import me.dthb.is.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PreviewInv implements InventoryProvider {

    public static SmartInventory INVENTORY(InventorySnapshot snapshot) {
        return SmartInventory.builder()
                .id("previewInventory")
                .provider(new PreviewInv(snapshot))
                .size(6, 9)
                .title("Inventory snapshot")
                .build();
    }

    private final InventorySnapshot snapshot;

    public PreviewInv(InventorySnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemStack[] inventory = snapshot.inventory();
        ItemStack[] armor = snapshot.armor();

        contents.fill(ItemBuilder.start(Material.BLACK_STAINED_GLASS_PANE).name("<red>").asEmpty());

        int totalExp = snapshot.totalExp();

        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            int column = i % 9;
            ItemStack item = inventory[i];
            contents.set(row, column, item == null ? null : ItemBuilder.start(item).asEmpty());
        }

        for (int i = 0; i < 4; i++) {
            ItemStack item = armor[i];
            contents.set(5, i, item == null ? null : ItemBuilder.start(item).asEmpty());
        }

        contents.set(5, 5, ItemBuilder.start(Material.SHIELD).name("").lore("Off hand").asEmpty());
        contents.set(5, 7, ItemBuilder.start(Material.EXPERIENCE_BOTTLE).name("").lore("Exp: " + totalExp).asEmpty());
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
