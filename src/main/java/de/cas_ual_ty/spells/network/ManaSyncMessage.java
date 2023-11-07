package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public record ManaSyncMessage(int entityId, float mana, float extraMana)
{
    public static void encode(ManaSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId());
        buf.writeFloat(msg.mana());
        buf.writeFloat(msg.extraMana());
    }
    
    public static ManaSyncMessage decode(FriendlyByteBuf buf)
    {
        return new ManaSyncMessage(buf.readInt(), buf.readFloat(), buf.readFloat());
    }
    
    public static void handle(ManaSyncMessage msg, NetworkEvent.Context context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleManaSync(msg));
        context.setPacketHandled(true);
    }
}
