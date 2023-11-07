package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkEvent;

public record SpellsSyncMessage(int entityId, ResourceLocation[] spells, SpellNodeId[] nodeIds)
{
    public static void encode(SpellsSyncMessage msg, FriendlyByteBuf buf)
    {
        assert msg.spells().length == msg.nodeIds().length;
        
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
        
        for(SpellNodeId id : msg.nodeIds())
        {
            if(id != null)
            {
                buf.writeBoolean(true);
                buf.writeResourceLocation(id.treeId());
                buf.writeInt(id.nodeId());
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
        
        SpellNodeId[] ids = new SpellNodeId[spells.length];
        for(int i = 0; i < ids.length; ++i)
        {
            if(buf.readBoolean())
            {
                ids[i] = new SpellNodeId(buf.readResourceLocation(), buf.readInt());
            }
            else
            {
                ids[i] = null;
            }
        }
        
        return new SpellsSyncMessage(entityId, spells, ids);
    }
    
    public static void handle(SpellsSyncMessage msg, NetworkEvent.Context context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleSpellsSync(msg));
        context.setPacketHandled(true);
    }
}
