package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemSpellIcon extends SpellIcon
{
    public static Codec<ItemSpellIcon> makeCodec(SpellIconType<ItemSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(ItemSpellIcon::getItem)
        ).apply(instance, (item) -> new ItemSpellIcon(type, item)));
    }
    
    public static ItemSpellIcon make(ItemStack item)
    {
        return new ItemSpellIcon(SpellIconTypes.ITEM.get(), item);
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
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        item = buf.readItem();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeItem(item);
    }
}
