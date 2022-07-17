package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.base.ISpell;
import de.cas_ual_ty.spells.spell.tree.SpellTree;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record SpellProgressionSyncMessage(BlockPos blockPos, List<SpellTree> spellTrees,
                                          HashMap<ISpell, SpellStatus> map)
{
    public static void encode(SpellProgressionSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(msg.blockPos());
        
        buf.writeInt(msg.spellTrees().size());
        
        for(SpellTree spellTree : msg.spellTrees())
        {
            SpellTreeSerializer.encodeTree(spellTree, buf);
        }
        
        buf.writeInt(msg.map().size());
        
        for(Map.Entry<ISpell, SpellStatus> entry : msg.map().entrySet())
        {
            buf.writeRegistryId(SpellsRegistries.SPELLS_REGISTRY.get(), entry.getKey());
            buf.writeByte(entry.getValue().ordinal());
        }
    }
    
    public static SpellProgressionSyncMessage decode(FriendlyByteBuf buf)
    {
        BlockPos blockPos = buf.readBlockPos();
        
        int size = buf.readInt();
        List<SpellTree> spellTrees = new ArrayList<>(size);
        
        for(int i = 0; i < size; ++i)
        {
            spellTrees.add(SpellTreeSerializer.decodeTree(buf));
        }
        
        size = buf.readInt();
        HashMap<ISpell, SpellStatus> map = new HashMap<>(size);
        
        for(int i = 0; i < size; ++i)
        {
            ISpell spell = buf.readRegistryId();
            SpellStatus spellStatus = SpellStatus.values()[buf.readByte()];
            map.put(spell, spellStatus);
        }
        
        return new SpellProgressionSyncMessage(blockPos, spellTrees, map);
    }
    
    public static void handle(SpellProgressionSyncMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleSpellProgressionSync(msg));
        context.get().setPacketHandled(true);
    }
}
