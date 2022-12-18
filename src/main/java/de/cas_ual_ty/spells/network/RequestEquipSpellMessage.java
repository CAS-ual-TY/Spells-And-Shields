package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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
    
    public static void handle(RequestEquipSpellMessage msg, Supplier<NetworkEvent.Context> context)
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
                menu.equipSpellRequest(msg.slot(), msg.nodeId());
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
