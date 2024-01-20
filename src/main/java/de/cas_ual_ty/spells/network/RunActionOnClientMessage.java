package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RunActionOnClientMessage(SyncedSpellActionType<?, ?> actionType, IClientAction action)
{
    public static void encode(RunActionOnClientMessage msg, FriendlyByteBuf buf)
    {
        buf.writeRegistryId(msg.actionType());
        msg.action().writeToBuf(buf);
    }
    
    public static RunActionOnClientMessage decode(FriendlyByteBuf buf)
    {
        SpellActionType<?> type = buf.readRegistryId();
        
        if(type instanceof SyncedSpellActionType<?, ?> syncedType)
        {
            IClientAction action = syncedType.makeClientInstance();
            action.readFromBuf(buf);
            return new RunActionOnClientMessage(syncedType, action);
        }
        
        return new RunActionOnClientMessage(null, null);
    }
    
    public static void handle(RunActionOnClientMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleSpellAction(msg)).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
        context.get().setPacketHandled(true);
    }
}
