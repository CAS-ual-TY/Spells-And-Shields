package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class DrainFlameSpell extends BaseIngredientsSpell
{
    public DrainFlameSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public DrainFlameSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    public DrainFlameSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public DrainFlameSpell()
    {
        super(2F);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity entity = manaHolder.getPlayer();
        Level level = entity.level;
        HitResult hit = SpellsUtil.rayTrace(level, entity, 20, e -> e instanceof LivingEntity, 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        
        if(hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit)
        {
            BlockPos pos = blockHit.getBlockPos();
            
            for(int i = 0; i < 2; i++)
            {
                BlockState blockState = level.getBlockState(pos);
                BlockPos below = pos.below();
                
                // look at the fire (first iteration)
                // or at the block below the fire (second iteration)
                if(blockState.getBlock() instanceof BaseFireBlock baseFireBlock && level.getBlockState(below).isFireSource(level, below, Direction.UP))
                {
                    level.removeBlock(pos, false);
                    manaHolder.getPlayer().addEffect(new MobEffectInstance(SpellsRegistries.REPLENISHMENT_EFFECT.get(), 200));
                    break;
                }
                
                pos = pos.above();
            }
        }
    }
}
