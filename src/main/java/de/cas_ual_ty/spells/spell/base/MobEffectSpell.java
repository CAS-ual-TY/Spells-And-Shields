package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.SpellHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MobEffectSpell extends PassiveSpell implements IEquippedTickSpell, IEquipSpell
{
    public static final String KEY_WHEN_APPLIED = "spell.mob_effect.attributes";
    
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
        ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        this.setIcon(new ResourceLocation(rl.getNamespace(), "textures/mob_effect/" + rl.getPath() + ".png"));
    }
    
    public MobEffectSpell(MobEffect mobEffect, int duration, int amplifier)
    {
        this(mobEffect, duration, amplifier, false, false, false);
    }
    
    public MobEffectSpell(MobEffect mobEffect, int duration)
    {
        this(mobEffect, duration, 0);
    }
    
    public MobEffectSpell(MobEffect mobEffect)
    {
        this(mobEffect, 20);
    }
    
    @Override
    public void onEquip(SpellHolder spellHolder, int slot)
    {
        tick(spellHolder);
    }
    
    @Override
    public void onUnequip(SpellHolder spellHolder, int slot)
    {
        MobEffectInstance activeEffect = spellHolder.getPlayer().getEffect(this.mobEffect);
        
        if(activeEffect == null)
        {
            return;
        }
        
        if(spellHolder.getPlayer().level.isClientSide)
        {
            activeEffect.setNoCounter(false);
        }
        else if(activeEffect.getDuration() <= duration + 1 &&
                activeEffect.getAmplifier() == amplifier &&
                activeEffect.isAmbient() == ambient &&
                activeEffect.getEffect() == mobEffect)
        {
            spellHolder.getPlayer().removeEffect(mobEffect);
        }
    }
    
    @Override
    public void tick(SpellHolder spellHolder)
    {
        MobEffectInstance activeEffect = spellHolder.getPlayer().getEffect(this.mobEffect);
        
        if(spellHolder.getPlayer().level.isClientSide)
        {
            if(activeEffect != null)
            {
                activeEffect.setNoCounter(true);
            }
        }
        else
        {
            MobEffectInstance newEffect = new MobEffectInstance(mobEffect, duration + 1, amplifier, ambient, visible, showIcon);
            
            if(activeEffect != null)
            {
                if(activeEffect.getDuration() == 1 &&
                        activeEffect.getAmplifier() == amplifier &&
                        activeEffect.isAmbient() == ambient &&
                        activeEffect.getEffect() == mobEffect)
                {
                    activeEffect.update(newEffect);
                }
            }
            else
            {
                spellHolder.getPlayer().addEffect(newEffect);
            }
        }
    }
    
    @Override
    public List<Component> getSpellDescription()
    {
        List<Component> list = new LinkedList<>();
        list.add(Component.translatable(getDescKey()));
        
        MutableComponent component = Component.translatable(mobEffect.getDescriptionId());
        
        if(amplifier > 0)
        {
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + amplifier));
        }
        
        list.add(component.withStyle(mobEffect.getCategory().getTooltipFormatting()));
        
        Map<Attribute, AttributeModifier> map = mobEffect.getAttributeModifiers();
        
        if(!map.isEmpty())
        {
            list.add(Component.empty());
            list.add((Component.translatable(KEY_WHEN_APPLIED)).withStyle(ChatFormatting.DARK_PURPLE));
            
            
            for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet())
            {
                Attribute attribute = entry.getKey();
                AttributeModifier attributeModifier = entry.getValue();
                
                // to calculate the actual attribute change dependent on amplifier
                attributeModifier = new AttributeModifier(attributeModifier.getName(), mobEffect.getAttributeModifierValue(amplifier, attributeModifier), attributeModifier.getOperation());
                
                AttributeSpell.addTooltip(list, attribute, attributeModifier);
            }
        }
        
        return list;
    }
}
