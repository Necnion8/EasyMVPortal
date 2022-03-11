package com.gmail.necnionch.myplugin.easymvportal.bukkit;

import org.bukkit.entity.Player;

public enum Sound {
    EXPERIENCE_ORB_PICKUP("ENTITY_EXPERIENCE_ORB_PICKUP", "random.orb"),
    NOTE_BASS("BLOCK_NOTE_BLOCK_BASS", "note.bass"),
    BLAZE_HURT("ENTITY_BLAZE_HURT", "mob.blaze.hit"),
    PLAYER_LEVELUP("ENTITY_PLAYER_LEVELUP", "random.levelup"),
    ;

    private final String id;
    Sound(String soundId, String soundOldId) {
        String id;
        try {
            id = org.bukkit.Sound.valueOf(soundId).getKey().toString();
        } catch (IllegalArgumentException e) {
            id = soundOldId;
        }
        this.id = id;
    }

    public String getSoundId() {
        return id;
    }

    public void playTo(Player player, float vol, float pit) {
        player.playSound(player.getLocation(), id, vol, pit);
    }

}
