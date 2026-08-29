package ru.khozain.inhgraphics;

import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Применение сохранённых настроек к игроку (при входе и после команд).
 */
public final class Applier {

    private Applier() {
    }

    public static void applyAll(InhGraphicsPlugin plugin, Player p, PlayerData d) {
        applyTime(p, d);
        applyWeather(p, d);
        applyRender(plugin, p, d);
        // туман подхватит FogTask сам
    }

    public static void applyTime(Player p, PlayerData d) {
        if (d.timeTicks >= 0) {
            // relative=true: время игрока = мировое + сдвиг, течёт вместе с миром
            p.setPlayerTime(d.timeTicks, true);
        } else {
            p.resetPlayerTime();
        }
    }

    /**
     * Установить игроку время суток targetTicks «прямо сейчас» и дальше
     * синхронно с мировым: считаем сдвиг от текущего мирового времени.
     */
    public static void applyTimeAt(InhGraphicsPlugin plugin, Player p, PlayerData d, long targetTicks) {
        long worldTime = Math.floorMod(p.getWorld().getTime(), 24000L);
        d.timeTicks = Math.floorMod(targetTicks - worldTime, 24000L);
        p.setPlayerTime(d.timeTicks, true);
        plugin.getStore().save(p.getUniqueId());
    }

    /**
     * ВАЖНО: в Paper 1.21.1 (build 133) Bukkit setPlayerWeather работает
     * некорректно — вместо смены погоды сервер синкает игроку мировое
     * состояние. Поэтому персональная погода делается полностью своими
     * пакетами: start/stop_raining + уровни дождя и грома.
     */
    public static void applyWeather(Player p, PlayerData d) {
        if (NmsBridge.isAvailable()) {
            switch (d.weather) {
                case "CLEAR" -> {
                    NmsBridge.send(p, NmsBridge.STOP_RAINING, 0.0f);
                    NmsBridge.send(p, NmsBridge.RAIN_LEVEL_CHANGE, 0.0f);
                    NmsBridge.send(p, NmsBridge.THUNDER_LEVEL_CHANGE, 0.0f);
                }
                case "RAIN" -> {
                    NmsBridge.send(p, NmsBridge.START_RAINING, 0.0f);
                    NmsBridge.send(p, NmsBridge.RAIN_LEVEL_CHANGE, 1.0f);
                    NmsBridge.send(p, NmsBridge.THUNDER_LEVEL_CHANGE, 0.0f);
                }
                case "THUNDER" -> {
                    NmsBridge.send(p, NmsBridge.START_RAINING, 0.0f);
                    NmsBridge.send(p, NmsBridge.RAIN_LEVEL_CHANGE, 1.0f);
                    NmsBridge.send(p, NmsBridge.THUNDER_LEVEL_CHANGE, 1.0f);
                }
                default -> {
                    World w = p.getWorld();
                    NmsBridge.send(p, w.hasStorm() ? NmsBridge.START_RAINING : NmsBridge.STOP_RAINING, 0.0f);
                    NmsBridge.send(p, NmsBridge.RAIN_LEVEL_CHANGE, w.hasStorm() ? 1.0f : 0.0f);
                    NmsBridge.send(p, NmsBridge.THUNDER_LEVEL_CHANGE, w.isThundering() ? 1.0f : 0.0f);
                }
            }
        } else {
            // запасной путь без NMS: хотя бы вкл/выкл дождя
            switch (d.weather) {
                case "CLEAR" -> p.setPlayerWeather(WeatherType.CLEAR);
                case "RAIN", "THUNDER" -> p.setPlayerWeather(WeatherType.DOWNFALL);
                default -> p.resetPlayerWeather();
            }
        }
    }

    public static void applyRender(InhGraphicsPlugin plugin, Player p, PlayerData d) {
        try {
            if (d.renderDistance >= 2) {
                if (d.defaultRender < 2) {
                    d.defaultRender = p.getSendViewDistance();
                    plugin.getStore().save(p.getUniqueId());
                }
                p.setSendViewDistance(Math.min(d.renderDistance, 32));
            } else if (d.defaultRender >= 2) {
                // возврат к значению, которое было до первого ограничения
                p.setSendViewDistance(d.defaultRender);
            } else {
                // полного сброса: мировое значение
                p.setSendViewDistance(Math.max(2, p.getWorld().getViewDistance()));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не смог изменить прорисовку для "
                    + p.getName() + ": " + e.getMessage());
        }
    }
}
