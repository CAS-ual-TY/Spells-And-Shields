package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.util.SpellHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

public record FireSpellMessage(int spell)
{
    public static void encode(FireSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.spell());
    }
    
    public static FireSpellMessage decode(FriendlyByteBuf buf)
    {
        return new FireSpellMessage(buf.readInt());
    }
    
    public static void handle(FireSpellMessage msg, NetworkEvent.Context context)
    {
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            
            if(player != null)
            {
                SpellHelper.fireSpellSlot(player, msg.spell());
            }
        });
        
        context.setPacketHandled(true);
    }
}
