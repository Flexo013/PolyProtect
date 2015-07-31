/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.flexo.polyprotect.utils;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import me.flexo.polyprotect.PolyProtect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Rik Schaaf aka CC007 (http://coolcat007.nl/)
 */
public class PolyProtectUtils {

    private static final Map<String, ProtectedRegion> selectedRegionMap = new HashMap<>();

    public static int newProtectionNumber(Player owner, List<String> worldNames) {
        int j = 1;
        while (true) {
            boolean regionFound = false;
            for (String worldName : worldNames) {
                RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(worldName));
                if (rgm.hasRegion(owner.getName() + "_" + j)) {
                    regionFound = true;
                    break;
                }
            }
            if (regionFound) {
                j++;
            } else {
                return j;
            }
        }
    }

    public static int countProtections(Player owner, List<String> worldNames) {
        int j = 1;
        for (int i = 0; i < 10; i++) {//TODO change max protections to config value
            for (String worldName : worldNames) {
                RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(worldName));
                if (rgm.hasRegion(owner.getName() + "_" + j)) {
                    j++;
                    break;
                }
            }
        }
        return j;
    }

    public static void createProtection(WorldType worldType, Player player, Player owner, int newRegionNumber) {

        if (!owner.hasPermission("pgc.tag.member") && newRegionNumber > 0) {
            player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
            return;
        }
        switch (worldType) {
            case CREATIVE:
                if (!owner.hasPermission("pgc.tag.builder") && newRegionNumber > 2) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.craftsman") && newRegionNumber > 4) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.designer") && newRegionNumber > 6) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.architect") && newRegionNumber > 8) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
            case SURVIVAL:
                if (!owner.hasPermission("pgc.tag.resident") && newRegionNumber > 2) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.wealthy") && newRegionNumber > 4) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.millionair") && newRegionNumber > 6) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!owner.hasPermission("pgc.tag.royalty") && newRegionNumber > 8) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
        }
        if (!owner.isOp() && newRegionNumber > 10) {
            player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
            return;
        }
        player.performCommand("/region define " + owner.getName() + "_" + newRegionNumber + " " + owner.getName());
        String worldTypeName = "";
        switch (worldType) {
            case CREATIVE:
                worldTypeName = "creative";
                break;
            case SURVIVAL:
                worldTypeName = "survival";
                break;
        }
        Bukkit.getLogger().log(Level.INFO, "{0} just created a new {1} protection for {2}.", new Object[]{player.getName(), worldTypeName, owner.getName()});
    }

    public static void selectProtection(Player player, boolean notify) {
        RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(player.getWorld().getName()));
        ApplicableRegionSet ars = rgm.getApplicableRegions(player.getLocation());
        if (ars.size() == 0) {
            player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You are not within a protection!");
            return;
        }

        if (ars.size() >= 2) {
            player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You are standing in intersecting protection!");
            return;
        }

        ProtectedRegion ar = ars.iterator().next();
        selectedRegionMap.put(player.getName(), ar);
        if (notify) {
            player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.GREEN + "You selected the protection: " + ar.getId());
        }
    }

    public static void removeProtection(Player player) {
        ProtectedRegion protection = selectedRegionMap.get(player.getName());
        if (protection == null) {
            player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You didn't select a region!");
            return;
        }
        player.performCommand("/region remove " + protection.getId());
        selectedRegionMap.remove(player.getName());
        player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.GREEN + "The selected region has been removed.");
    }

    public static void sendProtectionInfo(Player player) {
        sendProtectionInfo(player, selectedRegionMap.get(player.getName()).getId());
    }

    public static void sendProtectionInfo(Player player, String protectionId) {
        player.performCommand("/region info " + protectionId);
    }

    public static void addMemberToProtection(Player player, String newMemberName) {
        RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(player.getWorld().getName()));
        selectedRegionMap.get(player.getName()).getMembers().addPlayer(newMemberName);
        try {
            rgm.save();
        } catch (StorageException ex) {
             Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
        player.sendMessage(PolyProtect.pluginChatPrefix(true) + "The player has been added to the protection.");
    }

    public static void removeMemberFromProtection(Player player, String memberToRemove) {
        RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(player.getWorld().getName()));
        selectedRegionMap.get(player.getName()).getMembers().removePlayer(memberToRemove);
        try {
            rgm.save();
        } catch (StorageException ex) {
             Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
        player.sendMessage(PolyProtect.pluginChatPrefix(true) + "The player has been removed from the protection.");
    }
}
