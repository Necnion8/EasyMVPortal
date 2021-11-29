package com.gmail.necnionch.myplugin.easymvportal.bukkit.commands;

import com.gmail.necnionch.myplugin.easymvportal.bukkit.EasyMVPortal;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.Lang;
import com.gmail.necnionch.myplugin.easymvportal.bukkit.PortalCreator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    private EasyMVPortal instance;

    public MainCommand(EasyMVPortal plugin) {
        instance = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            if (checkPerms(sender, EasyMVPortal.COMMAND_CREATE_PERMISSION))
                cmdCreate(sender);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            if (checkPerms(sender, EasyMVPortal.COMMAND_CREATE_PERMISSION))
                cmdCreate(sender);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (checkPerms(sender, EasyMVPortal.COMMAND_RELOAD_PERMISSION))
                cmdReload(sender);

        } else {
            return !checkPerms(sender, EasyMVPortal.COMMAND_HELP_PERMISSION);

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission(EasyMVPortal.COMMAND_HELP_PERMISSION)) {
            List<String> entries = new ArrayList<>();
            if (sender.hasPermission(EasyMVPortal.COMMAND_CREATE_PERMISSION))
                entries.add("create");
            if (sender.hasPermission(EasyMVPortal.COMMAND_RELOAD_PERMISSION))
                entries.add("reload");

            return entries.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void cmdReload(CommandSender sender) {
        if (instance.getPluginConfig().load()) {
            instance.getMessageConfig().load(instance.getPluginConfig().getLanguageName());
            Lang.CONFIG_RELOADED.sendTo(sender);
        } else {
            Lang.CONFIG_RELOAD_FAIL.sendTo(sender);
        }
    }

    private void cmdCreate(CommandSender sender) {
        if (sender instanceof Player) {
            PlayerInventory inv = ((Player) sender).getInventory();

            if (!Material.AIR.equals(inv.getItemInMainHand().getType())) {
                int emptySlot = inv.firstEmpty();
                // ホットバーに空きがある？
                if (0 <= emptySlot && emptySlot <= 8) {
                    inv.setHeldItemSlot(emptySlot);
                } else {
                    if (emptySlot == -1) {
                        Lang.INVENTORY_FULL.sendTo(sender);
                        return;
                    }
                    // メインハンドアイテムを別の空きスロットに
                    inv.setItem(emptySlot, inv.getItemInMainHand());
                    inv.setItemInMainHand(null);
                }
            }

            PortalCreator creator = PortalCreator.getCreator((Player) sender, true);
            creator.initAndReplaceMainHandItem();

        } else {
            Lang.PLAYER_ONLY.sendTo(sender);
        }
    }

    private boolean checkPerms(CommandSender sender, Permission permission) {
        if (!sender.hasPermission(permission)) {
            Lang.PERMISSION_ERROR.sendTo(sender);
            return false;
        }
        return true;
    }


}
