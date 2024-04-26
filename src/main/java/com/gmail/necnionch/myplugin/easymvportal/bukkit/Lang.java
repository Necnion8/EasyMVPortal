package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;


public enum Lang {
    TEST("{0}"),

    TOOL_NAME_ANVIL_DISPLAY("&d&lPortal Naming"),
    TOOL_PORTAL1_WAND_DISPLAY("&e&lPortal-1 Wand"),
    TOOL_PORTAL1_WAND_LORE("&eShift right click to select next portal"),
    TOOL_PORTAL2_WAND_DISPLAY("&b&lPortal-2 Wand"),
    TOOL_PORTAL2_WAND_LORE("&eShift right click to enter portal name"),
    SET_PORTAL1_FIRST_POS("&7First position set to: ({0}, {1}, {2})"),
    SET_PORTAL1_SECOND_POS("&7Second position set to: ({0}, {1}, {2})"),
    SET_PORTAL2_FIRST_POS("&7First position set to: ({0}, {1}, {2})"),
    SET_PORTAL2_SECOND_POS("&7Second position set to: ({0}, {1}, {2})"),

    GUI_INSERT_PORTAL_NAME("&0Insert portal name."),
    GUI_INSERT_PORTAL_NAME_ALREADY_EXISTS("&4It already existed!"),

    CREATE_AND_INSERT_PORTAL_NAME("&6Please execute &f&n/emvp (portalName)"),
    ALREADY_EXISTS_PORTAL_NAME("&cAlready exists portal name!"),

    SELECT_PORTAL1("&6Please specify the first portal."),
    SELECT_PORTAL2("&6Please specify the second portal."),

    CANCEL("&cCancelled!"),
    PORTAL_CREATED("&aPortal created: &f{0}"),
    NOT_LOADED_MULTIVERSE("&cThis world is not loaded by Multiverse!"),
    INVENTORY_FULL("&cInventory is full!"),

    CONFIG_RELOADED("&aConfiguration reloaded!"),
    CONFIG_RELOAD_FAIL("&cReload failed!"),
    PLAYER_ONLY("&cOnly the player can execute it."),
    PERMISSION_ERROR("&cYou do not have permission to execute.")

    ;

    private String defaultMessage;

    Lang(String def) {
        this.defaultMessage = def;
    }

    public void sendTo(Collection<? extends CommandSender> senders, Object... params) {
        String message = format(params);
        if (message.isEmpty())
            return;

        String prefix = getPlugin().getPluginConfig().getPrefix();
        String finalValue = prefix + message;

        senders.forEach(s -> s.sendMessage(finalValue));
    }

    public void sendTo(CommandSender s, Object... params) {
        sendTo(Collections.singleton(s), params);
    }

    public String format(Object... params) {
        String keyName = name().toLowerCase().replace('_', '-');
        String value = getPlugin().getMessageConfig().get(keyName);
        if (value == null)
            value = defaultMessage;
        if (value.isEmpty())
            return "";

        String[] tmp = value.split("\n");
        value = (tmp.length != 0) ? tmp[0] : "";
        value = ChatColor.translateAlternateColorCodes('&', value);

        try {
            value = MessageFormat.format(value, params);

        } catch (IllegalArgumentException e) {
            getPlugin().getLogger().severe("Message format error: " + e.getLocalizedMessage());
            getPlugin().getLogger().severe("(Key/value) " + keyName + ": \"" + value + "\"");
            return "";
        }
        return value;
    }

    public String[] formatMultiline(Object... params) {
        String keyName = name().toLowerCase().replace('_', '-');
        String value = getPlugin().getMessageConfig().get(keyName);
        if (value == null)
            value = defaultMessage;
        if (value.isEmpty())
            return new String[0];

        value = ChatColor.translateAlternateColorCodes('&', value);

        try {
            value = MessageFormat.format(value, params);

        } catch (IllegalArgumentException e) {
            getPlugin().getLogger().severe("Message format error: " + e.getLocalizedMessage());
            getPlugin().getLogger().severe("(Key/value) " + keyName + ": \"" + value + "\"");
            return new String[0];
        }
        return value.split("\n");
    }


    private EasyMVPortal getPlugin() {
        return EasyMVPortal.getInstance();
    }

}
