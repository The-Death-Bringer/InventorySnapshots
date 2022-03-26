package me.dthb.is.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import me.dthb.is.InventorySnapshot;
import me.dthb.is.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class InventoriesInv implements InventoryProvider {

    public static SmartInventory INVENTORY(Player player, List<InventorySnapshot> snapshots) {
        return SmartInventory.builder()
                .id("inventorySnapshots")
                .provider(new InventoriesInv(player, snapshots))
                .size(6, 9)
                .title("Snapshots of " + player.getName())
                .build();
    }

    private final List<InventorySnapshot> snapshots;
    private final Player target;

    private InventoriesInv(Player target, List<InventorySnapshot> snapshots) {
        this.snapshots = snapshots;
        this.target = target;
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        Pagination pagination = contents.pagination();
        ClickableItem[] items = new ClickableItem[snapshots.size()];

        snapshots.sort(Comparator.comparingLong(InventorySnapshot::timestamp).reversed());

        for (int i = 0; i < snapshots.size(); i++) {
            InventorySnapshot snapshot = snapshots.get(i);
            items[i] = snapshot.asDisplay()
                    .lore("Clic para recuperar el inventario", "Clic derecho para ver el inventario")
                    .asClickable(e -> {
                        if (!target.isOnline()) {
                            player.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            PreviewInv.INVENTORY(snapshot).open(player);
                        } else {
                            snapshot.restoreTo(target);
                            player.sendMessage(Component.text("Inventory restored", NamedTextColor.GREEN));
                        }
                    });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(45);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));

        contents.set(5, 4, ItemBuilder.start(Material.BARRIER).name("Return")
                .asClickable(e -> {
                    player.closeInventory();
                    player.performCommand("inv");
                }));

        if (!pagination.isFirst())
            contents.set(5, 1,
                    ItemBuilder.start(Material.ARROW)
                            .name("Prev")
                            .asClickable(e -> INVENTORY(target, snapshots)
                                    .open(player, pagination.previous().getPage())));

        if (!pagination.isLast())
            contents.set(5, 7, ItemBuilder.start(Material.ARROW)
                    .name("Next")
                    .asClickable(e -> INVENTORY(target, snapshots)
                            .open(player, pagination.next().getPage())));

    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
