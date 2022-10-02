package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class EnderArmySpell extends BaseIngredientsSpell
{
    public final int defaultTargetRange;
    public final int defaultEndermanRange;
    
    protected int targetRange;
    protected int endermanRange;
    
    public EnderArmySpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int targetRange, int endermanRange)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultTargetRange = targetRange;
        defaultEndermanRange = endermanRange;
    }
    
    public EnderArmySpell(float manaCost, ItemStack handIngredient, int targetRange, int endermanRange)
    {
        super(manaCost, handIngredient);
        defaultTargetRange = targetRange;
        defaultEndermanRange = endermanRange;
    }
    
    public EnderArmySpell(float manaCost, int targetRange, int endermanRange)
    {
        super(manaCost);
        defaultTargetRange = targetRange;
        defaultEndermanRange = endermanRange;
    }
    
    public EnderArmySpell()
    {
        this(20F, new ItemStack(Items.DRAGON_HEAD), 50, 40);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer().level instanceof ServerLevel level)
        {
            LivingEntity entity = manaHolder.getPlayer();
            HitResult hit = SpellsUtil.rayTrace(level, entity, targetRange, e -> e instanceof LivingEntity, 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
            
            if(hit.getType() == HitResult.Type.ENTITY)
            {
                EntityHitResult entityHit = (EntityHitResult) hit;
                
                if(entityHit.getEntity() instanceof LivingEntity target)
                {
                    int range = endermanRange * 2;
                    List<Entity> list = level.getEntities(target, AABB.ofSize(target.position(), range, range, range), e -> e instanceof EnderMan);
                    list.stream().filter(e -> e instanceof EnderMan).map(e -> (EnderMan) e).forEach(e ->
                    {
                        e.setTarget(target);
                        e.setBeingStaredAt();
                        
                        double spread = 0.25D;
                        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, e.getEyePosition().x, e.getEyePosition().y, e.getEyePosition().z, 3, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, 0D);
                    });
                    
                    level.playSound(null, manaHolder.getPlayer(), SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1F, 1F);
                    level.playSound(null, target, SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1F, 1F);
                }
            }
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("targetRange", defaultTargetRange);
        json.addProperty("endermanRange", defaultEndermanRange);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        targetRange = SpellsFileUtil.jsonInt(json, "targetRange");
        endermanRange = SpellsFileUtil.jsonInt(json, "endermanRange");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        targetRange = defaultTargetRange;
        endermanRange = defaultEndermanRange;
    }
}
