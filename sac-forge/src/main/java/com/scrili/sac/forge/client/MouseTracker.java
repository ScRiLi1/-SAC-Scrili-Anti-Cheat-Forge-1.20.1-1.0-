package com.scrili.sac.forge.client;

import com.scrili.sac.forge.network.MouseDataPacket;
import com.scrili.sac.forge.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;

/**
 * Клиентский трекер движения мыши.
 *
 * Каждый тик:
 * 1. Читаем позицию курсора через glfwGetCursorPos (raw пиксели)
 * 2. Вычисляем дельту от предыдущего тика
 * 3. Записываем изменение угла камеры (deltaYaw/deltaPitch)
 *
 * Каждые 20 тиков отправляем всё на сервер.
 */
@Mod.EventBusSubscriber(modid = "sac", value = Dist.CLIENT)
public class MouseTracker {

    private static final int SEND_INTERVAL = 20;

    // Raw позиция курсора
    private static double lastCursorX = Double.NaN;
    private static double lastCursorY = Double.NaN;

    // Угол камеры
    private static float lastYaw   = Float.NaN;
    private static float lastPitch = Float.NaN;

    // Буферы за текущий интервал
    private static final float[] rawDX      = new float[SEND_INTERVAL];
    private static final float[] rawDY      = new float[SEND_INTERVAL];
    private static final float[] deltaYaw   = new float[SEND_INTERVAL];
    private static final float[] deltaPitch = new float[SEND_INTERVAL];
    private static int idx = 0;

    private static final DoubleBuffer xBuf = BufferUtils.createDoubleBuffer(1);
    private static final DoubleBuffer yBuf = BufferUtils.createDoubleBuffer(1);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        long window = mc.getWindow().getWindow();

        // Читаем позицию курсора
        xBuf.clear(); yBuf.clear();
        GLFW.glfwGetCursorPos(window, xBuf, yBuf);
        double cx = xBuf.get(0);
        double cy = yBuf.get(0);

        float yaw   = mc.player.getYRot();
        float pitch = mc.player.getXRot();

        if (!Double.isNaN(lastCursorX) && !Float.isNaN(lastYaw)) {
            rawDX[idx]      = (float)(cx - lastCursorX);
            rawDY[idx]      = (float)(cy - lastCursorY);
            deltaYaw[idx]   = angleDelta(yaw, lastYaw);
            deltaPitch[idx] = angleDelta(pitch, lastPitch);
            idx++;
        }

        lastCursorX = cx;
        lastCursorY = cy;
        lastYaw     = yaw;
        lastPitch   = pitch;

        if (idx >= SEND_INTERVAL) {
            // Отправляем только реально заполненные данные
            float[] sendRawDX      = java.util.Arrays.copyOf(rawDX, idx);
            float[] sendRawDY      = java.util.Arrays.copyOf(rawDY, idx);
            float[] sendDeltaYaw   = java.util.Arrays.copyOf(deltaYaw, idx);
            float[] sendDeltaPitch = java.util.Arrays.copyOf(deltaPitch, idx);
            PacketHandler.CHANNEL.sendToServer(new MouseDataPacket(
                sendRawDX, sendRawDY, sendDeltaYaw, sendDeltaPitch
            ));
            idx = 0;
        }
    }

    private static float angleDelta(float a, float b) {
        float d = a - b;
        while (d > 180)  d -= 360;
        while (d < -180) d += 360;
        return d;
    }
}
