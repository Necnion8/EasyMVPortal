package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.config.MainConfig;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.config.MessageConfig;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class EasyMVPortal extends JavaPlugin implements Listener {
    private static EasyMVPortal instance = null;
    private MainConfig mainConfig;
    private MessageConfig messageConfig;
    private MultiverseCore mvPlugin;
    private MultiversePortals mvpPlugin;
    private PortalManager portalManager;
    private MVWorldManager worldManager;
    private HashMap<Player, PortalCreator> creatorSessions = new HashMap<>();
    public static final Permission COMMAND_HELP_PERMISSION = new Permission("easymvportal.command.help");
    public static final Permission COMMAND_CREATE_PERMISSION = new Permission("easymvportal.command.create");
    public static final Permission COMMAND_RELOAD_PERMISSION = new Permission("easymvportal.command.reload");
    private boolean useGUI;

    static public EasyMVPortal getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        try {
            new VersionMatcher().match();
            useGUI = true;

        } catch (IllegalStateException e) {
            getLogger().warning(e.getLocalizedMessage());
            getLogger().info("ignored it!");
        } catch (Throwable e) {
            e.printStackTrace();
            setEnabled(false);
        }

        instance = this;

        Utils.init(this, mainConfig);
        mainConfig = new MainConfig(this);
        messageConfig = new MessageConfig(this);
        mainConfig.load();
        messageConfig.load(mainConfig.getLanguageName());

        try {
            mvPlugin = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
            mvpPlugin = (MultiversePortals) getServer().getPluginManager().getPlugin("Multiverse-Portals");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mvPlugin == null || mvpPlugin == null) {
            getLogger().severe("Hook failed!");
            setEnabled(false);
            return;
        }

        portalManager = mvpPlugin.getPortalManager();
        worldManager = mvPlugin.getMVWorldManager();
        getCommand("easymvportal").setExecutor(new MainCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Event_1_9_Listener(), this);


        new MetricsLite(this, 9114);
    }

    @Override
    public void onDisable() {
        new ArrayList<>(creatorSessions.values())
                .forEach(PortalCreator::close);

    }


    public PortalCreator getCreatorSession(Player player, boolean create) {
        if (!creatorSessions.containsKey(player) && create)
            creatorSessions.put(player, new PortalCreator(this, player));

        return creatorSessions.get(player);
    }

    void removeCreator(Player player) {
        creatorSessions.remove(player);
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public MVWorldManager getWorldManager() {
        return worldManager;
    }

    public MultiversePortals getMvpPlugin() {
        return mvpPlugin;
    }

    public MainConfig getPluginConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public boolean isUseGUI() {
        return useGUI;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PortalCreator session = creatorSessions.remove(event.getPlayer());
        if (session != null) {
            session.close();
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        PortalCreator creator = creatorSessions.get(event.getPlayer());
        if (creator != null)
            creator.onEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PortalCreator creator = creatorSessions.get(event.getPlayer());
        if (creator != null)
            creator.onEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInvClick(InventoryClickEvent event) {
        PortalCreator creator = creatorSessions.get(event.getWhoClicked());
        if (creator != null)
            creator.onEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PortalCreator creator = creatorSessions.get(event.getEntity());
        if (creator != null)
            creator.onEvent(event);
    }


    public class Event_1_9_Listener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onSwapHand(PlayerSwapHandItemsEvent event) {
            PortalCreator creator = creatorSessions.get(event.getPlayer());
            if (creator != null)
                creator.onEvent(event);
        }

    }

}
