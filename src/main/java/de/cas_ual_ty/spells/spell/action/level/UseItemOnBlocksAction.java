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
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class UseItemOnBlocksAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<UseItemOnBlocksAction> makeCodec(SpellActionType<UseItemOnBlocksAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("source")).forGetter(UseItemOnBlocksAction::getSource),
                ItemStack.CODEC.fieldOf("item").forGetter(UseItemOnBlocksAction::getItem),
                Codec.BOOL.fieldOf("offhand").forGetter(UseItemOnBlocksAction::getOffhand),
                SpellsUtil.namedEnumCodec(Direction::byName, Direction::getName).fieldOf("direction").forGetter(UseItemOnBlocksAction::getDirection)
        ).apply(instance, (activation, multiTargets, source, item, offhand, direction) -> new UseItemOnBlocksAction(type, activation, multiTargets, source, item, offhand, direction)));
    }
    
    public static UseItemOnBlocksAction make(String activation, String multiTargets, String source, ItemStack item, boolean offhand, Direction direction)
    {
        return new UseItemOnBlocksAction(SpellActionTypes.USE_ITEM_ON_BLOCK.get(), activation, multiTargets, source, item, offhand, direction);
    }
    
    protected String source;
    protected ItemStack item;
    protected boolean offhand;
    protected Direction direction;
    
    public UseItemOnBlocksAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public UseItemOnBlocksAction(SpellActionType<?> type, String activation, String multiTargets, String source, ItemStack item, boolean offhand, Direction direction)
    {
        super(type, activation, multiTargets);
        this.source = source;
        this.item = item;
        this.offhand = offhand;
        this.direction = direction;
    }
    
    public String getSource()
    {
        return source;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public boolean getOffhand()
    {
        return offhand;
    }
    
    public Direction getDirection()
    {
        return direction;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        ctx.getTargetGroup(source).getSingleTarget(t -> TargetTypes.PLAYER.get().ifType(t, player ->
        {
            ItemStack item = this.item.copy();
            item.useOn(new UseOnContext(ctx.level, null, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, item, new BlockHitResult(positionTarget.getPosition(), direction, positionTarget.getBlockPos(), false)));
        }));
    }
}
