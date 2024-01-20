package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RunActionOnClientMessage(SyncedSpellActionType<?, ?> actionType, IClientAction action) implements CustomPacketPayload
{
    public static final Type<RunActionOnClientMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "run_action_on_client"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RunActionOnClientMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) ->
            {
                buf.writeById(SpellActionTypes.REGISTRY::getId, msg.actionType());
                msg.action().writeToBuf(buf);
            },
            buf ->
            {
                SpellActionType<?> type = buf.readById(SpellActionTypes.REGISTRY::byId);
                if(type instanceof SyncedSpellActionType<?, ?> syncedType)
                {
                    IClientAction action = syncedType.makeClientInstance();
                    action.readFromBuf(buf);
                    return new RunActionOnClientMessage(syncedType, action);
                }
                return new RunActionOnClientMessage(null, null);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(RunActionOnClientMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleSpellAction(msg)).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
    }
}
