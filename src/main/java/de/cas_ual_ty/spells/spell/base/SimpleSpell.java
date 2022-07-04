package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;

import java.util.function.Consumer;

public class SimpleSpell extends Spell
{
    public final Consumer<ManaHolder> effect;
    
    public SimpleSpell(float manaCost, Consumer<ManaHolder> effect)
    {
        super(manaCost);
        this.effect = effect;
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        this.effect.accept(manaHolder);
    }
}
