package ru.khozain.inhgraphics;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Персональные уровни дождя и грома через пакеты ClientboundGameEventPacket.
 * Bukkit API умеет включать/выключать дождь per-player (setPlayerWeather),
 * но уровень грома (тёмное грозовое небо) есть только в протоколе.
 * Работаем через рефлексию по Mojang-маппингам рантайма Paper.
 * Если что-то сломалось — плагин продолжает работать, просто без
 * персонального уровня грома.
 */
public final class NmsBridge {

    private static final boolean AVAILABLE;
    private static Constructor<?> ctor;
    private static final java.util.Map<String, Object> TYPES = new java.util.HashMap<>();

    /** Имена полей ClientboundGameEventPacket, нужные плагину. */
    public static final String START_RAINING = "START_RAINING";
    public static final String STOP_RAINING = "STOP_RAINING";
    public static final String RAIN_LEVEL_CHANGE = "RAIN_LEVEL_CHANGE";
    public static final String THUNDER_LEVEL_CHANGE = "THUNDER_LEVEL_CHANGE";

    static {
        // флаг для диагностики: -Dinhgraphics.no-nms=true отключает мост
        boolean disabled = Boolean.getBoolean("inhgraphics.no-nms");
        boolean ok = false;
        try {
            Class<?> pkt = Class.forName("net.minecraft.network.protocol.game.ClientboundGameEventPacket");
            for (String name : new String[]{START_RAINING, STOP_RAINING,
                    RAIN_LEVEL_CHANGE, THUNDER_LEVEL_CHANGE}) {
                TYPES.put(name, pkt.getField(name).get(null));
            }
            Object sampleType = TYPES.get(START_RAINING);
            ctor = pkt.getDeclaredConstructor(sampleType.getClass(), float.class);
            ctor.setAccessible(true);
            ctor.newInstance(sampleType, 0.0f);
            ok = true;
        } catch (Throwable t) {
            ok = false;
        }
        AVAILABLE = ok && !disabled;
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    /**
     * Слать игроку игровой event.
     *
     * @param fieldName одно из START_RAINING / STOP_RAINING / RAIN_LEVEL_CHANGE / THUNDER_LEVEL_CHANGE
     * @param value     параметр события
     */
    public static void send(Player player, String fieldName, float value) {
        if (!AVAILABLE) return;
        try {
            Object type = TYPES.get(fieldName);
            if (type == null) return;
            Object packet = ctor.newInstance(type, value);
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("connection").get(handle);
            for (Method m : connection.getClass().getMethods()) {
                if ("send".equals(m.getName()) && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isInstance(packet)) {
                    m.invoke(connection, packet);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // молча: косметика не критична
        }
    }
}
