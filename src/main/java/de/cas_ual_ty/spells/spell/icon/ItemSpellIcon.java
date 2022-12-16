package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public class ItemSpellIcon extends SpellIcon
{
    public static Codec<ItemSpellIcon> makeCodec(SpellIconType<ItemSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(ItemSpellIcon::getItem)
        ).apply(instance, (item) -> new ItemSpellIcon(type, item)));
    }
    
    protected ItemStack item;
    
    public ItemSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public ItemSpellIcon(SpellIconType<?> type, ItemStack item)
    {
        this(type);
        this.item = item;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
}
