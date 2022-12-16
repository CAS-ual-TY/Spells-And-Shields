package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestEquipSpellMessage(ResourceLocation spell, byte slot, ResourceLocation treeId)
{
    public static void encode(RequestEquipSpellMessage msg, FriendlyByteBuf buf)
    {
        if(msg.spell() != null)
        {
            buf.writeBoolean(true);
            buf.writeResourceLocation(msg.spell());
        }
        else
        {
            buf.writeBoolean(false);
        }
        
        buf.writeByte(msg.slot());
        buf.writeResourceLocation(msg.treeId());
    }
    
    public static RequestEquipSpellMessage decode(FriendlyByteBuf buf)
    {
        return new RequestEquipSpellMessage(buf.readBoolean() ? buf.readResourceLocation() : null, buf.readByte(), buf.readResourceLocation());
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
                menu.equipSpellRequest(msg.spell(), msg.slot(), msg.treeId());
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
