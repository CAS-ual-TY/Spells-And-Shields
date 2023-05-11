package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record SpellProgressionSyncMessage(BlockPos blockPos, List<SpellTree> spellTrees,
                                          HashMap<SpellNodeId, SpellStatus> map, Level level)
{
    public static void encode(SpellProgressionSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(msg.blockPos());
        
        buf.writeInt(msg.spellTrees().size());
        for(SpellTree spellTree : msg.spellTrees())
        {
            SpellTreeSerializer.encodeTree(spellTree, Spells.getRegistry(msg.level()), buf);
        }
        
        buf.writeInt(msg.map().size());
        for(Map.Entry<SpellNodeId, SpellStatus> entry : msg.map().entrySet())
        {
            entry.getKey().toBuf(buf);
            buf.writeByte(entry.getValue().ordinal());
        }
    }
    
    public static SpellProgressionSyncMessage decode(FriendlyByteBuf buf)
    {
        Level level = SpellsUtil.getClientLevel();
        
        BlockPos blockPos = buf.readBlockPos();
        
        int size = buf.readInt();
        List<SpellTree> spellTrees = new ArrayList<>(size);
        
        Registry<Spell> registry = Spells.getRegistry(SpellsUtil.getClientLevel());
        
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
        
        return new SpellProgressionSyncMessage(blockPos, spellTrees, map, level);
    }
    
    public static void handle(SpellProgressionSyncMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleSpellProgressionSync(msg));
        context.get().setPacketHandled(true);
    }
}
