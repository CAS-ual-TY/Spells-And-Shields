package de.cas_ual_ty.spells.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A data-driven player animation - one file, two independent {@link AnimationSection}s (first- and
 * third-person) that don't share tracks/timing with each other, only the file they're declared in.
 */
public class PlayerAnimation
{
    public static final Codec<PlayerAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AnimationSection.CODEC.fieldOf("first_person").forGetter(PlayerAnimation::getFirstPerson),
            AnimationSection.CODEC.fieldOf("third_person").forGetter(PlayerAnimation::getThirdPerson)
    ).apply(instance, PlayerAnimation::new));

    protected final AnimationSection firstPerson;
    protected final AnimationSection thirdPerson;

    public PlayerAnimation(AnimationSection firstPerson, AnimationSection thirdPerson)
    {
        this.firstPerson = firstPerson;
        this.thirdPerson = thirdPerson;
    }

    public AnimationSection getFirstPerson()
    {
        return firstPerson;
    }

    public AnimationSection getThirdPerson()
    {
        return thirdPerson;
    }
}
