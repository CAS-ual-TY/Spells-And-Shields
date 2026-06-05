package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ParticleEmitterHolder;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedList;
import java.util.List;

public record ParticleEmitterSyncMessage(int entityId, boolean clear, List<ParticleEmitterHolder.ParticleEmitter> list) implements CustomPacketPayload
{
    public static final Type<ParticleEmitterSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "particle_emitter_sync"));
    public static final StreamCodec<FriendlyByteBuf, ParticleEmitterSyncMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) ->
            {
                buf.writeInt(msg.entityId());
                buf.writeBoolean(msg.clear());
                buf.writeInt(msg.list().size());
                for(ParticleEmitterHolder.ParticleEmitter e : msg.list())
                {
                    e.toByteBuf(buf);
                }
            },
            buf ->
            {
                int entityId = buf.readInt();
                boolean clear = buf.readBoolean();
                int size = buf.readInt();
                List<ParticleEmitterHolder.ParticleEmitter> list = new LinkedList<>();
                for(int i = 0; i < size; i++)
                {
                    list.add(ParticleEmitterHolder.ParticleEmitter.fromByteBuf(buf));
                }
                return new ParticleEmitterSyncMessage(entityId, clear, list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(ParticleEmitterSyncMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleParticleEmitterSync(msg));
    }
}
