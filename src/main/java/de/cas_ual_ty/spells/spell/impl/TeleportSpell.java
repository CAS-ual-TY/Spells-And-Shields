package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TeleportSpell extends BaseIngredientsSpell
{
    public final int defaultTeleportRange;
    protected int teleportRange;
    
    public TeleportSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int teleportRange)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultTeleportRange = teleportRange;
    }
    
    public TeleportSpell(float manaCost, ItemStack handIngredient, int teleportRange)
    {
        super(manaCost, handIngredient);
        defaultTeleportRange = teleportRange;
    }
    
    public TeleportSpell(float manaCost, int teleportRange)
    {
        super(manaCost);
        defaultTeleportRange = teleportRange;
    }
    
    public TeleportSpell()
    {
        this(10F, new ItemStack(Items.CHORUS_FRUIT), 32);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        Level level = manaHolder.getPlayer().level;
        
        LivingEntity entity = manaHolder.getPlayer();
        HitResult hit = SpellsUtil.rayTrace(level, entity, teleportRange, e -> true, 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY);
        
        Vec3 oldPos = entity.position();
        
        if(hit.getType() != HitResult.Type.MISS)
        {
            Vec3 pos = null;
            
            if(hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit)
            {
                pos = blockHit.getLocation().add(0, 1.0E-5D, 0);
            }
            else if(hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit)
            {
                pos = entityHit.getEntity().position().add(0, 1.0E-5D, 0);
            }
            
            if(pos != null)
            {
                level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
                entity.teleportTo(pos.x, pos.y, pos.z);
                level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
            }
        }
        else
        {
            Vec3 dir = entity.getLookAngle();
            Vec3 pos = entity.position().add(dir.scale(teleportRange));
            
            level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
            entity.teleportTo(pos.x, pos.y, pos.z);
            level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("teleportRange", defaultTeleportRange);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        teleportRange = SpellsFileUtil.jsonInt(json, "teleportRange");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        teleportRange = defaultTeleportRange;
    }
}
