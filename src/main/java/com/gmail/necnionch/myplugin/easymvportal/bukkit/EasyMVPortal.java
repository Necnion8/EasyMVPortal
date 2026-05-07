package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.config.MainConfig;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.config.MessageConfig;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPManager;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.impl.EMVPManagerImpl2_4;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.impl.EMVPManagerImpl5;
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
import java.util.logging.Level;

public final class EasyMVPortal extends JavaPlugin implements Listener {
    public static final Permission COMMAND_HELP_PERMISSION = new Permission("easymvportal.command.help");
    public static final Permission COMMAND_CREATE_PERMISSION = new Permission("easymvportal.command.create");
    public static final Permission COMMAND_RELOAD_PERMISSION = new Permission("easymvportal.command.reload");
    private static EasyMVPortal instance = null;
    private final HashMap<Player, PortalCreator> creatorSessions = new HashMap<>();
    private MainConfig mainConfig;
    private MessageConfig messageConfig;
    private EMVPManager emvpManager;
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
            getLogger().log(Level.WARNING, "Failed to check AnvilGUI: Unsupported server version?", e);
        }

        instance = this;

        Utils.init(this, mainConfig);
        mainConfig = new MainConfig(this);
        messageConfig = new MessageConfig(this);
        mainConfig.load();
        messageConfig.load(mainConfig.getLanguageName());

        emvpManager = null;
        try {
            emvpManager = EMVPManagerImpl5.init();
        } catch (RuntimeException e) {
            try {
                emvpManager = EMVPManagerImpl2_4.init();
            } catch (RuntimeException ex) {
                getLogger().log(Level.SEVERE, "Failed to hook to Multiverse-Core/Portals v2-5 API", e);
                setEnabled(false);
                return;
            }
        }

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

    public EMVPManager getMVP() {
        return emvpManager;
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
