package com.gukdev.orykminesnew;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MineRegion {
    private final ProtectedRegion region;
    private final Map<Material, BlockData> blockDataMap = new HashMap<>();
    private int minRegenTime;
    private int maxRegenTime;
    private final Random random = new Random();

    public MineRegion(ProtectedRegion region) {
        this.region = region;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public void loadSettings(ConfigurationSection section) {
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                Material material = Material.valueOf(key);
                double chance = blocksSection.getDouble(key + ".chance");
                Map<Material, Double> drops = new HashMap<>();
                ConfigurationSection dropsSection = blocksSection.getConfigurationSection(key + ".drops");
                if (dropsSection != null) {
                    for (String dropKey : dropsSection.getKeys(false)) {
                        Material dropMaterial = Material.valueOf(dropKey);
                        double dropChance = dropsSection.getDouble(dropKey + ".chance");
                        drops.put(dropMaterial, dropChance);
                    }
                }
                blockDataMap.put(material, new BlockData(chance, drops));
            }
        }
        minRegenTime = section.getInt("regeneration.min_time", 30);
        maxRegenTime = section.getInt("regeneration.max_time", 60);
    }

    public Material getRandomBlock() {
        double totalChance = blockDataMap.values().stream().mapToDouble(BlockData::getChance).sum();
        double roll = random.nextDouble() * totalChance;
        double cumulativeChance = 0.0;
        for (Map.Entry<Material, BlockData> entry : blockDataMap.entrySet()) {
            cumulativeChance += entry.getValue().getChance();
            if (roll <= cumulativeChance) {
                return entry.getKey();
            }
        }
        return Material.COBBLESTONE;
    }

    public Map<Material, Double> getDropsForBlock(Material material) {
        BlockData blockData = blockDataMap.get(material);
        return blockData != null ? blockData.getDrops() : new HashMap<>();
    }

    public int getRandomRegenTime() {
        return random.nextInt(maxRegenTime - minRegenTime + 1) + minRegenTime;
    }

    public Set<Material> getMineableBlocks() {
        return blockDataMap.keySet();
    }

    private static class BlockData {
        private final double chance;
        private final Map<Material, Double> drops;

        public BlockData(double chance, Map<Material, Double> drops) {
            this.chance = chance;
            this.drops = drops;
        }

        public double getChance() {
            return chance;
        }

        public Map<Material, Double> getDrops() {
            return drops;
        }
    }
}
