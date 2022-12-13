package de.cas_ual_ty.spells.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.Spells;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.requirement.Requirement;
import de.cas_ual_ty.spells.requirement.RequirementType;
import de.cas_ual_ty.spells.spell.ISpell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;

public class SpellsCodecs
{
    public static Codec<ISpell> SPELL;
    public static Codec<RequirementType<?>> REQUIREMENT_TYPE;
    
    public static Codec<Requirement> REQUIREMENT;
    public static Codec<SpellNode> SPELL_NODE;
    public static Codec<SpellTree> SPELL_TREE_CONTENTS;
    
    public static void makeCodecs()
    {
        SPELL = ExtraCodecs.lazyInitializedCodec(() -> Spells.SPELLS_REGISTRY.get().getCodec());
        REQUIREMENT_TYPE = ExtraCodecs.lazyInitializedCodec(() -> SpellsRegistries.REQUIREMENTS_REGISTRY.get().getCodec());
        
        REQUIREMENT = ExtraCodecs.lazyInitializedCodec(() -> REQUIREMENT_TYPE.dispatch("type", Requirement::getType, RequirementType::getCodec));
        
        SPELL_NODE = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                Spells.SPELLS_REGISTRY.get().getCodec().fieldOf("spell").forGetter(SpellNode::getSpell),
                Codec.INT.fieldOf("level_cost").forGetter(SpellNode::getLevelCost),
                REQUIREMENT.listOf().fieldOf("requirements").forGetter(SpellNode::getRequirements),
                ExtraCodecs.lazyInitializedCodec(() -> SPELL_NODE).listOf().fieldOf("children").forGetter(SpellNode::getChildren)
        ).apply(instance, SpellNode::new)));
        
        SPELL_TREE_CONTENTS = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
                SPELL_NODE.fieldOf("root").forGetter(SpellTree::getRoot),
                Codec.STRING.xmap(Component::translatable, component -> ((TranslatableContents) component.getContents()).getKey()).fieldOf("title").forGetter(SpellTree::getTitle),
                SPELL.fieldOf("icon").forGetter(SpellTree::getIconSpell),
                REQUIREMENT.listOf().fieldOf("requirements").forGetter(SpellTree::getRequirements)
        ).apply(instance, SpellTree::new)));
        
    }
}
