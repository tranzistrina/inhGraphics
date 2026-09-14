package ru.khozain.inhclientpathgraphics;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Best-effort adapters for cache/LOD mods; every operation is optional and guarded. */
final class ThirdPartyCompat {
    private static Integer bobbyUnloadDelay;
    private static Boolean voxyRendering;

    private ThirdPartyCompat() {
    }

    static void enableRestrictedMode() {
        setBobbyUnloadDelay(0);
        setVoxyRendering(false);
    }

    static void disableRestrictedMode() {
        setBobbyUnloadDelay(bobbyUnloadDelay == null ? null : bobbyUnloadDelay);
        setVoxyRendering(voxyRendering == null ? null : voxyRendering);
        bobbyUnloadDelay = null;
        voxyRendering = null;
    }

    private static void setBobbyUnloadDelay(Integer value) {
        if (!FabricLoader.getInstance().isModLoaded("bobby")) return;
        try {
            Class<?> bobby = Class.forName("de.johni0702.minecraft.bobby.Bobby");
            Object instance = bobby.getMethod("getInstance").invoke(null);
            Object config = bobby.getMethod("getConfig").invoke(instance);
            Field field = config.getClass().getDeclaredField("unloadDelaySecs");
            field.setAccessible(true);
            if (bobbyUnloadDelay == null) bobbyUnloadDelay = field.getInt(config);
            field.setInt(config, value == null ? bobbyUnloadDelay : value);
        } catch (Throwable ignored) {
            // Bobby internals are version-specific; vanilla rendering remains safe.
        }
    }

    private static void setVoxyRendering(Boolean enabled) {
        if (!FabricLoader.getInstance().isModLoaded("voxy")) return;
        try {
            Class<?> configClass = Class.forName("me.cortex.voxy.client.config.VoxyConfig");
            Field configField = configClass.getField("CONFIG");
            Object config = configField.get(null);
            Field rendering = configClass.getField("enableRendering");
            if (voxyRendering == null) voxyRendering = rendering.getBoolean(config);
            rendering.setBoolean(config, enabled == null ? voxyRendering : enabled);
            try {
                Method save = configClass.getMethod("save");
                save.invoke(config);
            } catch (NoSuchMethodException ignored) {
                // Older Voxy builds have no public save method.
            }
        } catch (Throwable ignored) {
            // Voxy is optional and its API is not stable across builds.
        }
    }
}
