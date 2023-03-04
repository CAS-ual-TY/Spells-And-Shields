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

public class MainhandItemTargetAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<MainhandItemTargetAction> makeCodec(SpellActionType<MainhandItemTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("item")).forGetter(MainhandItemTargetAction::getDst)
        ).apply(instance, (activation, source, dst) -> new MainhandItemTargetAction(type, activation, source, dst)));
    }
    
    public static MainhandItemTargetAction make(String activation, String target, String dst)
    {
        return new MainhandItemTargetAction(SpellActionTypes.MAINHAND_ITEM_TARGET.get(), activation, target, dst);
    }
    
    protected String dst;
    
    public MainhandItemTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public MainhandItemTargetAction(SpellActionType<?> type, String activation, String target, String dst)
    {
        super(type, activation, target);
        this.dst = dst;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        ctx.getOrCreateTargetGroup(dst).addTargets(Target.of(playerTarget.getLevel(), playerTarget.getPlayer().getMainHandItem()));
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}
