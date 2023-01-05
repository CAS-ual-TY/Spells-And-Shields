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

public class DamageItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<DamageItemAction> makeCodec(SpellActionType<DamageItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(DamageItemAction::getDamage),
                Codec.STRING.fieldOf(ParamNames.singleTarget("user")).forGetter(DamageItemAction::getUser)
        ).apply(instance, (activation, target, damage, user) -> new DamageItemAction(type, activation, target, damage, user)));
    }
    
    public static DamageItemAction make(String activation, String target, DynamicCtxVar<Integer> damage, String user)
    {
        return new DamageItemAction(SpellActionTypes.DAMAGE_ITEM.get(), activation, target, damage, user);
    }
    
    protected DynamicCtxVar<Integer> damage;
    protected String user;
    
    public DamageItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DamageItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> damage, String user)
    {
        super(type, activation, targets);
        this.damage = damage;
        this.user = user;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
    }
    
    public String getUser()
    {
        return user;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        damage.getValue(ctx).ifPresent(damage ->
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
        return null;
    }
}
