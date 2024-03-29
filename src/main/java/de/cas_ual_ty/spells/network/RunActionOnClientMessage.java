package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.IClientAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.SyncedSpellActionType;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public record RunActionOnClientMessage(SyncedSpellActionType<?, ?> actionType, IClientAction action)
{
    public static void encode(RunActionOnClientMessage msg, FriendlyByteBuf buf)
    {
        buf.writeById(SpellActionTypes.REGISTRY::getId, msg.actionType());
        msg.action().writeToBuf(buf);
    }
    
    public static RunActionOnClientMessage decode(FriendlyByteBuf buf)
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
    
    public static void handle(RunActionOnClientMessage msg, NetworkEvent.Context context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleSpellAction(msg));
        context.setPacketHandled(true);
    }
}
