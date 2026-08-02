package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.progression.FullSpellNodeId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpellsSyncMessage(int entityId, ResourceLocation[] spells, FullSpellNodeId[] nodeIds, int[] cooldowns) implements CustomPacketPayload
{
    public static final Type<SpellsSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "spells_sync"));
    public static final StreamCodec<FriendlyByteBuf, SpellsSyncMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) ->
            {
                if(msg.spells().length != msg.nodeIds().length || msg.spells().length != msg.cooldowns().length)
                {
                    throw new IllegalStateException("SpellsSyncMessage: spells/nodeIds/cooldowns length mismatch (" + msg.spells().length + " vs " + msg.nodeIds().length + " vs " + msg.cooldowns().length + ")");
                }

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
                for(FullSpellNodeId id : msg.nodeIds())
                {
                    if(id != null)
                    {
                        buf.writeBoolean(true);
                        buf.writeResourceLocation(id.treeId());
                        buf.writeResourceLocation(id.nodeId());
                    }
                    else
                    {
                        buf.writeBoolean(false);
                    }
                }
                for(int cooldown : msg.cooldowns())
                {
                    buf.writeVarInt(cooldown);
                }
            },
            buf ->
            {
                int entityId = buf.readInt();
                ResourceLocation[] spells = new ResourceLocation[buf.readByte()];
                for(int i = 0; i < spells.length; ++i)
                {
                    spells[i] = buf.readBoolean() ? buf.readResourceLocation() : null;
                }
                FullSpellNodeId[] ids = new FullSpellNodeId[spells.length];
                for(int i = 0; i < ids.length; ++i)
                {
                    ids[i] = buf.readBoolean() ? new FullSpellNodeId(buf.readResourceLocation(), buf.readResourceLocation()) : null;
                }
                int[] cooldowns = new int[spells.length];
                for(int i = 0; i < cooldowns.length; ++i)
                {
                    cooldowns[i] = buf.readVarInt();
                }
                return new SpellsSyncMessage(entityId, spells, ids, cooldowns);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(SpellsSyncMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleSpellsSync(msg)).exceptionally(e ->
        {
            e.printStackTrace();
            return null;
        });
    }
}
