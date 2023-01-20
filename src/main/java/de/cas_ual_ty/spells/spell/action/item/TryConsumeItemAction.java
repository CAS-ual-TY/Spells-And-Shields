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

public class TryConsumeItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<TryConsumeItemAction> makeCodec(SpellActionType<TryConsumeItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(TryConsumeItemAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.singleTarget("user")).forGetter(TryConsumeItemAction::getUser),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("success")).forGetter(TryConsumeItemAction::getSuccess)
        ).apply(instance, (activation, target, amount, user, success) -> new TryConsumeItemAction(type, activation, target, amount, user, success)));
    }
    
    public static TryConsumeItemAction make(String activation, String target, DynamicCtxVar<Integer> damage, String user, String success)
    {
        return new TryConsumeItemAction(SpellActionTypes.TRY_CONSUME_ITEM.get(), activation, target, damage, user, success);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected String user;
    protected String success;
    
    public TryConsumeItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryConsumeItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> amount, String user, String success)
    {
        super(type, activation, targets);
        this.amount = amount;
        this.user = user;
        this.success = success;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public String getUser()
    {
        return user;
    }
    
    public String getSuccess()
    {
        return success;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(itemTarget.getItem().getCount() < amount)
            {
                return;
            }
            
            ctx.activate(success);
            
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
