package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestLearnSpellMessage(SpellNodeId nodeId)
{
    public static void encode(RequestLearnSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(msg.nodeId().treeId());
        buf.writeShort(msg.nodeId().nodeId());
    }
    
    public static RequestLearnSpellMessage decode(FriendlyByteBuf buf)
    {
        return new RequestLearnSpellMessage(new SpellNodeId(buf.readResourceLocation(), buf.readShort()));
    }
    
    public static void handle(RequestLearnSpellMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer player = context.get().getSender();
            
            if(player == null)
            {
                return;
            }
            
            if(player.containerMenu instanceof SpellProgressionMenu menu)
            {
                menu.buySpellRequest(msg.nodeId());
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
