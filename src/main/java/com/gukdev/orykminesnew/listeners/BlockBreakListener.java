package com.gukdev.orykminesnew.listeners;

import com.gukdev.orykminesnew.OrykMinesNew;
import com.gukdev.orykminesnew.MineRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final OrykMinesNew plugin;

    public BlockBreakListener(OrykMinesNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        MineRegion mineRegion = plugin.getMineRegion(block.getLocation());
        if (mineRegion != null) {
            event.setCancelled(true);
            block.setType(Material.COBBLESTONE);
            Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.STONE), 15 * 20L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.DIAMOND_ORE), 45 * 20L);
        }
    }
}
