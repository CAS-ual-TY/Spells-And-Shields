package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spelltree.FullSpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestLearnSpellMessage(FullSpellNodeId nodeId) implements CustomPacketPayload
{
    public static final Type<RequestLearnSpellMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "request_learn_spell"));
    public static final StreamCodec<FriendlyByteBuf, RequestLearnSpellMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) ->
            {
                buf.writeResourceLocation(msg.nodeId().treeId());
                buf.writeResourceLocation(msg.nodeId().nodeId());
            },
            buf -> new RequestLearnSpellMessage(new FullSpellNodeId(buf.readResourceLocation(), buf.readResourceLocation()))
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(RequestLearnSpellMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            ServerPlayer player = (ServerPlayer) context.player();
            if(player.containerMenu instanceof SpellProgressionMenu menu)
            {
                menu.buySpellRequest(msg.nodeId());
            }
        }).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
    }
}
