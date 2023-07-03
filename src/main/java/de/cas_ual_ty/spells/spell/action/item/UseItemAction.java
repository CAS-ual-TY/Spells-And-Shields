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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class UseItemAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<UseItemAction> makeCodec(SpellActionType<UseItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                ItemStack.CODEC.fieldOf("item").forGetter(UseItemAction::getItem),
                Codec.BOOL.fieldOf("offhand").forGetter(UseItemAction::getOffhand)
        ).apply(instance, (activation, source, item, offhand) -> new UseItemAction(type, activation, source, item, offhand)));
    }
    
    public static UseItemAction make(Object activation, Object source, ItemStack item, boolean offhand)
    {
        return new UseItemAction(SpellActionTypes.USE_ITEM.get(), activation.toString(), source.toString(), item, offhand);
    }
    
    protected ItemStack item;
    protected boolean offhand;
    
    public UseItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public UseItemAction(SpellActionType<?> type, String activation, String source, ItemStack item, boolean offhand)
    {
        super(type, activation, source);
        this.item = item;
        this.offhand = offhand;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public boolean getOffhand()
    {
        return offhand;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        boolean instabuild = playerTarget.getPlayer().getAbilities().instabuild;
        playerTarget.getPlayer().getAbilities().instabuild = true; // to stop item consumption
        ItemStack item = this.item.copy();
        item.use(ctx.level, playerTarget.getPlayer(), offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        playerTarget.getPlayer().getAbilities().instabuild = instabuild;
    }
}
