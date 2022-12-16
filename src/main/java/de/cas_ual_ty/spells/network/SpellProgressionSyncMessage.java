package de.cas_ual_ty.spells.network;

import com.mojang.datafixers.util.Pair;
import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellTreeSerializer;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public record SpellProgressionSyncMessage(BlockPos blockPos, List<SpellTree> spellTrees,
                                          Collection<Pair<ResourceLocation, SpellStatus>> map, Level level)
{
    public static void encode(SpellProgressionSyncMessage msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(msg.blockPos());
        
        buf.writeInt(msg.spellTrees().size());
        for(SpellTree spellTree : msg.spellTrees())
        {
            SpellTreeSerializer.encodeTree(spellTree, SpellsUtil.getSpellRegistry(msg.level()), buf);
        }
        
        buf.writeInt(msg.map().size());
        for(Pair<ResourceLocation, SpellStatus> entry : msg.map())
        {
            buf.writeResourceLocation(entry.getFirst());
            buf.writeByte(entry.getSecond().ordinal());
        }
    }
    
    public static SpellProgressionSyncMessage decode(FriendlyByteBuf buf)
    {
        Level level = SpellsUtil.getClientLevel();
        
        BlockPos blockPos = buf.readBlockPos();
        
        int size = buf.readInt();
        List<SpellTree> spellTrees = new ArrayList<>(size);
        
        for(int i = 0; i < size; ++i)
        {
            spellTrees.add(SpellTreeSerializer.decodeTree(SpellsUtil.getSpellRegistry(level), buf));
        }
        
        size = buf.readInt();
        List<Pair<ResourceLocation, SpellStatus>> map = new LinkedList<>();
        
        for(int i = 0; i < size; ++i)
        {
            ResourceLocation spell = buf.readResourceLocation();
            SpellStatus spellStatus = SpellStatus.values()[buf.readByte()];
            map.add(Pair.of(spell, spellStatus));
        }
        
        return new SpellProgressionSyncMessage(blockPos, spellTrees, map, level);
    }
    
    public static void handle(SpellProgressionSyncMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleSpellProgressionSync(msg));
        context.get().setPacketHandled(true);
    }
}
