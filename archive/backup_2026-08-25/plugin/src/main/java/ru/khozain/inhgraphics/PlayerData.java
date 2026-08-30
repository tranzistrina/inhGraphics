package ru.khozain.inhgraphics;

/**
 * Персональные настройки одного игрока. Всё, что видно только ему:
 * время, погода, дальность прорисовки, туман из частиц.
 */
public final class PlayerData {

    /** Персональное время суток: СДВИГ в тиках относительно мирового времени, -1 = мировое. */
    public long timeTicks = -1L;

    /** NONE | CLEAR | RAIN | THUNDER */
    public String weather = "NONE";

    /** Персональная дальность отправки чанков, -1 = мировая. Диапазон 2..32. */
    public int renderDistance = -1;

    /** Исходная дальность, запомненная до первого ограничения (для сброса). */
    public int defaultRender = -1;

    /** Включён ли персональный туман из частиц. */
    public boolean fogOn = false;

    /** Плотность тумана 1..10. */
    public int fogDensity = 5;

    /** Полный сброс к мировому состоянию. */
    public void reset() {
        timeTicks = -1L;
        weather = "NONE";
        renderDistance = -1;
        defaultRender = -1;
        fogOn = false;
        fogDensity = 5;
    }

    public boolean isDirty() {
        return timeTicks >= 0 || !"NONE".equals(weather)
                || renderDistance >= 2 || fogOn;
    }
}
