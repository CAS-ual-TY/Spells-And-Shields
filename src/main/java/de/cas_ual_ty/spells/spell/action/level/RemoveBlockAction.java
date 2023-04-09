package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import net.minecraft.world.level.block.Blocks;

public class RemoveBlockAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<RemoveBlockAction> makeCodec(SpellActionType<RemoveBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec()
        ).apply(instance, (activation, multiTargets) -> new RemoveBlockAction(type, activation, multiTargets)));
    }
    
    public static RemoveBlockAction make(String activation, String multiTargets)
    {
        return new RemoveBlockAction(SpellActionTypes.REMOVE_BLOCK.get(), activation, multiTargets);
    }
    
    public RemoveBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RemoveBlockAction(SpellActionType<?> type, String activation, String multiTargets)
    {
        super(type, activation, multiTargets);
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        ctx.level.setBlockAndUpdate(positionTarget.getBlockPos(), Blocks.AIR.defaultBlockState());
    }
}
