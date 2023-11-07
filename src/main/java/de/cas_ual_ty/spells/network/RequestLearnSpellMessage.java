package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

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
    
    public static void handle(RequestLearnSpellMessage msg, NetworkEvent.Context context)
    {
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            
            if(player == null)
            {
                return;
            }
            
            if(player.containerMenu instanceof SpellProgressionMenu menu)
            {
                menu.buySpellRequest(msg.nodeId());
            }
        });
        
        context.setPacketHandled(true);
    }
}
