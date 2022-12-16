package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SpellsSyncMessage(int entityId, ResourceLocation[] spells)
{
    public static void encode(SpellsSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId());
        buf.writeByte(msg.spells().length);
        for(ResourceLocation spell : msg.spells())
        {
            if(spell != null)
            {
                buf.writeBoolean(true);
                buf.writeResourceLocation(spell);
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
        
        ResourceLocation[] spells = new ResourceLocation[buf.readByte()];
        for(int i = 0; i < spells.length; ++i)
        {
            if(buf.readBoolean())
            {
                spells[i] = buf.readResourceLocation();
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
