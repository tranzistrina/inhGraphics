package ru.khozain.inhgraphics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Обмен совместимостью с необязательным клиентским модом inhClientPATHgraphics.
 * Vanilla-клиенты этот канал игнорируют и продолжают работать как раньше.
 */
public final class ClientModRegistry implements PluginMessageListener {
    public static final String REPORT_CHANNEL = "inhgraphics:mods";
    public static final String SYNC_CHANNEL = "inhgraphics:sync";
    private static final String REPORT_MAGIC = "INHGFX_MODS_V1";
    private static final String SYNC_MAGIC = "INHGFX_SYNC_V1";
    private static final int MAX_MODS = 512;
    private static final int MAX_STRING = 256;

    private final InhGraphicsPlugin plugin;
    private final Map<UUID, ClientReport> reports = new LinkedHashMap<>();

    public ClientModRegistry(InhGraphicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, REPORT_CHANNEL, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, SYNC_CHANNEL);
    }

    public void unregister() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, REPORT_CHANNEL, this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, SYNC_CHANNEL);
        reports.clear();
    }

    /** Отправляет клиентскому моду текущий серверный лимит отправки чанков. */
    public void sendSync(Player player) {
        PlayerData data = plugin.getStore().get(player.getUniqueId());
        int sendDistance = data.renderDistance >= 2
                ? data.renderDistance
                : Math.max(2, player.getSendViewDistance());
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF(SYNC_MAGIC);
            out.writeInt(sendDistance);
            out.writeBoolean(data.renderDistance >= 2);
            out.writeBoolean(plugin.getConfig().getBoolean("compatibility.client-cache-sync", true));
            out.writeUTF(plugin.getDescription().getVersion());
            out.flush();
            player.sendPluginMessage(plugin, SYNC_CHANNEL, bytes.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось подготовить sync для " + player.getName() + ": " + e.getMessage());
        }
    }

    public ClientReport get(Player player) {
        return reports.get(player.getUniqueId());
    }

    public void remove(Player player) {
        reports.remove(player.getUniqueId());
    }

    public List<ClientReport> reports() {
        return List.copyOf(reports.values());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!REPORT_CHANNEL.equals(channel) || message.length > 64 * 1024) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            if (!REPORT_MAGIC.equals(in.readUTF())) return;
            String brand = readBounded(in);
            int count = Math.max(0, Math.min(MAX_MODS, in.readInt()));
            Map<String, String> mods = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                String id = readBounded(in);
                String version = readBounded(in);
                if (!id.isBlank()) mods.put(id, version);
            }
            reports.put(player.getUniqueId(), new ClientReport(player.getUniqueId(), brand, mods));
            if (plugin.getConfig().getBoolean("compatibility.log-client-reports", false)) {
                plugin.getLogger().info("Моды клиента " + player.getName() + ": " + mods.keySet());
            }
        } catch (IOException | RuntimeException ignored) {
            // Клиентские сообщения не должны ломать сервер и не считаются доверенными.
        }
    }

    private String readBounded(DataInputStream in) throws IOException {
        String value = in.readUTF();
        return value.length() > MAX_STRING ? value.substring(0, MAX_STRING) : value;
    }

    public record ClientReport(UUID playerId, String brand, Map<String, String> mods) {
        public ClientReport {
            mods = Collections.unmodifiableMap(new LinkedHashMap<>(mods));
        }

        public boolean has(String id) {
            return mods.containsKey(id);
        }

        public String summary() {
            return mods.isEmpty() ? "нет данных" : String.join(", ", mods.keySet());
        }
    }
}
