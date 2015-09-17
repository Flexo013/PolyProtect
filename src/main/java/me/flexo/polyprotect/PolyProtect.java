package me.flexo.polyprotect;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.flexo.polyprotect.command.PolyProtectCommand;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @authors Flexo013, Orendigo
 */
public class PolyProtect extends JavaPlugin {

    private static final Logger log = Bukkit.getLogger();
    private Plugin vault = null;
    private Permission permission = null;
    private static ArrayList<String> creativeWorlds = new ArrayList<>();
    public static ArrayList<String> survivalWorlds = new ArrayList<>();
    //##lelijk

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
            log.log(Level.WARNING, "Could not find plugin \"{0}\"!", pluginName);
            return null;
        }
    }
    
    public static PolyProtect getPlugin(){
        return (PolyProtect) Bukkit.getPluginManager().getPlugin("PolyProtect");
    }
    
    public Permission getPermission(){
        return permission;
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
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        if (permission == null) {
            getLogger().log(Level.WARNING, "Could not hook Vault!");
        } else {
            getLogger().log(Level.WARNING, "Hooked Vault!");
        }

        return (permission != null);
    }

    /**
     * get the minecraft chat prefix for this plugin
     *
     * @param color if true get the colored version of the prefix
     * @return the minecraft chat prefix for this plugin
     */
    public static String pluginChatPrefix(boolean color) {
        if (color) {
            return ChatColor.DARK_AQUA + "[" + ChatColor.BLUE + "PolyProtect" + ChatColor.DARK_AQUA + "]" + ChatColor.RESET + " ";
        } else {
            return "[PolyProtect] ";
        }
    }

    public static ArrayList<String> getCreativeWorlds() {
        return creativeWorlds;
    }

    public static ArrayList<String> getSurvivalWorlds() {
        return survivalWorlds;
    }

}
