package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spell.ISpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record RequestEquipSpellMessage(ISpell spell, byte slot, UUID treeId)
{
    public static void encode(RequestEquipSpellMessage msg, FriendlyByteBuf buf)
    {
        if(msg.spell() != null)
        {
            buf.writeBoolean(true);
            buf.writeRegistryId(Spells.SPELLS_REGISTRY.get(), msg.spell());
        }
        else
        {
            buf.writeBoolean(false);
        }
        
        buf.writeByte(msg.slot());
        buf.writeUUID(msg.treeId());
    }
    
    public static RequestEquipSpellMessage decode(FriendlyByteBuf buf)
    {
        return new RequestEquipSpellMessage(buf.readBoolean() ? buf.readRegistryId() : null, buf.readByte(), buf.readUUID());
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
