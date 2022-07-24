package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.spell.ISpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SpellsSyncMessage(int entityId, ISpell[] spells)
{
    public static void encode(SpellsSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId());
        buf.writeByte(msg.spells().length);
        for(ISpell spell : msg.spells())
        {
            if(spell != null)
            {
                buf.writeBoolean(true);
                buf.writeRegistryId(Spells.SPELLS_REGISTRY.get(), spell);
            }
            else
            {
                buf.writeBoolean(false);
            }
        }
    }
    
    public static SpellsSyncMessage decode(FriendlyByteBuf buf)
    {
        int entityId = buf.readInt();
        
        ISpell[] spells = new ISpell[buf.readByte()];
        for(int i = 0; i < spells.length; ++i)
        {
            if(buf.readBoolean())
            {
                spells[i] = buf.readRegistryId();
            }
            else
            {
                spells[i] = null;
            }
        }
        
        return new SpellsSyncMessage(entityId, spells);
    }
    
    public static void handle(SpellsSyncMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleSpellsSync(msg));
        context.get().setPacketHandled(true);
    }
}
