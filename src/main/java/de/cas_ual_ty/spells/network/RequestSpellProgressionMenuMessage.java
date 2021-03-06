package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import net.minecraft.core.BlockPos;
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
            
            if(player.containerMenu != null && player.level.getBlockState(msg.pos()).getBlock() == SpellsRegistries.VANILLA_ENCHANTING_TABLE.get())
            {
                ContainerLevelAccess access = ContainerLevelAccess.create(player.level, msg.pos());
                
                SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
                {
                    access.execute((level, blockPos) ->
                    {
                        List<SpellTree> availableSpellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, blockPos);
                        HashMap<ISpell, SpellStatus> progression = spellProgressionHolder.getProgression();
                        
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
                            SpellProgressionSyncMessage data = new SpellProgressionSyncMessage(blockPos, availableSpellTrees, progression);
                            SpellProgressionSyncMessage.encode(data, buf);
                        });
                    });
                });
            }
        });
        
        context.get().setPacketHandled(true);
    }
}
