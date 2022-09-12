package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RandomTeleportSpell extends BaseIngredientsSpell
{
    public final int defaultAttempts;
    public final int defaultTeleportRange;
    
    protected int attempts;
    protected int teleportRange;
    
    public RandomTeleportSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int attempts, int teleportRange)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultAttempts = attempts;
        defaultTeleportRange = teleportRange;
    }
    
    public RandomTeleportSpell(float manaCost, ItemStack handIngredient, int attempts, int teleportRange)
    {
        super(manaCost, handIngredient);
        defaultAttempts = attempts;
        defaultTeleportRange = teleportRange;
    }
    
    public RandomTeleportSpell(float manaCost, int attempts, int teleportRange)
    {
        super(manaCost);
        defaultAttempts = attempts;
        defaultTeleportRange = teleportRange;
    }
    
    public RandomTeleportSpell(float manaCost)
    {
        this(manaCost, 5, 32);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(!manaHolder.getPlayer().level.isClientSide && manaHolder.getPlayer().level instanceof ServerLevel level)
        {
            teleport(manaHolder.getPlayer(), level, attempts, teleportRange);
        }
    }
    
    public static void teleport(LivingEntity entity, ServerLevel level, int attempts, int range)
    {
        RandomSource random = entity.getRandom();
        Vec3 pos = entity.position();
        
        double x = 0;
        double y = 0;
        double z = 0;
        
        boolean success = false;
        int i = 0;
        while(!success && i < attempts)
        {
            x = entity.getX() + (random.nextDouble() - 0.5D) * range * 2;
            y = entity.getY() + (double) (random.nextInt(range * 2) - range);
            z = entity.getZ() + (random.nextDouble() - 0.5D) * range * 2;
            
            success = entity.randomTeleport(x, y, z, true);
            i++;
        }
        
        if(success)
        {
            level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        else
        {
            level.playSound(null, entity, SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("attempts", 5);
        json.addProperty("teleportRange", 32);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.attempts = SpellsFileUtil.jsonInt(json, "attempts");
        this.teleportRange = SpellsFileUtil.jsonInt(json, "teleportRange");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        attempts = defaultAttempts;
        teleportRange = defaultTeleportRange;
    }
}
