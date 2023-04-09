package de.cas_ual_ty.spells.spell.action.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Mob;

public class SetTargetAction extends AffectSingleTypeAction<LivingEntityTarget>
{
    public static Codec<SetTargetAction> makeCodec(SpellActionType<SetTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.multiTarget("mobs")).forGetter(SetTargetAction::getMobs)
        ).apply(instance, (activation, singleTarget, mobs) -> new SetTargetAction(type, activation, singleTarget, mobs)));
    }
    
    public static SetTargetAction make(String activation, String singleTarget, String mobs)
    {
        return new SetTargetAction(SpellActionTypes.SET_TARGET.get(), activation, singleTarget, mobs);
    }
    
    protected String mobs;
    
    public SetTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SetTargetAction(SpellActionType<?> type, String activation, String singleTarget, String mobs)
    {
        super(type, activation, singleTarget);
        this.mobs = mobs;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public String getMobs()
    {
        return mobs;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        ctx.getTargetGroup(mobs).forEachTarget(t ->
        {
            TargetTypes.LIVING_ENTITY.get().ifType(t, mob ->
            {
                if(mob.getLivingEntity() instanceof Mob mob1)
                {
                    mob1.setTarget(target.getLivingEntity());
                }
            });
        });
    }
}
