package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumeItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<ConsumeItemAction> makeCodec(SpellActionType<ConsumeItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(ConsumeItemAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.singleTarget("user")).forGetter(ConsumeItemAction::getUser)
        ).apply(instance, (activation, target, amount, user) -> new ConsumeItemAction(type, activation, target, amount, user)));
    }
    
    public static ConsumeItemAction make(String activation, String target, DynamicCtxVar<Integer> damage, String user)
    {
        return new ConsumeItemAction(SpellActionTypes.CONSUME_ITEM.get(), activation, target, damage, user);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected String user;
    
    public ConsumeItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ConsumeItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> amount, String user)
    {
        super(type, activation, targets);
        this.amount = amount;
        this.user = user;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public String getUser()
    {
        return user;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            TargetGroup userGroup = ctx.getTargetGroup(user);
            AtomicBoolean done = new AtomicBoolean(false);
            
            userGroup.getSingleTarget(t ->
            {
                TargetTypes.PLAYER.get().ifType(t, t1 ->
                {
                    if(t1.getPlayer() instanceof ServerPlayer player)
                    {
                        if(!player.isCreative())
                        {
                            itemTarget.getItem().shrink(amount);
                        }
                        
                        done.set(true);
                    }
                });
            });
            
            if(!done.get())
            {
                itemTarget.getItem().shrink(amount);
            }
        });
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}
