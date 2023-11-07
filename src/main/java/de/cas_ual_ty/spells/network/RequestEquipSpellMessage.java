package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

public record RequestEquipSpellMessage(byte slot, SpellNodeId nodeId)
{
    public static void encode(RequestEquipSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeByte(msg.slot());
        buf.writeResourceLocation(msg.nodeId().treeId());
        buf.writeShort(msg.nodeId().nodeId());
    }
    
    public static RequestEquipSpellMessage decode(FriendlyByteBuf buf)
    {
        return new RequestEquipSpellMessage(buf.readByte(), new SpellNodeId(buf.readResourceLocation(), buf.readShort()));
    }
    
    public static void handle(RequestEquipSpellMessage msg, NetworkEvent.Context context)
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
                menu.equipSpellRequest(msg.slot(), msg.nodeId());
            }
        });
        
        context.setPacketHandled(true);
    }
}
