package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.SpellsUtil;
import net.minecraft.core.registries.BuiltInRegistries;

public class RemoveMobEffectAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<RemoveMobEffectAction> makeCodec(SpellActionType<RemoveMobEffectAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("mob_effect")).forGetter(RemoveMobEffectAction::getMobEffect)
        ).apply(instance, (activation, multiTargets, mobEffect) -> new RemoveMobEffectAction(type, activation, multiTargets, mobEffect)));
    }

    public static RemoveMobEffectAction make(Object activation, Object multiTargets, DynamicCtxVar<String> mobEffect)
    {
        return new RemoveMobEffectAction(SpellActionTypes.REMOVE_MOB_EFFECT.get(), activation.toString(), multiTargets.toString(), mobEffect);
    }

    protected DynamicCtxVar<String> mobEffect;

    public RemoveMobEffectAction(SpellActionType<?> type)
    {
        super(type);
    }

    public RemoveMobEffectAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> mobEffect)
    {
        super(type, activation, multiTargets);
        this.mobEffect = mobEffect;
    }

    public DynamicCtxVar<String> getMobEffect()
    {
        return mobEffect;
    }

    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        SpellsUtil.stringToHolder(ctx, mobEffect, BuiltInRegistries.MOB_EFFECT).ifPresent(mobEffect ->
        {
            target.getLivingEntity().removeEffect(mobEffect);
        });
    }
}
