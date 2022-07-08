package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.SpellHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MobEffectSpell extends PassiveSpell implements IEquippedTickSpell
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
        this.setIcon(new ResourceLocation(mobEffect.getRegistryName().getNamespace(), "textures/mob_effect/" + mobEffect.getRegistryName().getPath() + ".png"));
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
    
    @Override
    public List<Component> getSpellDescription()
    {
        List<Component> list = new LinkedList<>();
        list.add(new TranslatableComponent(getDescKey()));
        
        MutableComponent component = new TranslatableComponent(mobEffect.getDescriptionId());
        
        if(amplifier > 0)
        {
            component = new TranslatableComponent("potion.withAmplifier", component, new TranslatableComponent("potion.potency." + amplifier));
        }
        
        list.add(component.withStyle(mobEffect.getCategory().getTooltipFormatting()));
        
        Map<Attribute, AttributeModifier> map = mobEffect.getAttributeModifiers();
        
        if(!map.isEmpty())
        {
            list.add(TextComponent.EMPTY);
            list.add((new TranslatableComponent(KEY_WHEN_APPLIED)).withStyle(ChatFormatting.DARK_PURPLE));
            
            
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
