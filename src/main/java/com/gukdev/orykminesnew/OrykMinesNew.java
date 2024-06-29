package com.gukdev.orykminesnew;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.gukdev.orykminesnew.gui.MineRegionGUI;
import com.gukdev.orykminesnew.listeners.BlockBreakListener;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class OrykMinesNew extends JavaPlugin {

    private final Map<String, MineRegion> mineRegions = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("OrykMinesNew has been enabled!");
        // Register commands
        getCommand("orykmines").setExecutor(this);
        // Register event listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("OrykMinesNew has been disabled!");
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
                        new MineRegionGUI().openMineRegionMenu(player, mineRegion);
                    } else {
                        player.sendMessage("Mine region " + regionName + " does not exist.");
                    }
                    return true;
                } else {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
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
                mineRegions.put(regionName, new MineRegion(region));
                player.sendMessage("Mine region " + regionName + " created successfully.");
            } else {
                player.sendMessage("Region " + regionName + " does not exist.");
            }
        } else {
            player.sendMessage("Could not retrieve WorldGuard region manager.");
        }
    }

    public MineRegion getMineRegion(Location location) {
        for (MineRegion mineRegion : mineRegions.values()) {
            if (mineRegion.getRegion().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                return mineRegion;
            }
        }
        return null;
    }
}
