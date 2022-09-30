package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class ItemRequirement extends Requirement
{
    public static final String CONSUMED_SUFFIX = ".consumed";
    public static final String MULTIPLE_SUFFIX = ".multiple";
    public static final String MULTIPLE_CONSUMED_SUFFIX = CONSUMED_SUFFIX + MULTIPLE_SUFFIX;
    
    protected ItemStack itemStack;
    protected boolean consume;
    
    public ItemRequirement(IRequirementType<?> type)
    {
        super(type);
    }
    
    public ItemRequirement(IRequirementType<?> type, ItemStack itemStack, boolean consume)
    {
        super(type);
        this.itemStack = itemStack;
        this.consume = consume;
    }
    
    @Override
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
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
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        Inventory inventory = spellProgressionHolder.getPlayer().getInventory();
        
        if(itemStack.getCount() == 1)
        {
            if(consume)
            {
                return Component.translatable(getDescriptionId() + CONSUMED_SUFFIX, itemStack.getHoverName());
            }
            else
            {
                return Component.translatable(getDescriptionId(), itemStack.getHoverName());
            }
        }
        else
        {
            if(consume)
            {
                return Component.translatable(getDescriptionId() + MULTIPLE_CONSUMED_SUFFIX, itemStack.getCount(), itemStack.getHoverName());
            }
            else
            {
                return Component.translatable(getDescriptionId() + MULTIPLE_SUFFIX, itemStack.getCount(), itemStack.getHoverName());
            }
        }
    }
    
    @Override
    public void writeToJson(JsonObject json)
    {
        SpellsFileUtil.jsonItemStack(json, itemStack, "item", "count");
        json.addProperty("consume", consume);
    }
    
    @Override
    public void readFromJson(JsonObject json)
    {
        itemStack = SpellsFileUtil.jsonItemStack(json, "item", "count");
        consume = SpellsFileUtil.jsonBoolean(json, "consume");
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
