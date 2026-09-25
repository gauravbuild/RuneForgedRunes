package me.gauravbuilds.runeforgedrunes.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class RuneCooldownManager {
    private final Map<UUID, Map<String, Long>> lastTrigger = new HashMap<>();

    public boolean isReady(Player player, String runeId, long cooldownMillis) {
        Map<String, Long> playerMap = this.lastTrigger.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long now = System.currentTimeMillis();
        Long last = playerMap.get(runeId);
        if (last == null || now - last >= cooldownMillis) {
            playerMap.put(runeId, now);
            return true;
        }
        return false;
    }
}