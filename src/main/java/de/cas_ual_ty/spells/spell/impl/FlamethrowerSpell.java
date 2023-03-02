package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellDataHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.ITickedDataSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class FlamethrowerSpell extends BaseIngredientsSpell implements IProjectileSpell, ITickedDataSpell
{
    public final int defaultFireSeconds;
    public final int defaultRepetitions;
    public final int defaultRepetitionDelay;
    public final int defaultShotsPerRepetition;
    public final float defaultInaccuracy;
    
    protected int fireSeconds;
    protected int repetitions;
    protected int repetitionDelay;
    protected int shotsPerRepetition;
    protected float inaccuracy;
    
    protected int maxTime;
    
    public FlamethrowerSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int fireSeconds, int repetitions, int repetitionDelay, int shotsPerRepetition, float inaccuracy)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultFireSeconds = fireSeconds;
        defaultRepetitions = repetitions;
        defaultRepetitionDelay = repetitionDelay;
        defaultShotsPerRepetition = shotsPerRepetition;
        defaultInaccuracy = inaccuracy;
    }
    
    public FlamethrowerSpell(float manaCost, ItemStack ingredient, int fireSeconds, int repetitions, int repetitionDelay, int shotsPerRepetition, float inaccuracy)
    {
        super(manaCost, ingredient);
        defaultFireSeconds = fireSeconds;
        defaultRepetitions = repetitions;
        defaultRepetitionDelay = repetitionDelay;
        defaultShotsPerRepetition = shotsPerRepetition;
        defaultInaccuracy = inaccuracy;
    }
    
    public FlamethrowerSpell(float manaCost, int fireSeconds, int repetitions, int repetitionDelay, int shotsPerRepetition, float inaccuracy)
    {
        super(manaCost);
        defaultFireSeconds = fireSeconds;
        defaultRepetitions = repetitions;
        defaultRepetitionDelay = repetitionDelay;
        defaultShotsPerRepetition = shotsPerRepetition;
        defaultInaccuracy = inaccuracy;
    }
    
    public FlamethrowerSpell()
    {
        this(7F, new ItemStack(Items.BLAZE_POWDER), 10, 5, 4, 3, 15F);
    }
    
    @Override
    public int getTimeout()
    {
        return 20;
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        SpellDataHolder.getSpellDataHolder(manaHolder.getPlayer()).ifPresent(spellDataHolder ->
        {
            spellDataHolder.add(ITickedDataSpell.makeData(SpellsRegistries.FLAMETHROWER_DATA.get()));
        });
    }
    
    @Override
    public void projectileHitEntity(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entityHitResult.getEntity() instanceof LivingEntity hit)
        {
            hit.setSecondsOnFire(fireSeconds);
        }
        
        IProjectileSpell.super.projectileHitEntity(entity, entityHitResult);
    }
    
    @Override
    public void projectileHitBlock(SpellProjectile entity, BlockHitResult blockHitResult)
    {
        if(!entity.level.isClientSide)
        {
            BlockPos blockpos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            if(entity.level.isEmptyBlock(blockpos))
            {
                entity.level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(entity.level, blockpos));
            }
        }
        
        IProjectileSpell.super.projectileHitBlock(entity, blockHitResult);
    }
    
    @Override
    public int getMaxTime(SpellDataHolder spellDataHolder)
    {
        return maxTime;
    }
    
    @Override
    public void dataTick(SpellDataHolder spellDataHolder, int tickTime)
    {
        ManaHolder.getManaHolder(spellDataHolder.getEntity()).ifPresent(manaHolder ->
        {
            if(tickTime % repetitionDelay == 0)
            {
                for(int i = 0; i < shotsPerRepetition; i++)
                {
                    shootStraight(manaHolder, 2F, inaccuracy, (projectile, level) -> {});
                }
                
                spellDataHolder.getEntity().level.playSound(null, manaHolder.getPlayer(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1F, 1F);
            }
        });
    }
    
    @Override
    public void projectileTick(SpellProjectile entity)
    {
        if(entity.level.isClientSide)
        {
            Vec3 pos = entity.position();
            Random random = new Random();
            
            if(entity.tickCount % 4 == 0)
            {
                entity.level.addParticle(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 0, 0, 0);
            }
            
            final double spread = 0.1D;
            entity.level.addParticle(ParticleTypes.SMOKE, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
        }
    }
    
    @Override
    public ParticleOptions getTrailParticle()
    {
        return ParticleTypes.FLAME;
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("fireSeconds", defaultFireSeconds);
        json.addProperty("repetitions", defaultRepetitions);
        json.addProperty("repetitionDelay", defaultRepetitionDelay);
        json.addProperty("shotsPerRepetition", defaultShotsPerRepetition);
        json.addProperty("inaccuracy", defaultInaccuracy);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        fireSeconds = SpellsFileUtil.jsonInt(json, "fireSeconds");
        repetitions = SpellsFileUtil.jsonInt(json, "repetitions");
        repetitionDelay = SpellsFileUtil.jsonInt(json, "repetitionDelay");
        shotsPerRepetition = SpellsFileUtil.jsonInt(json, "shotsPerRepetition");
        inaccuracy = SpellsFileUtil.jsonFloat(json, "inaccuracy");
        
        calculateMaxTime();
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        fireSeconds = defaultFireSeconds;
        repetitions = defaultRepetitions;
        repetitionDelay = defaultRepetitionDelay;
        shotsPerRepetition = defaultShotsPerRepetition;
        inaccuracy = defaultInaccuracy;
        
        calculateMaxTime();
    }
    
    protected void calculateMaxTime()
    {
        if(repetitions > 0 && repetitionDelay > 0)
        {
            maxTime = repetitions * repetitionDelay;
        }
        else
        {
            repetitions = 1;
            repetitionDelay = 0;
            maxTime = 0;
        }
    }
}