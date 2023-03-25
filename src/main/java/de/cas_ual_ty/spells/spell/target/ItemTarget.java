package de.cas_ual_ty.spells.spell.target;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ItemTarget extends Target
{
    protected Level level;
    protected ItemStack item;
    protected Consumer<ItemStack> setter;
    protected boolean isCreative;
    
    public ItemTarget(ITargetType<?> type, Level level, ItemStack item, Consumer<ItemStack> setter, boolean isCreative)
    {
        super(type);
        this.item = item;
        this.level = level;
        this.setter = setter;
        this.isCreative = isCreative;
    }
    
    @Override
    public Level getLevel()
    {
        return level;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public boolean isCreative()
    {
        return isCreative;
    }
    
    public void modify(ItemStack itemStack)
    {
        setter.accept(itemStack);
    }
}
