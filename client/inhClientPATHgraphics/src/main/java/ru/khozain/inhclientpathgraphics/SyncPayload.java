package ru.khozain.inhclientpathgraphics;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Arrays;

final class SyncPayload implements CustomPacketPayload {
    static final CustomPacketPayload.Type<SyncPayload> ID = new CustomPacketPayload.Type<>(InhClientPathGraphics.SYNC_ID);
    static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.bytes),
            buf -> new SyncPayload(readBytes(buf))
    );

    final byte[] bytes;

    SyncPayload(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private static byte[] readBytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }
}
