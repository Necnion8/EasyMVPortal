package com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.impl;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.Utils;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPManager;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp.EMVPortal;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.MultiversePortals.utils.PortalManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Stream;

public class EMVPManagerImpl2_4 implements EMVPManager {

    private final MVWorldManager worlds;
    private final MultiversePortals  multiversePortals;
    private final PortalManager portals;

    public EMVPManagerImpl2_4(MVWorldManager worlds, MultiversePortals multiversePortals, PortalManager portals) {
        this.worlds = worlds;
        this.multiversePortals = multiversePortals;
        this.portals = portals;
    }

    public static EMVPManagerImpl2_4 init() throws RuntimeException {
        MultiverseCore mvc;
        MultiversePortals mvp;
        try {
            Class.forName("com.onarandombox.MultiverseCore.MultiverseCore");
            mvc = JavaPlugin.getPlugin(MultiverseCore.class);
            Class.forName("com.onarandombox.MultiversePortals.MultiversePortals");
            mvp = JavaPlugin.getPlugin(MultiversePortals.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Multiverse-[Core/Portals] v2-4: Class not found");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load Multiverse-[Core/Portals] v2-4", e);
        }

        return new EMVPManagerImpl2_4(mvc.getMVWorldManager(), mvp, mvp.getPortalManager());
    }

    @Override
    public boolean isMVWorld(World world) {
        return worlds.isMVWorld(world);
    }

    @Override
    public Stream<EMVPortal> streamPortals() {
        return portals.getAllPortals().stream().map(p -> new EMVPortal(p.getName()));
    }

    @Override
    public boolean createPortals(String ownerName, String p1Name, World p1World, Location p1Pos1, Location p1Pos2, String p2Name, World p2World, Location p2Pos1, Location p2Pos2) {
        MultiverseWorld mv1World = worlds.getMVWorld(p1World);
        PortalLocation p1Loc = new PortalLocation(p1Pos1.toVector(), p1Pos2.toVector(), mv1World);
        MVPortal p1 = new MVPortal(multiversePortals, p1Name, ownerName, p1Loc);
        MultiverseWorld mv2World = worlds.getMVWorld(p2World);
        PortalLocation p2Loc = new PortalLocation(p2Pos1.toVector(), p2Pos2.toVector(), mv2World);
        MVPortal p2 = new MVPortal(multiversePortals, p2Name, ownerName, p2Loc);

        if (portals.addPortal(p1)) {
            if (portals.addPortal(p2)) {
                p1.setDestination(Utils.formatPortalDestinationAction(p2Name, p2Pos1.getYaw() + 180));
                p2.setDestination(Utils.formatPortalDestinationAction(p1Name, p1Pos1.getYaw() + 180));
                return true;
            }
            portals.removePortal(p1Name, true);
        }
        return false;
    }

}
