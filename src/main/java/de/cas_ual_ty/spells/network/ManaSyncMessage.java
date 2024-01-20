package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ManaSyncMessage(int entityId, float mana, float extraMana) implements CustomPacketPayload
{
    public static final Type<ManaSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "mana_sync"));
    public static final StreamCodec<FriendlyByteBuf, ManaSyncMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) ->
            {
                buf.writeInt(msg.entityId());
                buf.writeFloat(msg.mana());
                buf.writeFloat(msg.extraMana());
            },
            buf -> new ManaSyncMessage(buf.readInt(), buf.readFloat(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(ManaSyncMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleManaSync(msg)).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
    }
}
