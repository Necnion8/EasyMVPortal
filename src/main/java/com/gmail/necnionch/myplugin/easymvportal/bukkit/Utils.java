package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.config.MainConfig;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Utils {
    private static EasyMVPortal plugin = null;
    private static MainConfig config;

    static void init(EasyMVPortal plugin, MainConfig config) {
        Utils.plugin = plugin;
        Utils.config = config;

//        try {
//            PlayerInventory.class.getMethod("getItemInMainHand")
//        } catch (NoSuchMethodException e) {
//            // 1.8 older
//            PlayerInventory.class.getMethod("getItem")
//        }
    }

    public static void debug(String m) {
        if (plugin != null && config.isDebug()) {
            plugin.getLogger().warning("[DEBUG]: " + m);
        }
    }

    public static Logger getLogger() {
        if (plugin != null) {
            return plugin.getLogger();
        }
        return Bukkit.getLogger();
    }

    public static boolean copyResourceToFile(String sourceFileName, Path targetFile) throws IOException {
        initFileParent(targetFile.toFile());
        InputStream is = plugin.getResource(sourceFileName);
        if (is != null) {
            Files.copy(is, targetFile);
            return true;
        }
        getLogger().warning("Resource file does not exist: " + sourceFileName);
        return false;
    }

    public static void initFileParent(File path) {
        File parent = path.getParentFile();
        if (!parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
    }





}
