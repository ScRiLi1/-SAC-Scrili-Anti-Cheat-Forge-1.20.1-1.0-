package com.scrili.sac.forge.network;

import com.scrili.sac.forge.SACForge;
import com.scrili.sac.forge.adapter.ForgePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CpsPacket {

    private final int cps;

    public CpsPacket(int cps) {
        this.cps = cps;
    }

    public static void encode(CpsPacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.cps);
    }

    public static CpsPacket decode(FriendlyByteBuf buf) {
        return new CpsPacket(buf.readInt());
    }

    public static void handle(CpsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SACForge.getCore().getAutoClickerCheck()
                .check(new ForgePlayer(player), packet.cps);
        });
        ctx.get().setPacketHandled(true);
    }
}
