package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public class LayeredSpellIcon extends SpellIcon
{
    public static Codec<LayeredSpellIcon> makeCodec(SpellIconType<LayeredSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(ExtraCodecs.lazyInitializedCodec(() -> SpellsCodecs.SPELL_ICON)).fieldOf("icons_list").forGetter(LayeredSpellIcon::getList)
        ).apply(instance, (list) -> new LayeredSpellIcon(type, list)));
    }
    
    public static LayeredSpellIcon make(List<SpellIcon> list)
    {
        return new LayeredSpellIcon(SpellIconTypes.LAYERED.get(), list);
    }
    
    protected List<SpellIcon> list;
    
    public LayeredSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public LayeredSpellIcon(SpellIconType<?> type, List<SpellIcon> list)
    {
        this(type);
        this.list = list;
    }
    
    public List<SpellIcon> getList()
    {
        return list;
    }
}
