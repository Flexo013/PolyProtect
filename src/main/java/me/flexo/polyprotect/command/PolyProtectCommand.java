/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.flexo.polyprotect.command;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexo.polyprotect.PolyProtect;
import me.flexo.polyprotect.utils.PolyProtectUtils;
import me.flexo.polyprotect.utils.WorldType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rik Schaaf aka CC007 (http://coolcat007.nl/)
 * @author Flexo013
 */
public class PolyProtectCommand implements CommandExecutor {

    private final PolyProtect plugin;

    public PolyProtectCommand(PolyProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length == 0) {
            return false;
        }
        Player player;
        OfflinePlayer owner;
        switch (args[0].toLowerCase()) {
            case "create":
            case "define":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "Go away Johnsole, nobody likes you.");
                    return true;
                }
                player = (Player) sender;

                if (!sender.hasPermission("pgc.prot.admin")) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You don't have permission to create protections!");
                    return true;
                }

                if (args.length != 2) {
                    return false;
                }
                owner = plugin.getServer().getOfflinePlayer(args[1]);

                if (!owner.hasPlayedBefore()) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + owner.getName() + " has never player before!");
                    return true;
                }

                if (PolyProtect.getCreativeWorlds().contains(player.getWorld().getName())) {
                    int newRegionNumber = PolyProtectUtils.newProtectionNumber(owner.getName(), PolyProtect.getCreativeWorlds());
                    PolyProtectUtils.createProtection(WorldType.CREATIVE, player, owner, newRegionNumber);
                } else if (PolyProtect.getSurvivalWorlds().contains(player.getWorld().getName())) {
                    int newRegionNumber = PolyProtectUtils.newProtectionNumber(owner.getName(), PolyProtect.getSurvivalWorlds());
                    PolyProtectUtils.createProtection(WorldType.SURVIVAL, player, owner, newRegionNumber);
                } else {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.YELLOW + "You cannot create protections in this world.");
                }
                return true;
            case "select":
            case "sel":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "Go away Johnsole, nobody likes you.");
                    return true;
                }
                player = (Player) sender;

                if (!player.hasPermission("pgc.prot.admin")) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You don't have permission to select protections!");
                    return false;
                }

                if (args.length != 1) {
                    return false;
                }

                PolyProtectUtils.selectProtection(player, true);
                return true;
            case "delete":
            case "del":
            case "remove":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "Go away Johnsole, nobody likes you.");
                    return true;
                }
                player = (Player) sender;

                if (!player.hasPermission("pgc.prot.admin")) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You don't have permission to remove protections!");
                    return true;
                }

                if (args.length != 1) {
                    return false;
                }
                PolyProtectUtils.removeProtection(player);
                return true;
            case "info":
            case "i":
                if (args.length != 1) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "Correct usage: /prot info!");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "You are not in a protection, Johnsole...");
                    return true;
                }
                player = (Player) sender;

                World world = Bukkit.getServer().getWorld(player.getWorld().getName());
                if (!PolyProtect.survivalWorlds.contains(world.getName()) && !PolyProtect.creativeWorlds.contains(world.getName())) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You cannot use PolyProtect in this world!");
                    return true;
                }

                RegionManager rgm = WGBukkit.getRegionManager(world);
                ApplicableRegionSet ars = rgm.getApplicableRegions(player.getLocation());
                if (ars.size() == 0) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You are not within a protection!");
                    return true;
                }

                if (ars.size() >= 2) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You are standing in intersecting protections!");
                    return true;
                }

                ProtectedRegion ar = ars.iterator().next();

                if (!ar.getId().matches("((\\w)*_(\\d)*)")) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You are not within a PolyProtect protection!");
                    return true;
                }

                LocalPlayer localplayer = WGBukkit.getPlugin().wrapPlayer(player);
                if (!ar.isOwner(localplayer) || !player.hasPermission("pgc.prot.admin") || !ar.isMember(localplayer)) {
                    player.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You do not have permission to view info of this region!");
                    return true;
                }
                PolyProtectUtils.sendProtectionInfo(player);
                return true;

            case "playerinfo":
            case "pinfo":
            case "playeri":
            case "pi":
                if (!sender.hasPermission("pgc.prot.admin")) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "You don't have permission to view info!");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(true) + ChatColor.RED + "Incorrect usage of /prot player info!");
                    return true;
                }

                owner = Bukkit.getServer().getOfflinePlayer(args[1]);
                if (owner.hasPlayedBefore()) {
                    int survivalCount = PolyProtectUtils.countProtections(owner.getName(), PolyProtect.getSurvivalWorlds());
                    int creativeCount = PolyProtectUtils.countProtections(owner.getName(), PolyProtect.getCreativeWorlds());
                    int maxSurvivalCount = PolyProtectUtils.getMaxProtectionCount(owner, WorldType.SURVIVAL);
                    int maxCreativeCount = PolyProtectUtils.getMaxProtectionCount(owner, WorldType.CREATIVE);
                    sender.sendMessage(ChatColor.BLUE + "---- " + ChatColor.DARK_AQUA + "Player " + ChatColor.RED + owner.getName() + ChatColor.BLUE + " ----\n"
                            + ChatColor.DARK_AQUA + "Total Protections: " + ChatColor.BLUE + (survivalCount + creativeCount) + "\n"
                            + ChatColor.DARK_AQUA + "Survival Protections: " + ChatColor.BLUE + survivalCount + " out of " + maxSurvivalCount + "\n"
                            + ChatColor.DARK_AQUA + "Creative Protections: " + ChatColor.BLUE + creativeCount + " out of " + maxCreativeCount);
                    return true;
                }
                sender.sendMessage(PolyProtect.pluginChatPrefix(true) + "Something went terribly wrong...");
                return false;

            case "allow":
            case "addmember":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "Go away Johnsole, nobody likes you.");
                    return true;
                }
                player = (Player) sender;

                if (args.length != 2) {
                    return false;
                }
                OfflinePlayer newMember = Bukkit.getServer().getOfflinePlayer(args[1]);
                if (newMember.hasPlayedBefore()) {
                    PolyProtectUtils.selectProtection(player, false);
                    PolyProtectUtils.addMemberToProtection(player, newMember.getName());
                }
                return true;
            case "deny":
            case "removemember":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(PolyProtect.pluginChatPrefix(false) + "Go away Johnsole, nobody likes you.");
                    return true;
                }
                player = (Player) sender;

                if (args.length != 2) {
                    return false;
                }
                OfflinePlayer memberToRemove = Bukkit.getServer().getOfflinePlayer(args[1]);
                if (memberToRemove.hasPlayedBefore()) {
                    PolyProtectUtils.selectProtection(player, false);
                    PolyProtectUtils.removeMemberFromProtection(player, memberToRemove.getName());
                }
                return true;
            case "help":
                sender.sendMessage(ChatColor.YELLOW + " ---- " + ChatColor.GOLD + "PolyProtect Help" + ChatColor.YELLOW + " ---- \n"
                        + ChatColor.GOLD + "/prot help" + ChatColor.RESET + ": Display this help page.\n"
                        + ChatColor.GOLD + "/prot info selection" + ChatColor.RESET + ": Display info about selection.\n"
                        + ChatColor.GOLD + "/prot info <player>" + ChatColor.RESET + ": Protection info about player.\n"
                        + ChatColor.GOLD + "/prot create <player>" + ChatColor.RESET + ": Create protection for player.\n"
                        + ChatColor.GOLD + "/prot select" + ChatColor.RESET + ": Select current protection.\n"
                        + ChatColor.GOLD + "/prot delete" + ChatColor.RESET + ": Delete the selected protection.\n"
                        + ChatColor.GOLD + "/prot allow <player>" + ChatColor.RESET + ": Give player access to selected protection.\n"
                        + ChatColor.GOLD + "/prot deny <player>" + ChatColor.RESET + ": Remove player from selected protection.\n");
                return true;
            default:
                return false;
        }
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;

    }
}
