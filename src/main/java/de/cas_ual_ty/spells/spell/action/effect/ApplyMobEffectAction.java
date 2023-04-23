package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class ApplyMobEffectAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ApplyMobEffectAction> makeCodec(SpellActionType<ApplyMobEffectAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("mob_effect")).forGetter(ApplyMobEffectAction::getMobEffect),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(ApplyMobEffectAction::getDuration),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amplifier")).forGetter(ApplyMobEffectAction::getAmplifier),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("ambient")).forGetter(ApplyMobEffectAction::getAmbient),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("visible")).forGetter(ApplyMobEffectAction::getVisible),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("showIcon")).forGetter(ApplyMobEffectAction::getShowIcon)
        ).apply(instance, (activation, multiTargets, mobEffect, duration, amplifier, ambient, visible, showIcon) -> new ApplyMobEffectAction(type, activation, multiTargets, mobEffect, duration, amplifier, ambient, visible, showIcon)));
    }
    
    public static ApplyMobEffectAction make(Object activation, Object multiTargets, DynamicCtxVar<String> mobEffect, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> amplifier, DynamicCtxVar<Boolean> ambient, DynamicCtxVar<Boolean> visible, DynamicCtxVar<Boolean> showIcon)
    {
        return new ApplyMobEffectAction(SpellActionTypes.APPLY_MOB_EFFECT.get(), activation.toString(), multiTargets.toString(), mobEffect, duration, amplifier, ambient, visible, showIcon);
    }
    
    protected DynamicCtxVar<String> mobEffect;
    protected DynamicCtxVar<Integer> duration;
    protected DynamicCtxVar<Integer> amplifier;
    protected DynamicCtxVar<Boolean> ambient;
    protected DynamicCtxVar<Boolean> visible;
    protected DynamicCtxVar<Boolean> showIcon;
    
    public ApplyMobEffectAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ApplyMobEffectAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> mobEffect, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> amplifier, DynamicCtxVar<Boolean> ambient, DynamicCtxVar<Boolean> visible, DynamicCtxVar<Boolean> showIcon)
    {
        super(type, activation, multiTargets);
        this.mobEffect = mobEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.visible = visible;
        this.showIcon = showIcon;
    }
    
    public DynamicCtxVar<String> getMobEffect()
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
        SpellsUtil.stringToObject(ctx, mobEffect, ForgeRegistries.MOB_EFFECTS).ifPresent(mobEffect ->
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
        });
    }
}
