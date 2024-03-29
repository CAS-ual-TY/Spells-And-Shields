package de.cas_ual_ty.spells.spell.action.item;

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
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class UseItemOnBlockAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<UseItemOnBlockAction> makeCodec(SpellActionType<UseItemOnBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.multiTarget("blocks")).forGetter(UseItemOnBlockAction::getBlocks),
                ItemStack.CODEC.fieldOf("item").forGetter(UseItemOnBlockAction::getItem),
                Codec.BOOL.fieldOf(ParamNames.paramBooleanImm("offhand")).forGetter(UseItemOnBlockAction::getOffhand),
                SpellsUtil.namedEnumCodec(Direction::byName, Direction::getName).fieldOf("direction").forGetter(UseItemOnBlockAction::getDirection)
        ).apply(instance, (activation, source, blocks, item, offhand, direction) -> new UseItemOnBlockAction(type, activation, source, blocks, item, offhand, direction)));
    }
    
    public static UseItemOnBlockAction make(Object activation, Object source, String blocks, ItemStack item, boolean offhand, Direction direction)
    {
        return new UseItemOnBlockAction(SpellActionTypes.USE_ITEM_ON_BLOCK.get(), activation.toString(), source.toString(), blocks, item, offhand, direction);
    }
    
    protected String blocks;
    protected ItemStack item;
    protected boolean offhand;
    protected Direction direction;
    
    public UseItemOnBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public UseItemOnBlockAction(SpellActionType<?> type, String activation, String source, String blocks, ItemStack item, boolean offhand, Direction direction)
    {
        super(type, activation, source);
        this.blocks = blocks;
        this.item = item;
        this.offhand = offhand;
        this.direction = direction;
    }
    
    public String getBlocks()
    {
        return blocks;
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
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        ctx.getTargetGroup(blocks).forEachType(TargetTypes.POSITION.get(), positionTarget ->
        {
            ItemStack item = this.item.copy();
            item.useOn(new UseOnContext(ctx.level, null, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, item, new BlockHitResult(positionTarget.getPosition(), direction, positionTarget.getBlockPos(), false)));
        });
    }
}
