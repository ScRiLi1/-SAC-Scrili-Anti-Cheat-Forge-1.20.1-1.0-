package com.scrili.sac.forge.client;

import com.scrili.sac.forge.network.CpsPacket;
import com.scrili.sac.forge.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;

@Mod.EventBusSubscriber(modid = "sac", value = Dist.CLIENT)
public class CpsTracker {

    private static final Deque<Long> clickTimes = new ArrayDeque<>();
    private static int tickCounter = 0;

    /** Вызывается при каждом клике мышью (из InputEvent) */
    public static void onClick() {
        clickTimes.addLast(System.currentTimeMillis());
    }

    /** Каждую секунду (20 тиков) отправляем CPS на сервер */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        long now = System.currentTimeMillis();
        // убираем клики старше 1 секунды
        clickTimes.removeIf(t -> now - t > 1000);

        int cps = clickTimes.size();
        PacketHandler.CHANNEL.sendToServer(new CpsPacket(cps));
    }
}
