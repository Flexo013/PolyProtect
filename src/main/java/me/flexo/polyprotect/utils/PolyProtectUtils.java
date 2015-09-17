/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.flexo.polyprotect.utils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.DomainInputResolver;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;
import com.sk89q.worldguard.util.profile.resolver.ProfileService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import me.flexo.polyprotect.PolyProtect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Rik Schaaf aka CC007 (http://coolcat007.nl/)
 */
public class PolyProtectUtils {

    private static final Map<String, ProtectedRegion> selectedRegionMap = new HashMap<>();

    public static int newProtectionNumber(String owner, List<String> worldNames) {
        int j = 1;
        while (true) {
            boolean regionFound = false;
            for (String worldName : worldNames) {
                World world = Bukkit.getServer().getWorld(worldName);
                if (world != null) {
                    RegionManager rgm = WGBukkit.getRegionManager(world);
                    if (rgm.hasRegion(owner + "_" + j)) {
                        regionFound = true;
                        break;
                    }
                }
            }
            if (regionFound) {
                j++;
            } else {
                return j;
            }
        }
    }

    public static int countProtections(String owner, List<String> worldNames) {
        int j = 0;
        for (int i = 0; i < 10; i++) {//TODO change max protections to config value
            for (String worldName : worldNames) {
                World world = Bukkit.getServer().getWorld(worldName);
                if (world != null) {
                    RegionManager rgm = WGBukkit.getRegionManager(world);
                    if (rgm.hasRegion(owner + "_" + i)) {
                        j++;
                        break;
                    }
                }
            }
        }
        return j;
    }

    public static void createProtection(WorldType worldType, Player player, OfflinePlayer owner, int newRegionNumber) {

        if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.member") && newRegionNumber > 0) {
            player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
            return;
        }
        switch (worldType) {
            case CREATIVE:
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.builder") && newRegionNumber > 2) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.craftsman") && newRegionNumber > 4) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.designer") && newRegionNumber > 6) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.architect") && newRegionNumber > 8) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
            case SURVIVAL:
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.resident") && newRegionNumber > 2) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.wealthy") && newRegionNumber > 4) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.millionair") && newRegionNumber > 6) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
                if (!PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.royalty") && newRegionNumber > 8) {
                    player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
                    return;
                }
        }
        if (!owner.isOp() && newRegionNumber > 10) {
            player.sendMessage("Could not create region: " + owner.getName() + " has reached the maximum amount of protections");
            return;
        }
        player.performCommand("region define " + owner.getName() + "_" + newRegionNumber + " " + owner.getName());
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
        player.performCommand("region remove " + protection.getId());
        selectedRegionMap.remove(player.getName());
        player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.GREEN + "The selected region has been removed.");
    }

    public static void sendProtectionInfo(Player player) {
        sendProtectionInfo(player, selectedRegionMap.get(player.getName()).getId());
    }

    public static void sendProtectionInfo(Player player, String protectionId) {
        player.performCommand("region info " + protectionId);
    }

    public static void addMemberToProtection(Player sender, String newMemberName) {
        RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(sender.getWorld().getName()));
        addMemberFromName(newMemberName, selectedRegionMap.get(sender.getName()), sender);
    }

    public static void removeMemberFromProtection(Player sender, String memberToRemove) {
        RegionManager rgm = WGBukkit.getRegionManager(Bukkit.getServer().getWorld(sender.getWorld().getName()));
        addMemberFromName(memberToRemove, selectedRegionMap.get(sender.getName()), sender);
        
    }

    public static void removeMemberFromName(String name, final ProtectedRegion region, final Player sender) {
        // Google's Guava library provides useful concurrency classes.
        // The following executor would be re-used in your plugin.
        ListeningExecutorService executor
                = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

        String[] input = new String[]{name};
        ProfileService profiles = WGBukkit.getPlugin().getProfileService();
        DomainInputResolver resolver = new DomainInputResolver(profiles, input);
        resolver.setLocatorPolicy(UserLocatorPolicy.UUID_AND_NAME);
        ListenableFuture<DefaultDomain> future = executor.submit(resolver);

        // Add a callback using Guava
        Futures.addCallback(future, new FutureCallback<DefaultDomain>() {
            @Override
            public void onSuccess(DefaultDomain result) {
                region.getMembers().removeAll(result);
                sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.GREEN + "The player has been removed from the protection.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Bukkit.getLogger().log(Level.WARNING, null, throwable);
                sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "Failed removing player from protection. Please contact an admin.");
            }
        });
    }
    
    public static void addMemberFromName(String name, final ProtectedRegion region, final Player sender) {
        // Google's Guava library provides useful concurrency classes.
        // The following executor would be re-used in your plugin.
        ListeningExecutorService executor
                = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

        String[] input = new String[]{name};
        ProfileService profiles = WGBukkit.getPlugin().getProfileService();
        DomainInputResolver resolver = new DomainInputResolver(profiles, input);
        resolver.setLocatorPolicy(UserLocatorPolicy.UUID_AND_NAME);
        ListenableFuture<DefaultDomain> future = executor.submit(resolver);

        // Add a callback using Guava
        Futures.addCallback(future, new FutureCallback<DefaultDomain>() {
            @Override
            public void onSuccess(DefaultDomain result) {
                region.getMembers().addAll(result);
                sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.GREEN + "The player has been added to the protection.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Bukkit.getLogger().log(Level.WARNING, null, throwable);
                sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "Failed adding player to protection. Please contact an admin.");
            }
        });
    }
    
    public static int getMaxProtectionCount(OfflinePlayer player, WorldType worldtype){
        if (player.isOp()) return -1;
        switch (worldtype) {
            case SURVIVAL:
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.royalty")) return 10;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.millionaire")) return 8;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.wealthy")) return 6;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.resident")) return 4;
                break;
            case CREATIVE:
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.architect")) return 10;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.designer")) return 8;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.craftsman")) return 6;
                if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.builder")) return 4;
                break;
            default:
                return -2;
        }
        if (PolyProtect.getPlugin().getPermission().playerHas(PolyProtect.survivalWorlds.get(0), player, "pgc.tag.member")) return 2;
        return 0;
    }
}
