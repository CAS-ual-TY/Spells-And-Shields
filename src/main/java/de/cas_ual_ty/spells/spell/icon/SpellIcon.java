package de.cas_ual_ty.spells.spell.icon;

public abstract class SpellIcon
{
    public final SpellIconType<?> type;
    
    public SpellIcon(SpellIconType<?> type)
    {
        this.type = type;
    }
    
    public SpellIconType<?> getType()
    {
        return type;
    }
}
