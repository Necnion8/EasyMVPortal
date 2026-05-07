package com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.impl;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.Utils;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPManager;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPortal;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.MultiversePortalsApi;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.stream.Stream;

public class EMVPManagerImpl5 implements EMVPManager {

    private final WorldManager worlds;
    private final PortalManager portals;
    private final MultiversePortals mvp;

    public EMVPManagerImpl5(MultiverseCoreApi coreApi, MultiversePortalsApi portalsApi, MultiversePortals mvp) {
        this.worlds = coreApi.getWorldManager();
        this.portals = portalsApi.getPortalManager();
        this.mvp = mvp;
    }

    public static EMVPManagerImpl5 init() throws RuntimeException {
        MultiverseCoreApi coreApi;
        MultiversePortalsApi portalsApi;
        try {
            Class.forName("org.mvplugins.multiverse.core.MultiverseCoreApi");
            coreApi = MultiverseCoreApi.get();
            Class.forName("org.mvplugins.multiverse.portals.MultiversePortalsApi");
            portalsApi = MultiversePortalsApi.get();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Multiverse-[Core/Portals] v5: Class not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Multiverse-[Core/Portals] v5: " + e.getMessage());
        }

        MultiversePortals mvp;
        try {
            Class.forName("org.mvplugins.multiverse.portals.MultiversePortals");
            mvp = JavaPlugin.getPlugin(MultiversePortals.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Multiverse-Portals v5: Class not found");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load Multiverse-Portals v5", e);
        }

        return new EMVPManagerImpl5(coreApi, portalsApi, mvp);
    }

    @Override
    public boolean isMVWorld(World world) {
        return worlds.getWorld(world).isDefined();
    }

    @Override
    public Stream<EMVPortal> streamPortals() {
        return portals.getAllPortals().stream().map(p -> new EMVPortal(p.getName()));
    }

    @Override
    public boolean createPortals(String ownerName, String p1Name, World p1World, Location p1Pos1, Location p1Pos2, String p2Name, World p2World, Location p2Pos1, Location p2Pos2) {
        LoadedMultiverseWorld mvWorld = worlds.getLoadedWorld(p1World).getOrElseThrow(() -> new RuntimeException("World " + p1World.getName() + " is not MV world or not loaded"));
        PortalLocation loc = new PortalLocation();
        loc.setLocation(p1Pos1.toVector(), p1Pos2.toVector(), mvWorld);
        MVPortal p1 = new MVPortal(mvp, p1Name, ownerName, loc);

        mvWorld = worlds.getLoadedWorld(p2World).getOrElseThrow(() -> new RuntimeException("World " + p2World.getName() + " is not MV world or not loaded"));
        loc = new PortalLocation();
        loc.setLocation(p2Pos1.toVector(), p2Pos2.toVector(), mvWorld);
        MVPortal p2 = new MVPortal(mvp, p2Name, ownerName, loc);

        if (portals.addPortal(p1)) {
            if (portals.addPortal(p2)) {
                p1.setActionType("multiverse-destination");
                p1.setAction(Utils.formatPortalDestinationAction(p2Name, p2Pos1.getYaw() + 180));
                p2.setActionType("multiverse-destination");
                p2.setAction(Utils.formatPortalDestinationAction(p1Name, p1Pos1.getYaw() + 180));
                return true;
            }
            portals.removePortal(p1Name, true);
        }
        return false;
    }

}
