package com.gukdev.orykminesnew.listeners;

import com.gukdev.orykminesnew.MineRegion;
import com.gukdev.orykminesnew.OrykMinesNew;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private final OrykMinesNew plugin;
    private final Random random = new Random();
    private static final Map<Location, BlockData> regeneratingBlocks = new HashMap<>();

    public BlockBreakListener(OrykMinesNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Ignore block breaks by admins in creative mode
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE && player.hasPermission("orykmines.admin")) {
            return;
        }

        MineRegion mineRegion = plugin.getMineRegion(block.getLocation());
        if (mineRegion != null) {
            if (mineRegion.getMineableBlocks().contains(block.getType())) {
                event.setCancelled(true);
                dropItems(block.getLocation(), player, block.getType(), mineRegion.getDropsForBlock(block.getType()));
                block.setType(Material.COBBLESTONE);
                int regenTime = mineRegion.getRandomRegenTime();
                long regenEndTime = System.currentTimeMillis() + regenTime * 1000L;
                regeneratingBlocks.put(block.getLocation(), new BlockData(block.getType(), regenEndTime));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Material newBlock = mineRegion.getRandomBlock();
                    block.setType(newBlock);
                    regeneratingBlocks.remove(block.getLocation());
                }, regenTime * 20L);
            }
        }
    }

    private void dropItems(Location blockLocation, Player player, Material blockType, Map<Material, Double> drops) {
        Vector direction = player.getLocation().toVector().subtract(blockLocation.toVector()).normalize();
        for (Map.Entry<Material, Double> entry : drops.entrySet()) {
            if (random.nextDouble() <= entry.getValue()) {
                // Calculate drop location near the block, biased towards the player
                Location dropLocation = blockLocation.clone().add(direction.multiply(0.5 + random.nextDouble() * 0.5));
                blockLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(entry.getKey()));
            }
        }
    }

    public static Map<Location, BlockData> getRegeneratingBlocks() {
        return regeneratingBlocks;
    }

    public static class BlockData {
        private final Material originalBlock;
        private final long regenTime;

        public BlockData(Material originalBlock, long regenTime) {
            this.originalBlock = originalBlock;
            this.regenTime = regenTime;
        }

        public Material getOriginalBlock() {
            return originalBlock;
        }

        public long getRegenTime() {
            return regenTime;
        }
    }
}
