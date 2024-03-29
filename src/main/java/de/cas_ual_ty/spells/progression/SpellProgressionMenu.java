package de.cas_ual_ty.spells.progression;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.network.SpellProgressionSyncMessage;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;

public class SpellProgressionMenu extends AbstractContainerMenu
{
    public static Component TITLE = Component.translatable("container." + SpellsAndShields.MOD_ID + ".spell_progression");
    
    public final ContainerLevelAccess access;
    public final Player player;
    
    public List<SpellTree> spellTrees;
    public HashMap<SpellNodeId, SpellStatus> spellProgression;
    
    public SpellProgressionMenu(int id, Inventory inventory, ContainerLevelAccess containerLevelAccess, List<SpellTree> spellTrees, HashMap<SpellNodeId, SpellStatus> spellProgression)
    {
        super(BuiltInRegisters.SPELL_PROGRESSION_MENU.get(), id);
        access = containerLevelAccess;
        player = inventory.player;
        
        this.spellTrees = spellTrees;
        this.spellProgression = spellProgression;
    }
    
    public void buySpellRequest(SpellNodeId nodeId)
    {
        if(player instanceof ServerPlayer player)
        {
            SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
            {
                access.execute((level, blockPos) ->
                {
                    if(ProgressionHelper.tryBuySpell(spellProgressionHolder, this, nodeId))
                    {
                        spellProgressionHolder.setSpellStatus(nodeId, SpellStatus.LEARNED);
                        level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, level.random.nextFloat() * 0.1F + 0.9F);
                    }
                    
                    spellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, access);
                    spellProgression = spellProgressionHolder.getProgression();
                    
                    SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SpellProgressionSyncMessage(blockPos, spellTrees, spellProgression, level));
                });
            });
        }
    }
    
    public void equipSpellRequest(int slot, SpellNodeId nodeId)
    {
        if(player instanceof ServerPlayer player)
        {
            SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
            {
                SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
                {
                    SpellNode node = nodeId.getSpellNode(SpellTrees.getRegistry(player.level()));
                    
                    if(node != null)
                    {
                        if(spellProgressionHolder.getSpellStatus(nodeId).isAvailable())
                        {
                            spellHolder.setSpell(slot, node.getSpellInstance());
                            spellHolder.sendSync();
                        }
                    }
                });
            });
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_)
    {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player)
    {
        return access.evaluate((level, blockPos) ->
        {
            return !SpellsUtil.isEnchantingTable(level.getBlockState(blockPos).getBlock()) ? false : player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64D;
        }, true);
    }
    
    public static SpellProgressionMenu construct(int id, Inventory inventory, FriendlyByteBuf extraData)
    {
        // client side construction
        try
        {
            SpellProgressionSyncMessage msg = SpellProgressionSyncMessage.decode(extraData);
            return new SpellProgressionMenu(id, inventory, ContainerLevelAccess.create(inventory.player.level(), msg.blockPos()), msg.spellTrees(), msg.map());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
