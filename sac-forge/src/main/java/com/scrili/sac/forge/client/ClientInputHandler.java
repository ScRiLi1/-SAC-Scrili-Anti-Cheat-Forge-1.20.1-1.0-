package com.scrili.sac.forge.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "sac", value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        // левая кнопка мыши — атака
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == GLFW.GLFW_PRESS) {
            CpsTracker.onClick();
        }
    }
}
