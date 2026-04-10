package com.scrili.sactest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FreecamTrigger {
    public static void trigger() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        Vec3 look = player.getLookAngle();
        BlockPos farBlock = BlockPos.containing(
            player.getX() + look.x * 20,
            player.getY() + look.y * 20,
            player.getZ() + look.z * 20
        );
        player.connection.send(new ServerboundUseItemOnPacket(
            InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(farBlock), Direction.UP, farBlock, false),
            0
        ));
    }
}
