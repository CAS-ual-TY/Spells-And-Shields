package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.util.SpellHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FireSpellMessage(int spell) implements CustomPacketPayload
{
    public static final Type<FireSpellMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "fire_spell"));
    public static final StreamCodec<FriendlyByteBuf, FireSpellMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeInt(msg.spell()),
            buf -> new FireSpellMessage(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(FireSpellMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if(context.player() instanceof ServerPlayer player)
            {
                SpellHelper.fireSpellSlot(player, msg.spell());
            }
        }).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
    }
}
