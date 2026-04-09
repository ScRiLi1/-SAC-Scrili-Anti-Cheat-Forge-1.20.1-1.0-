package com.scrili.sac.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("sac", "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(0, ModListPacket.class,
            ModListPacket::encode, ModListPacket::decode, ModListPacket::handle);
        CHANNEL.registerMessage(1, CpsPacket.class,
            CpsPacket::encode, CpsPacket::decode, CpsPacket::handle);
        CHANNEL.registerMessage(2, MouseDataPacket.class,
            MouseDataPacket::encode, MouseDataPacket::decode, MouseDataPacket::handle);
    }
}
