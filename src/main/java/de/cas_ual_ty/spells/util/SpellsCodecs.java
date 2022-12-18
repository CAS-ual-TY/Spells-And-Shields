package de.cas_ual_ty.spells.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
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
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;

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
    
    public static Codec<Spell> SPELL_CONTENTS;
    
    public static Codec<CtxVar<?>> CONTEXT_VARIABLE;
    public static Codec<SpellInstance> SPELL_INSTANCE;
    
    public static Codec<Component> COMPONENT; // json only, no NBT support
    
    public static void makeCodecs()
    {
        SPELL = ExtraCodecs.lazyInitializedCodec(() -> RegistryFileCodec.create(Spells.REGISTRY_KEY, ExtraCodecs.lazyInitializedCodec(() -> SPELL_CONTENTS), false));
        SPELL_TREE = ExtraCodecs.lazyInitializedCodec(() -> RegistryFixedCodec.create(SpellTrees.REGISTRY_KEY));
        
        REQUIREMENT_TYPE = ExtraCodecs.lazyInitializedCodec(() -> RequirementTypes.REGISTRY.get().getCodec());
        CTX_VAR_TYPE = ExtraCodecs.lazyInitializedCodec(() -> CtxVarTypes.REGISTRY.get().getCodec());
        SPELL_ACTION_TYPE = ExtraCodecs.lazyInitializedCodec(() -> SpellActionTypes.REGISTRY.get().getCodec());
        SPELL_ICON_TYPE = ExtraCodecs.lazyInitializedCodec(() -> SpellIconTypes.REGISTRY.get().getCodec());
        TARGET_TYPE = ExtraCodecs.lazyInitializedCodec(() -> TargetTypes.REGISTRY.get().getCodec());
        
        REQUIREMENT = ExtraCodecs.lazyInitializedCodec(() -> REQUIREMENT_TYPE.dispatch("type", Requirement::getType, RequirementType::getCodec));
        SPELL_ACTION = ExtraCodecs.lazyInitializedCodec(() -> SPELL_ACTION_TYPE.dispatch("type", SpellAction::getType, SpellActionType::getCodec));
        CTX_VAR = ExtraCodecs.lazyInitializedCodec(() -> CTX_VAR_TYPE.dispatch("type", CtxVar::getType, CtxVarType::getCodec));
        SPELL_ICON = ExtraCodecs.lazyInitializedCodec(() -> SPELL_ICON_TYPE.dispatch("type", SpellIcon::getType, SpellIconType::getCodec));
        
        SPELL_NODE = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.lazyInitializedCodec(() -> SPELL).fieldOf("spell").forGetter(node -> node.getSpellInstance().getSpell()),
                ExtraCodecs.lazyInitializedCodec(() -> CTX_VAR).listOf().fieldOf("variables").forGetter(node -> node.getSpellInstance().getVariables()),
                Codec.INT.fieldOf("level_cost").forGetter(SpellNode::getLevelCost),
                REQUIREMENT.listOf().fieldOf("requirements").forGetter(SpellNode::getRequirements),
                ExtraCodecs.lazyInitializedCodec(() -> SPELL_NODE).listOf().fieldOf("children").forGetter(SpellNode::getChildren)
        ).apply(instance, (spell, variables, levelCost, requirements, children) -> new SpellNode(new SpellInstance(spell, variables), levelCost, requirements, children))));
        
        SPELL_TREE_CONTENTS = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                SPELL_NODE.fieldOf("root").forGetter(SpellTree::getRoot),
                COMPONENT.fieldOf("title").forGetter(SpellTree::getTitle),
                SPELL.fieldOf("icon").forGetter(SpellTree::getIconSpell),
                REQUIREMENT.listOf().fieldOf("requirements").forGetter(SpellTree::getRequirements)
        ).apply(instance, SpellTree::new)));
        
        SPELL_CONTENTS = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.lazyInitializedCodec(() -> SPELL_ACTION).listOf().fieldOf("spell_actions").forGetter(Spell::getSpellActions),
                ExtraCodecs.lazyInitializedCodec(() -> SPELL_ICON).fieldOf("icon").forGetter(Spell::getIcon),
                COMPONENT.fieldOf("title").forGetter(Spell::getTitle),
                COMPONENT.listOf().fieldOf("tooltip").forGetter(Spell::getTooltip),
                Codec.FLOAT.fieldOf("mana_cost").forGetter(Spell::getManaCost)
        ).apply(instance, Spell::new)));
        
        SPELL_INSTANCE = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                SPELL.fieldOf("spell").forGetter(SpellInstance::getSpell),
                CONTEXT_VARIABLE.listOf().fieldOf("variables").forGetter(SpellInstance::getVariables)
        ).apply(instance, SpellInstance::new)));
        
        COMPONENT = ExtraCodecs.lazyInitializedCodec(() -> new PrimitiveCodec<>()
        {
            @Override
            public <T> DataResult<Component> read(DynamicOps<T> ops, T input0)
            {
                if(ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED || ops instanceof RegistryOps)
                {
                    JsonElement input = (JsonElement) input0;
                    return DataResult.success(Component.Serializer.fromJson(input));
                }
                
                return DataResult.error("Codec only works on JsonOps");
            }
            
            @Override
            public <T> T write(DynamicOps<T> ops, Component value)
            {
                if(ops == JsonOps.INSTANCE || ops == JsonOps.COMPRESSED || ops instanceof RegistryOps)
                {
                    return (T) Component.Serializer.toJsonTree(value);
                }
                
                return ops.empty();
            }
        });
    }
}
