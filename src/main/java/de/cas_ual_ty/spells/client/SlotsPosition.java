package de.cas_ual_ty.spells.client;

public enum SlotsPosition
{
    TL_DOWN(0),
    L(1),
    BL_UP(2),
    BL_RIGHT(3),
    B(4),
    BR_LEFT(5),
    BR_UP(6),
    R(7),
    TR_DOWN(8),
    TR_LEFT(9),
    T(10),
    TL_RIGHT(11),
    EDGE_TL_DOWN(12),
    EDGE_L(13),
    EDGE_BL_UP(14),
    EDGE_BL_RIGHT(15),
    EDGE_B(16),
    EDGE_BR_LEFT(17),
    EDGE_BR_UP(18),
    EDGE_R(19),
    EDGE_TR_DOWN(20),
    EDGE_TR_LEFT(21),
    EDGE_T(22),
    EDGE_TL_RIGHT(23);
    
    public static final int GROUP_AMT = 12;
    
    public final int id;
    
    public final boolean atScreenEdge;
    public final boolean isHorizontal;
    public final boolean isVertical;
    public final boolean isLeft;
    public final boolean isRight;
    public final boolean isTop;
    public final boolean isBottom;
    public final boolean isDownwards;
    public final boolean isUpwards;
    public final boolean isRightwards;
    public final boolean isLeftwards;
    public final boolean isCentered;
    
    SlotsPosition(int id)
    {
        this.id = id;
        
        atScreenEdge = id >= GROUP_AMT;
        id = id % GROUP_AMT;
        
        isVertical = id % 6 < 3;
        isHorizontal = !isVertical;
        isLeft = id <= 3 || id == 11;
        isRight = id >= 5 && id <= 9;
        isTop = id == 0 || id >= 8;
        isBottom = id >= 2 && id <= 6;
        isDownwards = id == 0 || id == 8;
        isUpwards = id == 2 || id == 6;
        isRightwards = id == 3 || id == 11;
        isLeftwards = id == 5 || id == 9;
        isCentered = id % 3 == 1;
    }
    
    public SlotsPosition transform()
    {
        if(id >= GROUP_AMT)
        {
            return fromId(id % GROUP_AMT);
        }
        else
        {
            return fromId(id + GROUP_AMT);
        }
    }
    
    public int startPositionX(int screenWidth, int screenHeight, int guiLeft, int guiTop, int guiWidth, int guiHeight, int frameWidth, int frameHeight, int frames, int margin)
    {
        if(atScreenEdge)
        {
            if(isVertical)
            {
                if(isLeft)
                {
                    return margin;
                }
                else if(isRight)
                {
                    return screenWidth - (frameWidth + margin);
                }
            }
            else
            {
                if(isLeft)
                {
                    return margin;
                }
                else if(isRight)
                {
                    return screenWidth - frames * (frameWidth + margin);
                }
                else
                {
                    return (screenWidth - frames * (frameWidth + margin) + margin) / 2;
                }
            }
        }
        else
        {
            if(isVertical)
            {
                if(isLeft)
                {
                    return guiLeft - (frameWidth + margin);
                }
                else if(isRight)
                {
                    return guiLeft + guiWidth + margin;
                }
            }
            else
            {
                if(isLeft)
                {
                    return guiLeft;
                }
                else if(isRight)
                {
                    return guiLeft + guiWidth - frames * (frameWidth + margin) + margin;
                }
                else
                {
                    return guiLeft + (guiWidth - frames * (frameWidth + margin) + margin) / 2;
                }
            }
        }
        
        return -1;
    }
    
    public int startPositionY(int screenWidth, int screenHeight, int guiLeft, int guiTop, int guiWidth, int guiHeight, int frameWidth, int frameHeight, int frames, int margin)
    {
        if(atScreenEdge)
        {
            if(isHorizontal)
            {
                if(isTop)
                {
                    return margin;
                }
                else if(isBottom)
                {
                    return screenHeight - (frameHeight + margin);
                }
            }
            else
            {
                if(isTop)
                {
                    return margin;
                }
                else if(isBottom)
                {
                    return screenHeight - frames * (frameHeight + margin);
                }
                else
                {
                    return (screenHeight - frames * (frameHeight + margin) + margin) / 2;
                }
            }
        }
        else
        {
            if(isHorizontal)
            {
                if(isTop)
                {
                    return guiTop - (frameHeight + margin);
                }
                else if(isBottom)
                {
                    return guiTop + guiHeight + margin;
                }
            }
            else
            {
                if(isTop)
                {
                    return guiTop;
                }
                else if(isBottom)
                {
                    return guiTop + guiHeight - frames * (frameHeight + margin) + margin;
                }
                else
                {
                    return guiTop + (guiHeight - frames * (frameHeight + margin) + margin) / 2;
                }
            }
        }
        
        return -1;
    }
    
    public int incrementX(int frameWidth, int frameHeight, int margin)
    {
        return isHorizontal ? frameWidth + margin : 0;
    }
    
    public int incrementY(int frameWidth, int frameHeight, int margin)
    {
        return isVertical ? frameHeight + margin : 0;
    }
    
    public static SlotsPosition fromId(int id)
    {
        return values()[id];
    }
}
