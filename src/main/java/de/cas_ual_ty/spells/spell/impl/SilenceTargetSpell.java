package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SilenceTargetSpell extends BaseIngredientsSpell
{
    public final double defaultRange;
    public final int defaultSilenceSeconds;
    
    protected double range;
    protected int silenceSeconds;
    
    public SilenceTargetSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, double range, int silenceSeconds)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        this.defaultRange = range;
        this.defaultSilenceSeconds = silenceSeconds;
    }
    
    public SilenceTargetSpell(float manaCost, ItemStack handIngredient, double range, int silenceSeconds)
    {
        super(manaCost, handIngredient);
        this.defaultRange = range;
        this.defaultSilenceSeconds = silenceSeconds;
    }
    
    public SilenceTargetSpell(float manaCost)
    {
        this(manaCost, new ItemStack(Items.AMETHYST_SHARD), 20D, 15);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(!manaHolder.getPlayer().level.isClientSide && manaHolder.getPlayer().level instanceof ServerLevel level)
        {
            LivingEntity entity = manaHolder.getPlayer();
            HitResult hit = SpellsUtil.rayTrace(level, entity, 20, e -> e instanceof LivingEntity, 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
            
            if(hit.getType() == HitResult.Type.ENTITY)
            {
                EntityHitResult entityHit = (EntityHitResult) hit;
                
                if(entityHit.getEntity() instanceof LivingEntity target)
                {
                    target.addEffect(new MobEffectInstance(SpellsRegistries.SILENCE_EFFECT.get(), 15));
                    
                    level.playSound(null, manaHolder.getPlayer(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.playSound(null, target, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    Vec3 pos = target.getEyePosition();
                    level.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 5, 0, 0, 0, 0D);
                }
            }
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("range", 20D);
        json.addProperty("silenceSeconds", 15);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        range = SpellsFileUtil.jsonDouble(json, "range");
        silenceSeconds = SpellsFileUtil.jsonInt(json, "silenceSeconds");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        range = defaultRange;
        silenceSeconds = defaultSilenceSeconds;
    }
}
