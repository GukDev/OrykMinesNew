package com.gukdev.orykminesnew;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.gukdev.orykminesnew.listeners.BlockBreakListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OrykMinesNew extends JavaPlugin implements CommandExecutor {

    private final Map<String, MineRegion> mineRegions = new HashMap<>();
    private FileConfiguration config;
    private File regenStateFile;
    private FileConfiguration regenState;

    @Override
    public void onEnable() {
        getLogger().info("OrykMinesNew has been enabled!");
        saveDefaultConfig();
        config = getConfig();
        regenStateFile = new File(getDataFolder(), "regen_state.yml");
        regenState = YamlConfiguration.loadConfiguration(regenStateFile);
        loadMineRegions();
        loadRegenState();
        // Register commands
        getCommand("orykmines").setExecutor(this);
        // Register event listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("OrykMinesNew has been disabled!");
        saveRegenState();
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    private void loadMineRegions() {
        mineRegions.clear();
        for (String mineName : config.getConfigurationSection("mines").getKeys(false)) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(getServer().getWorlds().get(0)));
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(mineName);
                if (region != null) {
                    MineRegion mineRegion = new MineRegion(region);
                    mineRegion.loadSettings(config.getConfigurationSection("mines." + mineName));
                    mineRegions.put(mineName, mineRegion);
                }
            }
        }
    }

    private void loadRegenState() {
        if (regenStateFile.exists()) {
            for (String key : regenState.getKeys(false)) {
                Location loc = stringToLocation(key);
                if (loc != null && regenState.isConfigurationSection(key)) {
                    ConfigurationSection section = regenState.getConfigurationSection(key);
                    if (section != null) {
                        Material originalBlock = Material.valueOf(section.getString("original_block"));
                        long regenTime = section.getLong("regen_time");
                        scheduleBlockRegeneration(loc.getBlock(), originalBlock, regenTime - System.currentTimeMillis());
                    }
                }
            }
        }
    }

    private void saveRegenState() {
        regenState.getKeys(false).forEach(key -> regenState.set(key, null)); // Clear the current state
        for (Map.Entry<Location, BlockBreakListener.BlockData> entry : BlockBreakListener.getRegeneratingBlocks().entrySet()) {
            Location loc = entry.getKey();
            BlockBreakListener.BlockData blockData = entry.getValue();
            regenState.set(locationToString(loc) + ".original_block", blockData.getOriginalBlock().name());
            regenState.set(locationToString(loc) + ".regen_time", blockData.getRegenTime());
        }
        try {
            regenState.save(regenStateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scheduleBlockRegeneration(Block block, Material originalBlock, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(originalBlock);
                BlockBreakListener.getRegeneratingBlocks().remove(block.getLocation());
            }
        }.runTaskLater(this, delay / 50L); // Convert milliseconds to ticks
    }

    public MineRegion getMineRegion(Location location) {
        for (MineRegion mineRegion : mineRegions.values()) {
            if (mineRegion.getRegion().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                return mineRegion;
            }
        }
        return null;
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length == 4) {
            return new Location(
                    Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("orykmines")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /orykmines <command>");
                return true;
            }

            if (args[0].equalsIgnoreCase("create")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length < 2) {
                        player.sendMessage("Usage: /orykmines create <regionName>");
                        return true;
                    }
                    String regionName = args[1];
                    createMineRegion(player, regionName);
                    return true;
                } else {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length < 2) {
                        player.sendMessage("Usage: /orykmines edit <regionName>");
                        return true;
                    }
                    String regionName = args[1];
                    MineRegion mineRegion = mineRegions.get(regionName);
                    if (mineRegion != null) {
                        // Placeholder for GUI logic
                    } else {
                        player.sendMessage(config.getString("messages.mine_region_not_exist").replace("%region_name%", regionName));
                    }
                    return true;
                } else {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                config = getConfig();
                loadMineRegions();
                sender.sendMessage("OrykMinesNew configuration reloaded.");
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                listMineRegions(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /orykmines status <regionName>");
                    return true;
                }
                String regionName = args[1];
                getMineStatus(sender, regionName);
                return true;
            }
        }
        return false;
    }

    private void createMineRegion(Player player, String regionName) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                MineRegion mineRegion = new MineRegion(region);
                mineRegion.loadSettings(config.getConfigurationSection("mines." + regionName));
                mineRegions.put(regionName, mineRegion);
                player.sendMessage(config.getString("messages.region_created").replace("%region_name%", regionName));
            } else {
                player.sendMessage(config.getString("messages.region_not_exist").replace("%region_name%", regionName));
            }
        } else {
            player.sendMessage(config.getString("messages.region_manager_error"));
        }
    }

    private void listMineRegions(CommandSender sender) {
        if (mineRegions.isEmpty()) {
            sender.sendMessage(config.getString("messages.mine_list_empty"));
        } else {
            sender.sendMessage(config.getString("messages.mine_list_header"));
            for (String regionName : mineRegions.keySet()) {
                sender.sendMessage("- " + regionName);
            }
        }
    }

    private void getMineStatus(CommandSender sender, String regionName) {
        MineRegion mineRegion = mineRegions.get(regionName);
        if (mineRegion == null) {
            sender.sendMessage(config.getString("messages.mine_region_not_exist").replace("%region_name%", regionName));
            return;
        }

        sender.sendMessage(config.getString("messages.mine_status_header").replace("%region_name%", regionName));
        sender.sendMessage(config.getString("messages.mine_status_regenerating_blocks"));
        for (Map.Entry<Location, BlockBreakListener.BlockData> entry : BlockBreakListener.getRegeneratingBlocks().entrySet()) {
            Location loc = entry.getKey();
            if (mineRegion.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                sender.sendMessage("- " + loc + " (original block: " + entry.getValue().getOriginalBlock() + ")");
            }
        }
    }
}
