package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class TemporaryMobEffectSpell extends BaseIngredientsSpell
{
    public final MobEffect mobEffect;
    public final int duration;
    public final int amplifier;
    public final boolean ambient;
    public final boolean visible;
    public final boolean showIcon;
    
    public TemporaryMobEffectSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
        
        ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        this.setIcon(new ResourceLocation(rl.getNamespace(), "textures/mob_effect/" + rl.getPath() + ".png"));
    }
    
    public TemporaryMobEffectSpell(float manaCost, ItemStack handIngredient, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
    {
        super(manaCost, handIngredient);
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
        
        ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        this.setIcon(new ResourceLocation(rl.getNamespace(), "textures/mob_effect/" + rl.getPath() + ".png"));
    }
    
    public TemporaryMobEffectSpell(float manaCost, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
    {
        super(manaCost);
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
        
        ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
        this.setIcon(new ResourceLocation(rl.getNamespace(), "textures/mob_effect/" + rl.getPath() + ".png"));
    }
    
    public TemporaryMobEffectSpell(float manaCost, ItemStack handIngredient, MobEffect mobEffect, int duration)
    {
        this(manaCost, handIngredient, mobEffect, duration, 0, false, true, true);
    }
    
    public TemporaryMobEffectSpell(float manaCost, ItemStack handIngredient, MobEffect mobEffect)
    {
        this(manaCost, handIngredient, mobEffect, 20 * 20);
    }
    
    public TemporaryMobEffectSpell(ItemStack handIngredient, MobEffect mobEffect)
    {
        this(5F, handIngredient, mobEffect, 20 * 20);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        manaHolder.getPlayer().addEffect(new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon));
        manaHolder.getPlayer().level.playSound(null, manaHolder.getPlayer(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1F, 1F);
    }
    
    @Override
    public MutableComponent getSpellName()
    {
        MutableComponent component = new TranslatableComponent(mobEffect.getDescriptionId());
        
        if(amplifier > 0)
        {
            component = new TranslatableComponent("potion.withAmplifier", component, new TranslatableComponent("potion.potency." + amplifier));
        }
        
        return new TranslatableComponent(getNameKey(), component);
    }
    
    @Override
    public MutableComponent getSpellDesc()
    {
        MutableComponent component = new TranslatableComponent(mobEffect.getDescriptionId());
        
        if(amplifier > 0)
        {
            component = new TranslatableComponent("potion.withAmplifier", component, new TranslatableComponent("potion.potency." + amplifier));
        }
        
        return new TranslatableComponent(getDescKey(), component.withStyle(ChatFormatting.YELLOW));
    }
    
    @Override
    public void addSpellDesc(List<Component> list)
    {
        super.addSpellDesc(list);
        
        Map<Attribute, AttributeModifier> map = mobEffect.getAttributeModifiers();
        
        if(!map.isEmpty())
        {
            list.add(TextComponent.EMPTY);
            list.add(PassiveSpell.whenAppliedComponent());
            
            for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet())
            {
                Attribute attribute = entry.getKey();
                AttributeModifier attributeModifier = entry.getValue();
                
                // to calculate the actual attribute change dependent on amplifier
                attributeModifier = new AttributeModifier(attributeModifier.getName(), mobEffect.getAttributeModifierValue(amplifier, attributeModifier), attributeModifier.getOperation());
                
                AttributeSpell.addTooltip(list, attribute, attributeModifier);
            }
        }
    }
}