package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spell.ISpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestLearnSpellMessage(int id, ISpell spell, ResourceLocation treeId)
{
    public static void encode(RequestLearnSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeShort(msg.id());
        buf.writeRegistryId(Spells.SPELLS_REGISTRY.get(), msg.spell());
        buf.writeResourceLocation(msg.treeId());
    }
    
    public static RequestLearnSpellMessage decode(FriendlyByteBuf buf)
    {
        return new RequestLearnSpellMessage(buf.readShort(), buf.readRegistryId(), buf.readResourceLocation());
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
                menu.buySpellRequest(msg.id(), msg.spell(), msg.treeId());
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
