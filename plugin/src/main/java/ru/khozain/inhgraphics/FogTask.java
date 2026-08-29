package ru.khozain.inhgraphics;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Персональный туман: частицы, которые видит только целевой игрок.
 */
public final class FogTask extends BukkitRunnable {

    private final InhGraphicsPlugin plugin;

    public FogTask(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Particle particle = plugin.fogParticle();
        double radius = plugin.fogRadius();
        double height = plugin.fogHeight();

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData d = plugin.getStore().get(p.getUniqueId());
            if (!d.fogOn) continue;

            int count = Math.max(1, d.fogDensity);
            var loc = p.getLocation();
            // верхний слой — основное облако тумана
            p.getWorld().spawnParticle(particle, List.of(p), null,
                    loc.getX(), loc.getY() + 1.6 + height, loc.getZ(),
                    count, radius * 0.8, 1.0, radius * 0.8,
                    0.0, null, false);
            // при высокой плотности добавляем нижний слой, чтобы гуще
            if (count >= 6) {
                p.getWorld().spawnParticle(particle, List.of(p), null,
                        loc.getX(), loc.getY() + 0.6, loc.getZ(),
                        count - 3, radius * 0.7, 0.8, radius * 0.7,
                        0.0, null, false);
            }
        }
    }
}
