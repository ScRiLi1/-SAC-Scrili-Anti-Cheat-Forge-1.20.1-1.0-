package com.scrili.sac.forge.network;

import com.scrili.sac.forge.SACForge;
import com.scrili.sac.forge.adapter.ForgePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModListPacket {

    private final List<String> modIds;

    public ModListPacket(List<String> modIds) {
        this.modIds = modIds;
    }

    public static void encode(ModListPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.modIds.size());
        for (String id : packet.modIds) buf.writeUtf(id);
    }

    public static ModListPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < size; i++) ids.add(buf.readUtf());
        return new ModListPacket(ids);
    }

    public static void handle(ModListPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SACForge.getCore().getModBlacklistCheck()
                .check(new ForgePlayer(player), packet.modIds);
        });
        ctx.get().setPacketHandled(true);
    }
}
