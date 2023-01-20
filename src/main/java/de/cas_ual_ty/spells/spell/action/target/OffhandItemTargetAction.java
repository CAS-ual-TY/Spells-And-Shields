package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

public class OffhandItemTargetAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<OffhandItemTargetAction> makeCodec(SpellActionType<OffhandItemTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("item")).forGetter(OffhandItemTargetAction::getDst)
        ).apply(instance, (activation, source, dst) -> new OffhandItemTargetAction(type, activation, source, dst)));
    }
    
    public static OffhandItemTargetAction make(String activation, String targets, String dst)
    {
        return new OffhandItemTargetAction(SpellActionTypes.OFFHAND_ITEM_TARGET.get(), activation, targets, dst);
    }
    
    protected String dst;
    
    public OffhandItemTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public OffhandItemTargetAction(SpellActionType<?> type, String activation, String targets, String dst)
    {
        super(type, activation, targets);
        this.dst = dst;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        ctx.getOrCreateTargetGroup(dst).addTargets(Target.of(playerTarget.getLevel(), playerTarget.getPlayer().getOffhandItem()));
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}
