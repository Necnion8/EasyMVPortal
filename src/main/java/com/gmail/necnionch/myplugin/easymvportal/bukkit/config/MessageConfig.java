package com.gmail.necnionch.myplugin.easymvportal.bukkit.config;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.Utils;
import com.google.common.base.Charsets;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public class MessageConfig {
    private JavaPlugin plugin;
    private YamlConfiguration config = null;

    public MessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Logger getLog() {
        return plugin.getLogger();
    }

    public void load(String defaultLangName) {
        File outFile = new File(plugin.getDataFolder(), "messages.yml");
        Utils.initFileParent(outFile);

        // copy default file
        if (!outFile.exists()) {
            boolean copyDone = false;
            try {
                copyDone = Utils.copyResourceToFile("languages/" + defaultLangName + ".yml", outFile.toPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!copyDone) {
                getLog().severe("The language file could not be extracted.");
                return;
            }
        }

        // load yaml
        boolean loadDone = false;
        try (InputStream is = new FileInputStream(outFile);
             InputStreamReader isr = new InputStreamReader(is, Charsets.UTF_8)) {

            if (config != null) {
                config.load(isr);
            } else {
                config = YamlConfiguration.loadConfiguration(isr);
            }
            loadDone = true;

        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        if (!loadDone) {
            getLog().severe("The language file could not be loaded.");
        }

    }

    public String get(String key) {
        if (config != null)
            return config.getString(key, null);
        return null;
    }

}
