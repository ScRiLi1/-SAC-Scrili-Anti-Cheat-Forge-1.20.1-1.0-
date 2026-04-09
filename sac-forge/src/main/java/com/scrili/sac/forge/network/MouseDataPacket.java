package com.scrili.sac.forge.network;

import com.scrili.sac.forge.SACForge;
import com.scrili.sac.forge.adapter.ForgePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Пакет с данными движения мыши за последние N тиков.
 *
 * Содержит:
 * - rawDX/rawDY — реальные пиксели мыши (до применения чувствительности)
 * - deltaYaw/deltaPitch — изменение угла камеры за тик
 *
 * Ключевой принцип детекта:
 * Если deltaYaw != 0, но rawDX == 0 — камера двигается без мыши = аим-бот.
 */
public class MouseDataPacket {

    public final float[] rawDX;      // пиксели мыши по X за каждый тик
    public final float[] rawDY;      // пиксели мыши по Y за каждый тик
    public final float[] deltaYaw;   // изменение yaw за каждый тик
    public final float[] deltaPitch; // изменение pitch за каждый тик

    public MouseDataPacket(float[] rawDX, float[] rawDY, float[] deltaYaw, float[] deltaPitch) {
        this.rawDX      = rawDX;
        this.rawDY      = rawDY;
        this.deltaYaw   = deltaYaw;
        this.deltaPitch = deltaPitch;
    }

    public static void encode(MouseDataPacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.rawDX.length);
        for (float v : p.rawDX)      buf.writeFloat(v);
        for (float v : p.rawDY)      buf.writeFloat(v);
        for (float v : p.deltaYaw)   buf.writeFloat(v);
        for (float v : p.deltaPitch) buf.writeFloat(v);
    }

    public static MouseDataPacket decode(FriendlyByteBuf buf) {
        int len = buf.readInt();
        // Защита от некорректных данных
        if (len <= 0 || len > 100) {
            return new MouseDataPacket(new float[0], new float[0], new float[0], new float[0]);
        }
        // Проверяем что в буфере достаточно байт (4 массива * len * 4 байта)
        if (buf.readableBytes() < len * 4 * 4) {
            return new MouseDataPacket(new float[0], new float[0], new float[0], new float[0]);
        }
        float[] rawDX      = new float[len];
        float[] rawDY      = new float[len];
        float[] deltaYaw   = new float[len];
        float[] deltaPitch = new float[len];
        for (int i = 0; i < len; i++) rawDX[i]      = buf.readFloat();
        for (int i = 0; i < len; i++) rawDY[i]      = buf.readFloat();
        for (int i = 0; i < len; i++) deltaYaw[i]   = buf.readFloat();
        for (int i = 0; i < len; i++) deltaPitch[i]  = buf.readFloat();
        return new MouseDataPacket(rawDX, rawDY, deltaYaw, deltaPitch);
    }

    public static void handle(MouseDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SACForge.getCore().getMouseAimCheck()
                .analyze(new ForgePlayer(player), packet.rawDX, packet.rawDY,
                         packet.deltaYaw, packet.deltaPitch);
        });
        ctx.get().setPacketHandled(true);
    }
}
