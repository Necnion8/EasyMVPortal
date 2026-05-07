package com.gmail.necnionch.myplugin.easymvportal.bukkit.mvp;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.stream.Stream;

public interface EMVPManager {

    boolean isMVWorld(World world);

    Stream<EMVPortal> streamPortals();

    boolean createPortals(String ownerName, String p1Name, World p1World, Location p1Pos1, Location p1Pos2, String p2Name, World p2World, Location p2Pos1, Location p2Pos2);

}
