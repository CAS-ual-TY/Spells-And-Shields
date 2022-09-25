package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TemporaryMobEffect extends BaseIngredientsSpell
{
    public final MobEffect mobEffect;
    public final int duration;
    public final int amplifier;
    public final boolean ambient;
    public final boolean visible;
    public final boolean showIcon;
    
    public TemporaryMobEffect(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
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
    
    public TemporaryMobEffect(float manaCost, ItemStack handIngredient, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
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
    
    public TemporaryMobEffect(float manaCost, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)
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
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        manaHolder.getPlayer().addEffect(new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon));
        manaHolder.getPlayer().level.playSound(null, manaHolder.getPlayer(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1F, 1F);
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
}