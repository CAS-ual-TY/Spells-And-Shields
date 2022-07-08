package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.SpellHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public class MobEffectSpell extends PassiveSpell implements IEquippedTickSpell
{
    public final MobEffect mobEffect;
    public final int duration;
    public final int amplifier;
    public final boolean ambient;
    public final boolean visible;
    public final boolean showIcon;
    
    public MobEffectSpell(MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
    {
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
    }
    
    public MobEffectSpell(MobEffect mobEffect, int duration, int amplifier)
    {
        this(mobEffect, duration, amplifier, false, false, false);
    }
    
    public MobEffectSpell(MobEffect mobEffect, int amplifier)
    {
        this(mobEffect, 20, amplifier);
    }
    
    public MobEffectSpell(MobEffect mobEffect)
    {
        this(mobEffect, 0);
    }
    
    @Override
    public void tick(SpellHolder spellHolder)
    {
        MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon);
        mobEffectInstance.setNoCounter(true);
        mobEffectInstance.setCurativeItems(List.of());
        spellHolder.getPlayer().addEffect(mobEffectInstance);
    }
}
