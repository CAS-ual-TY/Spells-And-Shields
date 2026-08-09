package de.cas_ual_ty.spells.animation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EaseType implements StringRepresentable
{
    NONE("none"),
    IN("in"),
    OUT("out"),
    BOTH("both");

    public static final Codec<EaseType> CODEC = StringRepresentable.fromEnum(EaseType::values);

    private final String name;

    EaseType(String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    /**
     * Applies this easing curve to a linear progress value {@code t} (already clamped to {@code [0, 1]}).
     */
    public float apply(float t)
    {
        return switch(this)
        {
            case NONE -> t;
            case IN -> t * t;
            case OUT -> 1F - (1F - t) * (1F - t);
            case BOTH -> t < 0.5F ? 2F * t * t : 1F - (float) Math.pow(-2F * t + 2F, 2) / 2F;
        };
    }
}
