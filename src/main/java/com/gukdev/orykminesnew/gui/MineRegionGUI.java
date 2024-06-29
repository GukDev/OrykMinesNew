package com.gukdev.orykminesnew.gui;

import com.gukdev.orykminesnew.MineRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MineRegionGUI {

    public void openMineRegionMenu(Player player, MineRegion mineRegion) {
        Inventory inv = Bukkit.createInventory(null, 27, "Mine Region: " + mineRegion.getRegion().getId());

        // Example item
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Edit Mine Region");
            item.setItemMeta(meta);
        }
        inv.setItem(13, item);

        player.openInventory(inv);
    }
}
