package de.cas_ual_ty.spells.spell.action.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Mob;

public class ClearTargetAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ClearTargetAction> makeCodec(SpellActionType<ClearTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.multiTarget("mobs")).forGetter(ClearTargetAction::getMultiTargets)
        ).apply(instance, (activation, mobs) -> new ClearTargetAction(type, activation, mobs)));
    }
    
    public static ClearTargetAction make(String activation, String mobs)
    {
        return new ClearTargetAction(SpellActionTypes.CLEAR_TARGET.get(), activation, mobs);
    }
    
    public ClearTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ClearTargetAction(SpellActionType<?> type, String activation, String mobs)
    {
        super(type, activation, mobs);
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        if(target.getLivingEntity() instanceof Mob mob)
        {
            mob.setTarget(target.getLivingEntity());
        }
    }
}
