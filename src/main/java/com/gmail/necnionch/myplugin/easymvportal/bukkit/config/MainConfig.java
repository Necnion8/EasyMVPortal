package com.gmail.necnionch.myplugin.easymvportal.bukkit.config;

import com.gmail.necnionch.myplugin.easymvportal.common.BukkitConfigDriver;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends BukkitConfigDriver {
    private boolean isDebug = false;
    private String defaultPrefix = "§7[§ee§aMVP§7] §r";
    private String cachedPrefix = null;

    public MainConfig(JavaPlugin plugin) {
        super(plugin, "config.yml", "config.yml");
    }

    public boolean isDebug() {
        return isDebug;
    }

    public String getPrefix() {
        return (cachedPrefix!=null) ? cachedPrefix : defaultPrefix;
    }

    public String getLanguageName() {
        String value = null;
        if (config != null)
            value = config.getString("language", null);

        return (value!=null) ? value : "en";
    }

    public String getPortal1Name(String name) {
        return config.getString("portal1-name", "{name}_in").replaceAll("\\{name}", name);
    }

    public String getPortal2Name(String name) {
        return config.getString("portal2-name", "{name}_out").replaceAll("\\{name}", name);
    }


    @Override
    public boolean onLoaded(FileConfiguration config) {
        isDebug = config.getBoolean("debug");

        String prefix = config.getString("prefix", defaultPrefix.replaceAll("§", "&"));
        cachedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);

        return true;
    }
}
