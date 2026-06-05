package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SpellProgressionSyncMessage(BlockPos blockPos, List<SpellTree> spellTrees, HashMap<SpellNodeId, SpellStatus> map) implements CustomPacketPayload
{
    public static final Type<SpellProgressionSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "spell_progression_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellProgressionSyncMessage> STREAM_CODEC = StreamCodec.of(
            SpellProgressionSyncMessage::encode,
            SpellProgressionSyncMessage::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, SpellProgressionSyncMessage msg)
    {
        buf.writeBlockPos(msg.blockPos());

        Registry<Spell> registry = Spells.getRegistry(buf.registryAccess());

        buf.writeInt(msg.spellTrees().size());
        for(SpellTree spellTree : msg.spellTrees())
        {
            SpellTreeSerializer.encodeTree(spellTree, registry, buf);
        }

        buf.writeInt(msg.map().size());
        for(Map.Entry<SpellNodeId, SpellStatus> entry : msg.map().entrySet())
        {
            entry.getKey().toBuf(buf);
            buf.writeByte(entry.getValue().ordinal());
        }
    }

    private static SpellProgressionSyncMessage decode(RegistryFriendlyByteBuf buf)
    {
        BlockPos blockPos = buf.readBlockPos();

        Registry<Spell> registry = Spells.getRegistry(buf.registryAccess());

        int size = buf.readInt();
        List<SpellTree> spellTrees = new ArrayList<>(size);
        for(int i = 0; i < size; ++i)
        {
            spellTrees.add(SpellTreeSerializer.decodeTree(registry, buf));
        }

        size = buf.readInt();
        HashMap<SpellNodeId, SpellStatus> map = new HashMap<>();
        for(int i = 0; i < size; ++i)
        {
            SpellNodeId nodeId = SpellNodeId.fromBuf(buf);
            SpellStatus spellStatus = SpellStatus.values()[buf.readByte()];
            map.put(nodeId, spellStatus);
        }

        return new SpellProgressionSyncMessage(blockPos, spellTrees, map);
    }

    public static void handle(SpellProgressionSyncMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() -> ClientMessageHandler.handleSpellProgressionSync(msg));
    }
}
