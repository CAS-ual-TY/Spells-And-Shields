package de.cas_ual_ty.spells.network;

import com.mojang.datafixers.util.Pair;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public record RequestSpellProgressionMenuMessage(BlockPos pos)
{
    public static void encode(RequestSpellProgressionMenuMessage msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(msg.pos());
    }
    
    public static RequestSpellProgressionMenuMessage decode(FriendlyByteBuf buf)
    {
        return new RequestSpellProgressionMenuMessage(buf.readBlockPos());
    }
    
    public static void handle(RequestSpellProgressionMenuMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer player = context.get().getSender();
            
            if(player == null)
            {
                return;
            }
            
            if(player.containerMenu != null && SpellsUtil.isEnchantingTable(player.level.getBlockState(msg.pos()).getBlock()))
            {
                ContainerLevelAccess access = ContainerLevelAccess.create(player.level, msg.pos());
                
                SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
                {
                    access.execute((level, blockPos) ->
                    {
                        List<SpellTree> availableSpellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, access);
                        HashMap<Spell, SpellStatus> progression = spellProgressionHolder.getProgression();
                        
                        Registry<Spell> registry = SpellsUtil.getSpellRegistry(level);
                        
                        NetworkHooks.openScreen(player, new MenuProvider()
                        {
                            @Override
                            public Component getDisplayName()
                            {
                                return SpellProgressionMenu.TITLE;
                            }
                            
                            @Override
                            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player)
                            {
                                return new SpellProgressionMenu(id, inventory, access, availableSpellTrees, progression);
                            }
                        }, buf ->
                        {
                            SpellProgressionSyncMessage data = new SpellProgressionSyncMessage(blockPos, availableSpellTrees, progression.entrySet().stream().map(e -> Pair.of(registry.getKey(e.getKey()), e.getValue())).collect(Collectors.toList()), level);
                            SpellProgressionSyncMessage.encode(data, buf);
                        });
                    });
                });
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
