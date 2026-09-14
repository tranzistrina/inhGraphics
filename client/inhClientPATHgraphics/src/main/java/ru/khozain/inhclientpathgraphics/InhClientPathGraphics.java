package ru.khozain.inhclientpathgraphics;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Необязательный клиентский компаньон inhGraphics.
 * Он не меняет серверную логику и молчит на серверах без канала inhGraphics.
 */
public final class InhClientPathGraphics implements ClientModInitializer {
    static final Identifier SYNC_ID = Identifier.fromNamespaceAndPath("inhgraphics", "sync");
    static final Identifier MODS_ID = Identifier.fromNamespaceAndPath("inhgraphics", "mods");

    private static final String SYNC_MAGIC = "INHGFX_SYNC_V1";
    private static final String REPORT_MAGIC = "INHGFX_MODS_V1";
    private static final int MAX_MODS = 512;
    private static int originalRenderDistance = -1;
    private static boolean serverEnforced;
    private static int serverDistance = -1;

    public InhClientPathGraphics() {
    }

    @Override
    public void onInitializeClient() {
        initialize();
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(SyncPayload.ID, SyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ModsPayload.ID, ModsPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) ->
                context.client().execute(() -> applySync(payload.bytes)));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(InhClientPathGraphics::sendModReport));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetLocalState());
        ClientTickEvents.END_CLIENT_TICK.register(client -> enforceRenderDistance(client));
    }

    private static void applySync(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (!SYNC_MAGIC.equals(in.readUTF())) return;
            int distance = Math.max(2, Math.min(32, in.readInt()));
            boolean enforced = in.readBoolean();
            boolean cacheSync = in.readBoolean();
            in.readUTF(); // версия серверного плагина для диагностики/совместимости

            Minecraft client = Minecraft.getInstance();
            if (enforced && originalRenderDistance < 0) {
                originalRenderDistance = client.options.renderDistance().get();
            }
            serverEnforced = enforced && cacheSync;
            serverDistance = distance;
            if (serverEnforced) {
                ThirdPartyCompat.enableRestrictedMode();
                client.options.renderDistance().set(Math.min(originalRenderDistance, distance));
                client.options.save();
                client.player.sendSystemMessage(Component.literal(
                        "§7inhGraphics: клиентский рендер ограничен до " + distance + " чанков"));
            } else if (originalRenderDistance >= 0) {
                ThirdPartyCompat.disableRestrictedMode();
                client.options.renderDistance().set(originalRenderDistance);
                client.options.save();
                originalRenderDistance = -1;
                serverDistance = -1;
            }
        } catch (IOException | RuntimeException ignored) {
            // Серверный payload не должен ломать клиент.
        }
    }

    private static void enforceRenderDistance(Minecraft client) {
        if (serverEnforced && serverDistance >= 2
                && client.options.renderDistance().get() > serverDistance) {
            client.options.renderDistance().set(serverDistance);
        }
    }

    private static void resetLocalState() {
        Minecraft client = Minecraft.getInstance();
        ThirdPartyCompat.disableRestrictedMode();
        if (originalRenderDistance >= 0) {
            client.options.renderDistance().set(originalRenderDistance);
            client.options.save();
        }
        originalRenderDistance = -1;
        serverEnforced = false;
        serverDistance = -1;
    }

    private static void sendModReport() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF(REPORT_MAGIC);
            out.writeUTF("Fabric");
            List<ModContainer> mods = FabricLoader.getInstance().getAllMods().stream()
                    .sorted(Comparator.comparing(m -> m.getMetadata().getId()))
                    .limit(MAX_MODS)
                    .toList();
            out.writeInt(mods.size());
            for (ModContainer mod : mods) {
                out.writeUTF(limit(mod.getMetadata().getId()));
                out.writeUTF(limit(mod.getMetadata().getVersion().getFriendlyString()));
            }
            out.flush();
            ClientPlayNetworking.send(new ModsPayload(bytes.toByteArray()));
        } catch (IOException | RuntimeException ignored) {
            // Vanilla/неподдерживающий сервер — обычный безопасный сценарий.
        }
    }

    private static String limit(String value) {
        return value.length() <= 256 ? value : value.substring(0, 256);
    }
}
