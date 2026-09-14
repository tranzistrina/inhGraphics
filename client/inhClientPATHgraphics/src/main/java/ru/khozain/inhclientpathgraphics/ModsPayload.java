package ru.khozain.inhclientpathgraphics;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

final class ModsPayload implements CustomPacketPayload {
    static final CustomPacketPayload.Type<ModsPayload> ID = new CustomPacketPayload.Type<>(InhClientPathGraphics.MODS_ID);
    static final StreamCodec<RegistryFriendlyByteBuf, ModsPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.bytes),
            buf -> new ModsPayload(readBytes(buf))
    );

    final byte[] bytes;

    ModsPayload(byte[] bytes) {
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
