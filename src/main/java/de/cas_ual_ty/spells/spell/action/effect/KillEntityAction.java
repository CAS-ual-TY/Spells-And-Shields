package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import net.minecraft.world.entity.LivingEntity;

public class KillEntityAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<KillEntityAction> makeCodec(SpellActionType<KillEntityAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec()
        ).apply(instance, (activation, multiTargets) -> new KillEntityAction(type, activation, multiTargets)));
    }

    public static KillEntityAction make(Object activation, Object multiTargets)
    {
        return new KillEntityAction(SpellActionTypes.KILL_ENTITY.get(), activation.toString(), multiTargets.toString());
    }

    public KillEntityAction(SpellActionType<?> type)
    {
        super(type);
    }

    public KillEntityAction(SpellActionType<?> type, String activation, String multiTargets)
    {
        super(type, activation, multiTargets);
    }

    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        target.getEntity().kill();
    }
}
