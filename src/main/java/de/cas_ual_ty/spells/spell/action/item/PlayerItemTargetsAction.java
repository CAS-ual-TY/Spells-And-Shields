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
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class PlayerItemTargetsAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<PlayerItemTargetsAction> makeCodec(SpellActionType<PlayerItemTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("items")).forGetter(PlayerItemTargetsAction::getDst),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("include_hands")).forGetter(PlayerItemTargetsAction::getIncludeHands),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("include_inventory")).forGetter(PlayerItemTargetsAction::getIncludeInventory),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("include_armor")).forGetter(PlayerItemTargetsAction::getIncludeArmor)
        ).apply(instance, (activation, source, dst, includeHands, includeInventory, includeArmor) -> new PlayerItemTargetsAction(type, activation, source, dst, includeHands, includeInventory, includeArmor)));
    }
    
    public static PlayerItemTargetsAction make(Object activation, Object source, String dst, DynamicCtxVar<Boolean> includeHands, DynamicCtxVar<Boolean> includeInventory, DynamicCtxVar<Boolean> includeArmor)
    {
        return new PlayerItemTargetsAction(SpellActionTypes.PLAYER_ITEM_TARGETS.get(), activation.toString(), source.toString(), dst, includeHands, includeInventory, includeArmor);
    }
    
    protected String dst;
    protected DynamicCtxVar<Boolean> includeHands;
    protected DynamicCtxVar<Boolean> includeInventory;
    protected DynamicCtxVar<Boolean> includeArmor;
    
    public PlayerItemTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PlayerItemTargetsAction(SpellActionType<?> type, String activation, String source, String dst, DynamicCtxVar<Boolean> includeHands, DynamicCtxVar<Boolean> includeInventory, DynamicCtxVar<Boolean> includeArmor)
    {
        super(type, activation, source);
        this.dst = dst;
        this.includeHands = includeHands;
        this.includeInventory = includeInventory;
        this.includeArmor = includeArmor;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    public DynamicCtxVar<Boolean> getIncludeHands()
    {
        return includeHands;
    }
    
    public DynamicCtxVar<Boolean> getIncludeInventory()
    {
        return includeInventory;
    }
    
    public DynamicCtxVar<Boolean> getIncludeArmor()
    {
        return includeArmor;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        Player player = playerTarget.getPlayer();
        TargetGroup items = ctx.getOrCreateTargetGroup(dst);
        
        boolean hands = includeHands.getValue(ctx).orElse(false);
        boolean inventory = includeInventory.getValue(ctx).orElse(false);
        boolean armor = includeArmor.getValue(ctx).orElse(false);
        
        if(hands)
        {
            items.addTargets(Target.of(player.level(), player.getMainHandItem(), (i) -> player.setItemInHand(InteractionHand.MAIN_HAND, i), player.isCreative()));
            items.addTargets(Target.of(player.level(), player.getOffhandItem(), (i) -> player.setItemInHand(InteractionHand.OFF_HAND, i), player.isCreative()));
        }
        
        if(inventory)
        {
            for(int i = 0; i < player.getInventory().items.size(); i++)
            {
                if(i == player.getInventory().selected)
                {
                    continue;
                }
                
                int finalI = i;
                items.addTargets(Target.of(player.level(), player.getInventory().items.get(i), (j) -> player.getInventory().items.set(finalI, j), player.isCreative()));
            }
        }
        
        if(armor)
        {
            for(int i = 0; i < player.getInventory().armor.size(); i++)
            {
                if(i == player.getInventory().selected)
                {
                    continue;
                }
                
                int finalI = i;
                items.addTargets(Target.of(player.level(), player.getInventory().armor.get(i), (j) -> player.getInventory().armor.set(finalI, j), player.isCreative()));
            }
        }
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}
