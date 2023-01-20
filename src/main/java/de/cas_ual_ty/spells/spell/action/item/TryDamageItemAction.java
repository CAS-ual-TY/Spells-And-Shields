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
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class TryDamageItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<TryDamageItemAction> makeCodec(SpellActionType<TryDamageItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(TryDamageItemAction::getDamage),
                Codec.STRING.fieldOf(ParamNames.singleTarget("user")).forGetter(TryDamageItemAction::getUser),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("success")).forGetter(TryDamageItemAction::getSuccess)
        ).apply(instance, (activation, target, damage, user, success) -> new TryDamageItemAction(type, activation, target, damage, user, success)));
    }
    
    public static TryDamageItemAction make(String activation, String target, DynamicCtxVar<Integer> damage, String user, String success)
    {
        return new TryDamageItemAction(SpellActionTypes.DAMAGE_ITEM.get(), activation, target, damage, user, success);
    }
    
    protected DynamicCtxVar<Integer> damage;
    protected String user;
    protected String success;
    
    public TryDamageItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryDamageItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> damage, String user, String success)
    {
        super(type, activation, targets);
        this.damage = damage;
        this.user = user;
        this.success = success;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
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
        damage.getValue(ctx).ifPresent(damage ->
        {
            if(itemTarget.getItem().getMaxDamage() - itemTarget.getItem().getDamageValue() < damage)
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
                            itemTarget.getItem().hurt(damage, SpellsUtil.RANDOM, player);
                        }
                        
                        done.set(true);
                    }
                });
            });
            
            if(!done.get())
            {
                itemTarget.getItem().hurt(damage, SpellsUtil.RANDOM, null);
            }
        });
        
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}
