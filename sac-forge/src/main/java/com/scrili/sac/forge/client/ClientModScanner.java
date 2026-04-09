package com.scrili.sac.forge.client;

import com.scrili.sac.forge.network.ModListPacket;
import com.scrili.sac.forge.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "sac", value = Dist.CLIENT)
public class ClientModScanner {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Выполняем только на клиентской стороне
        if (!net.minecraft.client.Minecraft.getInstance().isSameThread()) return;

        List<String> modIds = ModList.get().getMods()
            .stream()
            .map(info -> info.getModId().toLowerCase())
            .collect(Collectors.toList());

        PacketHandler.CHANNEL.sendToServer(new ModListPacket(modIds));
    }
}
