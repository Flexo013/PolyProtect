package me.flexo.polyprotect;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @authors Flexo013, Orendigo
 */
public class PolyProtect extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private Permission permission = null;
    private ArrayList<String> creativeWorlds = new ArrayList<>();
    private ArrayList<String> survivalWorlds = new ArrayList<>();
    private ArrayList<Pair<String,ProtectedRegion>> selectedRegionList = new ArrayList<>();

    /**
     * Gets a plugin
     *
     * @param pluginName Name of the plugin to get
     * @return The plugin from name
     */
    protected Plugin getPlugin(String pluginName) {
        if (getServer().getPluginManager().getPlugin(pluginName) != null && getServer().getPluginManager().getPlugin(pluginName).isEnabled()) {
            return getServer().getPluginManager().getPlugin(pluginName);
        } else {
            getLogger().log(Level.WARNING, "Could not find plugin \"{0}\"!", pluginName);
            return null;
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        log.info("[PolyProtect] PolyProtect configuration loaded.");

        vault = getPlugin("Vault");
        if (vault != null) {
            setupPermissions();
        }

        getCommand("prot").setExecutor(new PolyProtectCommand(this));

        creativeWorlds = (ArrayList) getConfig().getStringList("creative");
        survivalWorlds = (ArrayList) getConfig().getStringList("survival");
    }

    @Override
    public void onDisable() {

    }

    /**
     * Setup permissions
     *
     * @return True: Setup correctly, Didn't setup correctly
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        if (permission == null) {
            log.log(Level.WARNING, "Could not hook Vault!");
        } else {
            log.log(Level.WARNING, "Hooked Vault!");
        }

        return (permission != null);
    }

    /**
     * get the minecraft chat prefix for this plugin
     *
     * @return the minecraft chat prefix for this plugin
     */
    public String pluginChatPrefix() {
        return ChatColor.DARK_AQUA + "[" + ChatColor.BLUE + "PolyProtect" + ChatColor.DARK_AQUA + "]" + ChatColor.RESET + " ";
    }

    private int newRegionNumber(String worldtype, String player) {
        switch (worldtype) {
            case "creative":
                int i = 1;
                while (true) {
                    boolean regionFound = false;
                    for (String creativeWorld : creativeWorlds) {
                        if (!regionFound) {
                            RegionManager rgm = WGBukkit.getRegionManager(getServer().getWorld(creativeWorld));
                            if (rgm.hasRegion(player + "_" + i)) {
                                regionFound = true;
                            }
                        }
                    }
                    if (regionFound) {
                        i++;
                    } else {
                        return i;
                    }
                }
            case "survival":
                int j = 1;
                while (true) {
                    boolean regionFound = false;
                    for (String survivalWorld : survivalWorlds) {
                        if (!regionFound) {
                            RegionManager rgm = WGBukkit.getRegionManager(getServer().getWorld(survivalWorld));
                            if (rgm.hasRegion(player + "_" + j)) {
                                regionFound = true;
                            }
                        }
                    }
                    if (regionFound) {
                        j++;
                    } else {
                        return j;
                    }
                }
            default:
                log.severe("Internal plugin error on regionAmount()");
                return -1;
        }
    }

    private void selectRegion(Player player) {
        RegionManager rgm = WGBukkit.getRegionManager(getServer().getWorld(player.getWorld().getName()));
        ApplicableRegionSet ars = rgm.getApplicableRegions(player.getLocation());
        if (ars.size() == 0) {
            player.sendMessage(pluginChatPrefix() + ChatColor.RED + "You are not within a region!");
        } else if (ars.size() >= 2) {
            player.sendMessage(pluginChatPrefix() + ChatColor.RED + "You are standing in intersecting regions!");
        } else {
            Pair testPair;
            for (ProtectedRegion ar : ars) {
                testPair = Pair(player.getName(), ar);
            }
            
            selectedRegionList.add(testPair);
        }
    }

    private static class PolyProtectCommand implements CommandExecutor {

        private final PolyProtect plugin;

        public PolyProtectCommand(PolyProtect aThis) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

            if (args.length == 0) {
                sender.sendMessage(plugin.pluginChatPrefix() + ChatColor.GOLD + "Use: /prot (help | create | define | remove | delete | select | allow | deny) (updateheads | reset <boardnr> | create (<worldname> <xloc> <yloc> <zloc>|here) <width in x> <depth in z> <bombcount> | delete <boardnr>)");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                case "define":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Go away Johnsole, nobody likes you.");
                        return true;
                    }
                    if (!sender.hasPermission("pgc.prot.admin")) {
                        //custom perm error TODO
                        return false;
                    }

                    if (args.length != 2) {
                        return false;
                    }

                    Player owner = (Player) plugin.getServer().getOfflinePlayer(args[1]);

                    if (!owner.hasPlayedBefore()) {
                        sender.sendMessage(plugin.pluginChatPrefix() + ChatColor.RED + owner.getDisplayName() + " has never player before!");
                        return true;
                    }

                    if (plugin.creativeWorlds.contains(((Player) sender).getWorld().getName())) {
                        int newRegionNumber = plugin.newRegionNumber("creative", args[1]);
                        ((Player) sender).performCommand("/region define " + args[1] + "_" + newRegionNumber + " " + args[1]);
                        log.info(plugin.pluginChatPrefix() + sender.getName() + " just created a new creative region for " + args[1] + ".");
                    } else if (plugin.survivalWorlds.contains(((Player) sender).getWorld().getName())) {
                        int newRegionNumber = plugin.newRegionNumber("survival", args[1]);
                        ((Player) sender).performCommand("/region define " + args[1] + "_" + newRegionNumber + " " + args[1]);
                        log.info(plugin.pluginChatPrefix() + sender.getName() + " just created a new survival region for " + args[1] + ".");
                    } else {
                        sender.sendMessage(plugin.pluginChatPrefix() + ChatColor.YELLOW + "You cannot create protections in this world.");
                    }

                    return true;
                case "select":
                case "sel":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Go away Johnsole, nobody likes you.");
                        return true;
                    }
                    if (!sender.hasPermission("pgc.prot.admin")) {
                        //custom perm error TODO
                        return false;
                    }

                    if (args.length != 1) {
                        return false;
                    }
                    
                    plugin.selectRegion((Player) sender);

                    return true;
                case "delete":

                    if (!sender.hasPermission("sweeper.manage")) {
                        return false;
                    }
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
}
