package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ForcedTeleportSpell extends RandomTeleportSpell
{
    public final int defaultRange;
    protected int range;
    
    public ForcedTeleportSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int attempts, int teleportRange, int range)
    {
        super(manaCost, handIngredients, inventoryIngredients, attempts, teleportRange);
        defaultRange = range;
    }
    
    public ForcedTeleportSpell(float manaCost, ItemStack handIngredient, int attempts, int teleportRange, int range)
    {
        super(manaCost, handIngredient, attempts, teleportRange);
        defaultRange = range;
    }
    
    public ForcedTeleportSpell(float manaCost, int attempts, int teleportRange, int range)
    {
        super(manaCost, attempts, teleportRange);
        defaultRange = range;
    }
    
    public ForcedTeleportSpell()
    {
        this(10F, new ItemStack(Items.CHORUS_FRUIT), 5, 32, 20);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        Level level = manaHolder.getPlayer().level;
        
        LivingEntity entity = manaHolder.getPlayer();
        HitResult hit = SpellsUtil.rayTrace(level, entity, range, e -> e instanceof LivingEntity, 0.5F, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
        
        if(hit.getType() == HitResult.Type.ENTITY)
        {
            EntityHitResult entityHit = (EntityHitResult) hit;
            
            if(entityHit.getEntity() instanceof LivingEntity target)
            {
                randomTeleport(target, level, attempts, teleportRange);
            }
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("range", defaultRange);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        range = SpellsFileUtil.jsonInt(json, "range");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        range = defaultRange;
    }
}