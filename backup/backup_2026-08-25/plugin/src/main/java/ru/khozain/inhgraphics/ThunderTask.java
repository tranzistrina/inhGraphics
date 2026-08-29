package ru.khozain.inhgraphics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Раскаты грома при персональной грозе: чистый звук для одного игрока,
 * без молний и урона.
 */
public final class ThunderTask extends BukkitRunnable {

    private final InhGraphicsPlugin plugin;
    private final Map<UUID, Long> nextStrikeAt = new HashMap<>();
    private final java.util.Random random = new java.util.Random();

    public ThunderTask(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.thunderSoundEnabled()) return;
        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            PlayerData d = plugin.getStore().get(id);
            if (!"THUNDER".equals(d.weather)) {
                nextStrikeAt.remove(id);
                continue;
            }
            long at = nextStrikeAt.computeIfAbsent(id, k -> now + randomDelay());
            if (now >= at) {
                float pitch = 0.85f + random.nextFloat() * 0.3f;
                float volume = 0.6f + random.nextFloat() * 0.4f;
                // строковый вариант: Paper некорректно резолвит enum-константу
                // ENTITY_LIGHTNING_BOLT_THUNDER (клиент получает impact)
                p.playSound(p.getLocation(), "entity.lightning_bolt.thunder", volume, pitch);
                nextStrikeAt.put(id, now + randomDelay());
            }
        }
    }

    private long randomDelay() {
        int min = plugin.thunderMinSeconds();
        int max = Math.max(min, plugin.thunderMaxSeconds());
        return (min + random.nextInt(max - min + 1)) * 1000L;
    }
}
