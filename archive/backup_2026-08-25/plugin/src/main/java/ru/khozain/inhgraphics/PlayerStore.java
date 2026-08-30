package ru.khozain.inhgraphics;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище настроек: players.yml, сохраняется навсегда.
 */
public final class PlayerStore {

    private final InhGraphicsPlugin plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File file;
    private YamlConfiguration yaml = new YamlConfiguration();

    public PlayerStore(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        load();
    }

    public PlayerData get(UUID id) {
        return cache.computeIfAbsent(id, k -> new PlayerData());
    }

    public void save(UUID id) {
        PlayerData d = get(id);
        String path = "players." + id;
        yaml.set(path + ".time", d.timeTicks);
        yaml.set(path + ".weather", d.weather);
        yaml.set(path + ".render", d.renderDistance);
        yaml.set(path + ".default-render", d.defaultRender);
        yaml.set(path + ".fog-on", d.fogOn);
        yaml.set(path + ".fog-density", d.fogDensity);
        flush();
    }

    /** Полный сброс игрока: и в памяти, и на диске. */
    public void reset(UUID id) {
        get(id).reset();
        yaml.set("players." + id, null);
        flush();
    }

    public void loadInto(UUID id) {
        ConfigurationSection root = yaml.getConfigurationSection("players." + id);
        if (root == null) return;
        PlayerData d = get(id);
        d.timeTicks = root.getLong("time", -1L);
        d.weather = root.getString("weather", "NONE");
        d.renderDistance = root.getInt("render", -1);
        d.defaultRender = root.getInt("default-render", -1);
        d.fogOn = root.getBoolean("fog-on", false);
        d.fogDensity = root.getInt("fog-density", 5);
    }

    private void load() {
        if (!file.exists()) return;
        try {
            yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection players = yaml.getConfigurationSection("players");
            if (players == null) return;
            for (String key : players.getKeys(false)) {
                try {
                    loadInto(UUID.fromString(key));
                } catch (IllegalArgumentException ignored) {
                    // битый ключ — не наша беда
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не смог прочитать players.yml: " + e.getMessage());
        }
    }

    private void flush() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не смог сохранить players.yml: " + e.getMessage());
        }
    }
}
