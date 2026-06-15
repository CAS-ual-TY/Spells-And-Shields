package de.cas_ual_ty.spells.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.*;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIconType;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.neoforged.bus.api.IEventBus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class SpellsCodecs
{
    public static Codec<Holder<Spell>> SPELL;
    public static Codec<Holder<SpellTree>> SPELL_TREE;
    
    public static Codec<RequirementType<?>> REQUIREMENT_TYPE;
    public static Codec<SpellActionType<?>> SPELL_ACTION_TYPE;
    public static Codec<CtxVarType<?>> CTX_VAR_TYPE;
    public static Codec<SpellIconType<?>> SPELL_ICON_TYPE;
    public static Codec<ITargetType<?>> TARGET_TYPE;
    
    public static Codec<Requirement> REQUIREMENT;
    public static Codec<SpellAction> SPELL_ACTION;
    public static Codec<CtxVar<?>> CTX_VAR;
    public static Codec<SpellIcon> SPELL_ICON;
    
    public static Codec<SpellNode> SPELL_NODE;
    public static Codec<SpellTree> SPELL_TREE_CONTENTS;
    public static Codec<SpellTree> SPELL_TREE_SYNC;
    
    public static Codec<Spell> SPELL_CONTENTS;
    public static Codec<Spell> SPELL_SYNC;
    
    public static Codec<Map<String, String>> STRING_MAP;
    
    public static void makeCodecs(IEventBus modEventBus)
    {
        SPELL = Codec.lazyInitialized(() -> RegistryFileCodec.create(Spells.REGISTRY_KEY, Codec.lazyInitialized(() -> SPELL_CONTENTS), false));
        SPELL_TREE = Codec.lazyInitialized(() -> RegistryFixedCodec.create(SpellTrees.REGISTRY_KEY));
        
        REQUIREMENT_TYPE = Codec.lazyInitialized(() -> RequirementTypes.REGISTRY.byNameCodec());
        CTX_VAR_TYPE = Codec.lazyInitialized(() -> CtxVarTypes.REGISTRY.byNameCodec());
        SPELL_ACTION_TYPE = Codec.lazyInitialized(() -> SpellActionTypes.REGISTRY.byNameCodec());
        SPELL_ICON_TYPE = Codec.lazyInitialized(() -> SpellIconTypes.REGISTRY.byNameCodec());
        TARGET_TYPE = Codec.lazyInitialized(() -> TargetTypes.REGISTRY.byNameCodec());
        
        REQUIREMENT = Codec.lazyInitialized(() -> REQUIREMENT_TYPE.dispatch("type", Requirement::getType, type -> MapCodec.assumeMapUnsafe(type.getCodec())));
        SPELL_ACTION = Codec.lazyInitialized(() -> SPELL_ACTION_TYPE.dispatch("type", SpellAction::getType, type -> MapCodec.assumeMapUnsafe(type.getCodec())));
        CTX_VAR = Codec.lazyInitialized(() -> CTX_VAR_TYPE.dispatch("type", CtxVar::getType, type -> MapCodec.assumeMapUnsafe(type.getCodec())));
        SPELL_ICON = Codec.lazyInitialized(() -> SPELL_ICON_TYPE.dispatch("type", SpellIcon::getType, type -> MapCodec.assumeMapUnsafe(type.getCodec())));
        
        SPELL_NODE = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                Codec.lazyInitialized(() -> SPELL).fieldOf("n1/spell_id").forGetter(node -> node.getSpellInstance().getSpell()),
                Codec.FLOAT.optionalFieldOf("n7/mana_cost").xmap(o -> o.orElse(-1F), manaCost -> manaCost >= 0 ? Optional.of(manaCost) : Optional.empty()).forGetter(node -> node.getSpellInstance().getManaCost()),
                Codec.lazyInitialized(() -> CTX_VAR).listOf().optionalFieldOf("n8/spell_parameters").xmap(o -> o.orElse(new LinkedList<>()), p -> Optional.of(p).map(l -> l.isEmpty() ? null : l)).forGetter(node -> node.getSpellInstance().getParameters()),
                Codec.INT.fieldOf("n4/level_cost").forGetter(SpellNode::getLevelCost),
                REQUIREMENT.listOf().optionalFieldOf("n5/hidden_requirements").xmap(o -> o.orElse(new LinkedList<>()), r -> Optional.of(r).map(l -> l.isEmpty() ? null : l)).forGetter(SpellNode::getHiddenRequirements),
                REQUIREMENT.listOf().optionalFieldOf("n6/learn_requirements").xmap(o -> o.orElse(new LinkedList<>()), r -> Optional.of(r).map(l -> l.isEmpty() ? null : l)).forGetter(SpellNode::getLearnRequirements),
                Codec.lazyInitialized(() -> SPELL_NODE).listOf().fieldOf("n9/child_nodes").forGetter(SpellNode::getChildren),
                Codec.INT.optionalFieldOf("n2/node_id").xmap(o -> o.map(i -> new SpellNodeId(null, i)).orElse(null), nodeId -> Optional.ofNullable(nodeId).map(SpellNodeId::nodeId)).forGetter(SpellNode::getNodeId),
                Codec.intRange(0, 2).optionalFieldOf("n3/node_frame").xmap(o -> o.orElse(0), f -> Optional.of(f).map(i -> i <= 0 ? null : i)).forGetter(SpellNode::getFrame)
        ).apply(instance, (spell, manaCost, variables, levelCost, hiddenRequirements, learnRequirements, children, id, frame) -> new SpellNode(id, new SpellInstance(spell, manaCost, variables), levelCost, hiddenRequirements, learnRequirements, children, frame))));
        
        SPELL_TREE_CONTENTS = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                SPELL_NODE.fieldOf("t3/root_node").forGetter(SpellTree::getRoot),
                ComponentSerialization.CODEC.fieldOf("t1/title").forGetter(SpellTree::getTitle),
                SPELL_ICON.fieldOf("t2/icon").forGetter(SpellTree::getIcon)
        ).apply(instance, SpellTree::new)));
        
        SPELL_TREE_SYNC = Codec.unit(() -> new SpellTree());
        
        SPELL_CONTENTS = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                Codec.lazyInitialized(() -> SPELL_ACTION).listOf().fieldOf("s7/spell_actions").forGetter(Spell::getSpellActions),
                Codec.lazyInitialized(() -> SPELL_ICON).fieldOf("s2/icon").forGetter(Spell::getIcon),
                ComponentSerialization.CODEC.fieldOf("s1/title").forGetter(Spell::getTitle),
                ComponentSerialization.CODEC.listOf().fieldOf("s4/tooltip").forGetter(Spell::getTooltip),
                Codec.FLOAT.fieldOf("s3/mana_cost").xmap(f -> Math.max(0, f), f -> Math.max(0, f)).forGetter(Spell::getManaCost),
                CTX_VAR.listOf().fieldOf("s6/spell_parameters").forGetter(Spell::getParameters),
                Codec.STRING.listOf().fieldOf("s5/spell_events").forGetter(Spell::getEventsList)
        ).apply(instance, Spell::new)));
        
        SPELL_SYNC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                Codec.lazyInitialized(() -> SPELL_ICON).fieldOf("s2/icon").forGetter(Spell::getIcon),
                ComponentSerialization.CODEC.fieldOf("s1/title").forGetter(Spell::getTitle),
                ComponentSerialization.CODEC.listOf().fieldOf("s4/tooltip").forGetter(s -> s.getTooltip().isEmpty() ? ImmutableList.of(Component.empty()) : s.getTooltip()),
                Codec.FLOAT.fieldOf("s3/mana_cost").xmap(f -> Math.max(0, f), f -> Math.max(0, f)).forGetter(Spell::getManaCost)
        ).apply(instance, Spell::new)));
        
        STRING_MAP = new PrimitiveCodec<>()
        {
            @Override
            public <T> DataResult<Map<String, String>> read(DynamicOps<T> ops, T input0)
            {
                if(ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED || ops instanceof RegistryOps)
                {
                    JsonElement input = (JsonElement) input0;
                    if(input.isJsonObject())
                    {
                        JsonObject jsonMap = input.getAsJsonObject();
                        Map<String, String> map = new HashMap<>();
                        for(String key : jsonMap.keySet())
                        {
                            JsonElement value = jsonMap.get(key);
                            if(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString())
                            {
                                map.put(key, value.getAsString());
                            }
                            else
                            {
                                return DataResult.error(() -> "Value with key " + key + " is not a string");
                            }
                        }
                        
                        return DataResult.success(map);
                    }
                    else
                    {
                        DataResult.error(() -> "Not a json object");
                    }
                }
                
                return DataResult.error(() -> "Codec only works on JsonOps");
            }
            
            @Override
            public <T> T write(DynamicOps<T> ops, Map<String, String> value)
            {
                if(ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED || ops instanceof RegistryOps)
                {
                    JsonObject jsonMap = new JsonObject();
                    value.forEach(jsonMap::addProperty);
                    return (T) jsonMap;
                }
                
                return ops.empty();
            }
        };
    }
}
