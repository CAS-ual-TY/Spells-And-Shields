package de.cas_ual_ty.spells.spell.target;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemTarget extends Target
{
    protected Level level;
    protected ItemStack item;
    
    public ItemTarget(ITargetType<?> type, Level level, ItemStack item)
    {
        super(type);
        this.item = item;
        this.level = level;
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
}
