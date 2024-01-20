package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemRequirement extends Requirement
{
    public static Codec<ItemRequirement> makeCodec(RequirementType<ItemRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(ItemRequirement::getItemStack),
                Codec.BOOL.fieldOf("consume").forGetter(ItemRequirement::getConsume)
        ).apply(instance, (itemStack, consume) -> new ItemRequirement(type, itemStack, consume)));
    }
    
    public static final String CONSUMED_SUFFIX = ".consumed";
    public static final String MULTIPLE_SUFFIX = ".multiple";
    public static final String MULTIPLE_CONSUMED_SUFFIX = CONSUMED_SUFFIX + MULTIPLE_SUFFIX;
    
    protected ItemStack itemStack;
    protected boolean consume;
    
    public ItemRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public ItemRequirement(RequirementType<?> type, ItemStack itemStack, boolean consume)
    {
        this(type);
        this.itemStack = itemStack;
        this.consume = consume;
    }
    
    public ItemStack getItemStack()
    {
        return itemStack;
    }
    
    public boolean getConsume()
    {
        return consume;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        Inventory inventory = spellProgressionHolder.getPlayer().getInventory();
        return inventory.clearOrCountMatchingItems(item -> item.getItem() == itemStack.getItem(), 0, SpellsUtil.EMPTY_CONTAINER) >= itemStack.getCount();
    }
    
    @Override
    public void onSpellLearned(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        if(consume && !spellProgressionHolder.getPlayer().isCreative())
        {
            Inventory inventory = spellProgressionHolder.getPlayer().getInventory();
            inventory.clearOrCountMatchingItems(item -> item.getItem() == itemStack.getItem(), itemStack.getCount(), SpellsUtil.EMPTY_CONTAINER);
        }
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        Inventory inventory = spellProgressionHolder.getPlayer().getInventory();
        
        if(itemStack.getCount() == 1)
        {
            if(consume)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + CONSUMED_SUFFIX, itemStack.getHoverName())));
            }
            else
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId(), itemStack.getHoverName())));
            }
        }
        else
        {
            if(consume)
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + MULTIPLE_CONSUMED_SUFFIX, itemStack.getCount(), itemStack.getHoverName())));
            }
            else
            {
                tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(getDescriptionId() + MULTIPLE_SUFFIX, itemStack.getCount(), itemStack.getHoverName())));
            }
        }
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeItem(itemStack);
        buf.writeBoolean(consume);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        itemStack = buf.readItem();
        consume = buf.readBoolean();
    }
}