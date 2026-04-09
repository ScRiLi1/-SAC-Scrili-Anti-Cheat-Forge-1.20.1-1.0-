package com.scrili.sactest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "sac_test", value = Dist.CLIENT)
public class CheatController {

    private static final float SMOOTH_SPEED = 8.0f;
    private static final float TIMER_SPEED  = 1.5f;
    private static float timerAccum = 0f;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getKey() == GLFW.GLFW_KEY_INSERT && event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new CheatMenuScreen());
            } else if (mc.screen instanceof CheatMenuScreen) {
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        LocalPlayer player = mc.player;

        // AimBot
        if (CheatManager.aimBot) {
            Player target = findNearest(mc);
            if (target != null) {
                if (CheatManager.aimBotMode == 0) snapAim(player, target);
                else smoothAim(player, target);
            }
        }

        // Silent Aim
        if (CheatManager.silentAim) {
            Player target = findNearest(mc);
            if (target != null) {
                float realYaw = player.getYRot(), realPitch = player.getXRot();
                float[] a = calcAngles(player, target);
                player.setYRot(a[0]); player.setXRot(a[1]);
                player.yRotO = a[0];  player.xRotO = a[1];
                mc.submitAsync(() -> {
                    player.setYRot(realYaw); player.setXRot(realPitch);
                    player.yRotO = realYaw;  player.xRotO = realPitch;
                });
            }
        }

        // Fly
        if (CheatManager.fly) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            if (mc.options.keyJump.isDown())
                player.setDeltaMovement(player.getDeltaMovement().x, 0.2, player.getDeltaMovement().z);
            else if (mc.options.keyShift.isDown())
                player.setDeltaMovement(player.getDeltaMovement().x, -0.2, player.getDeltaMovement().z);
            player.setOnGround(true);
        }

        // Velocity
        if (CheatManager.velocity) {
            double hs = Math.sqrt(player.getDeltaMovement().x * player.getDeltaMovement().x
                                + player.getDeltaMovement().z * player.getDeltaMovement().z);
            if (hs > 0.3) player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        }

        // Timer
        if (CheatManager.timer) {
            timerAccum += (TIMER_SPEED - 1.0f);
            while (timerAccum >= 1.0f) {
                timerAccum -= 1.0f;
                player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    player.getX(), player.getY(), player.getZ(),
                    player.getYRot(), player.getXRot(), player.onGround()
                ));
            }
        }
    }

    private static void snapAim(LocalPlayer p, Player t) {
        float[] a = calcAngles(p, t);
        p.setYRot(a[0]); p.setXRot(a[1]); p.yRotO = a[0]; p.xRotO = a[1];
    }

    private static void smoothAim(LocalPlayer p, Player t) {
        float[] a = calcAngles(p, t);
        float dy = angleDelta(a[0], p.getYRot()), dp = angleDelta(a[1], p.getXRot());
        p.setYRot(p.getYRot() + Math.min(Math.abs(dy), SMOOTH_SPEED) * Math.signum(dy));
        p.setXRot(p.getXRot() + Math.min(Math.abs(dp), SMOOTH_SPEED) * Math.signum(dp));
    }

    private static Player findNearest(Minecraft mc) {
        Player nearest = null; double min = 100.0 * 100.0;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Player p) || p == mc.player) continue;
            double d = mc.player.distanceToSqr(p);
            if (d < min) { min = d; nearest = p; }
        }
        return nearest;
    }

    private static float[] calcAngles(LocalPlayer p, Player t) {
        Vec3 from = p.getEyePosition(), to = t.getEyePosition();
        double dx = to.x-from.x, dy = to.y-from.y, dz = to.z-from.z;
        double dist = Math.sqrt(dx*dx + dz*dz);
        return new float[]{ (float)Math.toDegrees(Math.atan2(-dx, dz)),
                            (float)Math.toDegrees(-Math.atan2(dy, dist)) };
    }

    private static float angleDelta(float a, float b) {
        float d = a - b;
        while (d > 180) d -= 360; while (d < -180) d += 360;
        return d;
    }
}
