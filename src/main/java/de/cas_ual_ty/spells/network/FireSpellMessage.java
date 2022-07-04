package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.spell.base.SpellHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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
    
    public static void handle(FireSpellMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer player = context.get().getSender();
            
            if(player != null)
            {
                SpellHelper.fireSpellSlot(player, msg.spell());
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
