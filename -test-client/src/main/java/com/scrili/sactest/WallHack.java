package com.scrili.sactest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = "sac_test", value = Dist.CLIENT)
public class WallHack {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!CheatManager.wallHack) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack pose = event.getPoseStack();
        LocalPlayer self = mc.player;
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player target) || target == self) continue;
            AABB box = target.getBoundingBox();
            float pt = event.getPartialTick();
            double x = lerp(target.xOld, target.getX(), pt) - camX;
            double y = lerp(target.yOld, target.getY(), pt) - camY;
            double z = lerp(target.zOld, target.getZ(), pt) - camZ;
            double w = (box.maxX - box.minX) / 2.0;
            double h = box.maxY - box.minY;

            pose.pushPose();
            pose.translate(x, y, z);
            Matrix4f mat = pose.last().pose();
            buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            drawBox(buf, mat, (float)-w, 0f, (float)-w, (float)w, (float)h, (float)w, 1f, 0.2f, 0.2f, 0.8f);
            tess.end();
            pose.popPose();
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawBox(BufferBuilder b, Matrix4f m,
                                float x1,float y1,float z1,float x2,float y2,float z2,
                                float r,float g,float bl,float a) {
        line(b,m,x1,y1,z1,x2,y1,z1,r,g,bl,a); line(b,m,x2,y1,z1,x2,y1,z2,r,g,bl,a);
        line(b,m,x2,y1,z2,x1,y1,z2,r,g,bl,a); line(b,m,x1,y1,z2,x1,y1,z1,r,g,bl,a);
        line(b,m,x1,y2,z1,x2,y2,z1,r,g,bl,a); line(b,m,x2,y2,z1,x2,y2,z2,r,g,bl,a);
        line(b,m,x2,y2,z2,x1,y2,z2,r,g,bl,a); line(b,m,x1,y2,z2,x1,y2,z1,r,g,bl,a);
        line(b,m,x1,y1,z1,x1,y2,z1,r,g,bl,a); line(b,m,x2,y1,z1,x2,y2,z1,r,g,bl,a);
        line(b,m,x2,y1,z2,x2,y2,z2,r,g,bl,a); line(b,m,x1,y1,z2,x1,y2,z2,r,g,bl,a);
    }

    private static void line(BufferBuilder b, Matrix4f m,
                              float x1,float y1,float z1,float x2,float y2,float z2,
                              float r,float g,float bl,float a) {
        b.vertex(m,x1,y1,z1).color(r,g,bl,a).endVertex();
        b.vertex(m,x2,y2,z2).color(r,g,bl,a).endVertex();
    }

    private static double lerp(double a, double b, float t) { return a + (b-a)*t; }
}
