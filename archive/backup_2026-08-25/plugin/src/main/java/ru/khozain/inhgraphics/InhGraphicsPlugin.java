package ru.khozain.inhgraphics;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.khozain.inhgraphics.commands.IgFogCommand;
import ru.khozain.inhgraphics.commands.IgResetCommand;
import ru.khozain.inhgraphics.commands.IgRenderCommand;
import ru.khozain.inhgraphics.commands.IgTimeCommand;
import ru.khozain.inhgraphics.commands.IgWeatherCommand;

/**
 * inhGraphics — персональные погода, время, прорисовка и туман
 * для отдельных игроков. Косметика: серверная логика не трогается.
 */
public final class InhGraphicsPlugin extends JavaPlugin implements Listener {

    private PlayerStore store;
    private Particle fogParticle = Particle.CLOUD;
    private double fogRadius = 7.0;
    private double fogHeight = 2.0;
    private boolean thunderSound;
    private int thunderMinSeconds;
    private int thunderMaxSeconds;
    private BukkitTask fogTask;
    private BukkitTask thunderTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        readConfig();
        store = new PlayerStore(this);

        register("igtime", new IgTimeCommand(this));
        register("igweather", new IgWeatherCommand(this));
        register("igrender", new IgRenderCommand(this));
        register("igfog", new IgFogCommand(this));
        register("igreset", new IgResetCommand(this));

        getServer().getPluginManager().registerEvents(this, this);

        long period = Math.max(1L, getConfig().getLong("fog.period-ticks", 3L));
        fogTask = new FogTask(this).runTaskTimer(this, period, period);
        thunderTask = new ThunderTask(this).runTaskTimer(this, 20L, 20L);

        if (!NmsBridge.isAvailable()) {
            getLogger().warning("Пакеты уровней дождя/грома недоступны — персональная гроза будет без тёмного неба.");
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyStored(p); // на случай /reload
        }
        getLogger().info("inhGraphics включён: персональная графика готова к художествам.");
    }

    @Override
    public void onDisable() {
        if (fogTask != null) fogTask.cancel();
        if (thunderTask != null) thunderTask.cancel();
        getLogger().info("inhGraphics выключен.");
    }

    private void register(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Команда " + name + " не объявлена в plugin.yml!");
            return;
        }
        cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
        if (executor instanceof org.bukkit.command.TabCompleter tc) {
            cmd.setTabCompleter(tc);
        }
    }

    private void readConfig() {
        String particleName = getConfig().getString("fog.particle", "CLOUD");
        try {
            fogParticle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Частица '" + particleName + "' не найдена, использую CLOUD.");
            fogParticle = Particle.CLOUD;
        }
        fogRadius = Math.max(2.0, getConfig().getDouble("fog.radius", 7.0));
        fogHeight = Math.max(0.0, getConfig().getDouble("fog.height", 2.0));
        thunderSound = getConfig().getBoolean("thunder-sound", true);
        thunderMinSeconds = Math.max(1, getConfig().getInt("thunder-interval-min", 20));
        thunderMaxSeconds = Math.max(thunderMinSeconds, getConfig().getInt("thunder-interval-max", 60));
    }

    /** Применить сохранённые настройки игроку с небольшой задержкой после входа. */
    private void applyStored(Player p) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!p.isOnline()) return;
            PlayerData d = store.get(p.getUniqueId());
            Applier.applyAll(this, p, d);
        }, 15L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyStored(event.getPlayer());
    }

    /**
     * Мир сменил погоду — сервер рассылает всем game events. Игрокам с
     * персональной погодой тихо возвращаем их собственную.
     */
    @EventHandler
    public void onWorldWeather(org.bukkit.event.weather.WeatherChangeEvent event) {
        reapplyCustomWeather();
    }

    @EventHandler
    public void onWorldThunder(org.bukkit.event.weather.ThunderChangeEvent event) {
        reapplyCustomWeather();
    }

    private void reapplyCustomWeather() {
        Bukkit.getScheduler().runTask(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData d = store.get(p.getUniqueId());
                if (!"NONE".equals(d.weather)) {
                    Applier.applyWeather(p, d);
                }
            }
        });
    }

    // ---- доступ для задач и команд ----

    public PlayerStore getStore() {
        return store;
    }

    public Particle fogParticle() {
        return fogParticle;
    }

    public double fogRadius() {
        return fogRadius;
    }

    public double fogHeight() {
        return fogHeight;
    }

    public boolean thunderSoundEnabled() {
        return thunderSound;
    }

    public int thunderMinSeconds() {
        return thunderMinSeconds;
    }

    public int thunderMaxSeconds() {
        return thunderMaxSeconds;
    }
}
