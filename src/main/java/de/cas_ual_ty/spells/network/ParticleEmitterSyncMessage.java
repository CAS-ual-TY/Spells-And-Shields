package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.capability.ParticleEmitterHolder;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public record ParticleEmitterSyncMessage(int entityId, boolean clear, List<ParticleEmitterHolder.ParticleEmitter> list)
{
    public static void encode(ParticleEmitterSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId());
        buf.writeBoolean(msg.clear());
        buf.writeInt(msg.list().size());
        for(ParticleEmitterHolder.ParticleEmitter e : msg.list())
        {
            e.toByteBuf(buf);
        }
    }
    
    public static ParticleEmitterSyncMessage decode(FriendlyByteBuf buf)
    {
        int entityId = buf.readInt();
        boolean clear = buf.readBoolean();
        List<ParticleEmitterHolder.ParticleEmitter> list = new LinkedList<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++)
        {
            list.add(ParticleEmitterHolder.ParticleEmitter.fromByteBuf(buf));
        }
        
        return new ParticleEmitterSyncMessage(entityId, clear, list);
    }
    
    public static void handle(ParticleEmitterSyncMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleParticleEmitterSync(msg));
        context.get().setPacketHandled(true);
    }
}
