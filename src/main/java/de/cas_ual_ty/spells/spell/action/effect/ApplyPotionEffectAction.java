package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class ApplyPotionEffectAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ApplyPotionEffectAction> makeCodec(SpellActionType<ApplyPotionEffectAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(ApplyPotionEffectAction::getMobEffect),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(ApplyPotionEffectAction::getDuration),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amplifier")).forGetter(ApplyPotionEffectAction::getAmplifier),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("ambient")).forGetter(ApplyPotionEffectAction::getAmbient),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("visible")).forGetter(ApplyPotionEffectAction::getVisible),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("showIcon")).forGetter(ApplyPotionEffectAction::getShowIcon)
        ).apply(instance, (activation, targets, mobEffect, duration, amplifier, ambient, visible, showIcon) -> new ApplyPotionEffectAction(type, activation, targets, mobEffect, duration, amplifier, ambient, visible, showIcon)));
    }
    
    public static ApplyPotionEffectAction make(String activation, String targets, MobEffect mobEffect, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> amplifier, DynamicCtxVar<Boolean> ambient, DynamicCtxVar<Boolean> visible, DynamicCtxVar<Boolean> showIcon)
    {
        return new ApplyPotionEffectAction(SpellActionTypes.APPLY_POTION_EFFECT.get(), activation, targets, mobEffect, duration, amplifier, ambient, visible, showIcon);
    }
    
    protected MobEffect mobEffect;
    protected DynamicCtxVar<Integer> duration;
    protected DynamicCtxVar<Integer> amplifier;
    protected DynamicCtxVar<Boolean> ambient;
    protected DynamicCtxVar<Boolean> visible;
    protected DynamicCtxVar<Boolean> showIcon;
    
    public ApplyPotionEffectAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyPotionEffectAction(SpellActionType<?> type, String activation, String targets, MobEffect mobEffect, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> amplifier, DynamicCtxVar<Boolean> ambient, DynamicCtxVar<Boolean> visible, DynamicCtxVar<Boolean> showIcon)
    {
        super(type, activation, targets);
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
    }
    
    public MobEffect getMobEffect()
    {
        return mobEffect;
    }
    
    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }
    
    public DynamicCtxVar<Integer> getAmplifier()
    {
        return amplifier;
    }
    
    public DynamicCtxVar<Boolean> getAmbient()
    {
        return ambient;
    }
    
    public DynamicCtxVar<Boolean> getVisible()
    {
        return visible;
    }
    
    public DynamicCtxVar<Boolean> getShowIcon()
    {
        return showIcon;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        duration.getValue(ctx).ifPresent(duration ->
        {
            amplifier.getValue(ctx).ifPresent(amplifier ->
            {
                ambient.getValue(ctx).ifPresent(ambient ->
                {
                    visible.getValue(ctx).ifPresent(visible ->
                    {
                        showIcon.getValue(ctx).ifPresent(showIcon ->
                        {
                            target.getLivingEntity().addEffect(new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon));
                        });
                    });
                });
            });
        });
    }
}
