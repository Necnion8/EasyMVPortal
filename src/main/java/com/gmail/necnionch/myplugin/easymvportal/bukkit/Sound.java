package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import org.bukkit.entity.Player;

public enum Sound {
    EXPERIENCE_ORB_PICKUP("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP"),  // "random.orb"),
    NOTE_BASS("BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS", "NOTE_BASS"),  // "note.bass"),
    BLAZE_HURT("ENTITY_BLAZE_HURT", "BLAZE_HIT"),  // "mob.blaze.hit"),
    PLAYER_LEVELUP("ENTITY_PLAYER_LEVELUP", "LEVEL_UP")  // "random.levelup"),
    ;

    private final org.bukkit.Sound id;
    Sound(String... soundId) {
        org.bukkit.Sound id = null;
        for (String s : soundId) {
            try {
                id = org.bukkit.Sound.valueOf(s);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (id == null) {
            EasyMVPortal.getInstance().getLogger().warning("Unknown sound: " + soundId[0]);
        }
        this.id = id;

    }

    public org.bukkit.Sound getSound() {
        return id;
    }

    public void playTo(Player player, float vol, float pit) {
        if (id != null) {
            player.playSound(player.getLocation(), id, vol, pit);
        }
    }

}
