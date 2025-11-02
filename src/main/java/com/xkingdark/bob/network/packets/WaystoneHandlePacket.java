package com.xkingdark.bob.network.packets;

import com.xkingdark.bob.blocks.Blocks;
import com.xkingdark.bob.items.Items;
import com.xkingdark.bob.network.Packets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record WaystoneHandlePacket(
    Boolean isFromBlock,
    BlockPos blockPos,
    int index
) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, WaystoneHandlePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN, WaystoneHandlePacket::isFromBlock,
        BlockPos.PACKET_CODEC, WaystoneHandlePacket::blockPos,
        PacketCodecs.INTEGER, WaystoneHandlePacket::index,
        WaystoneHandlePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Packets.WAYSTONE_HANDLE_PACKET;
    }

    public static void handle(WaystoneHandlePacket payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        World world = player.getEntityWorld();

        ItemStack mainHand = player.getMainHandStack();
        BlockState state = world.getBlockState(payload.blockPos);
        if (!payload.isFromBlock && !mainHand.getItem().equals(Items.WAYSTONE))
            return;

        if (payload.isFromBlock && !state.getBlock().equals(Blocks.WAYSTONE))
            return;

        context.player().sendMessage(Text.of("Index: " + payload.index));
    }
}
