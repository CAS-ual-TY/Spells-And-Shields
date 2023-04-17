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
    TL_RIGHT(11);
    
    public final int id;
    
    SlotsPosition(int id)
    {
        this.id = id;
    }
    
    public boolean isVertical()
    {
        //0, 1, 2, 6, 7, 8
        return id % 6 < 3;
    }
    
    public boolean isHorizontal()
    {
        return !isVertical();
    }
    
    public boolean isLeft()
    {
        return id <= 3 || id == 11;
    }
    
    public boolean isBottom()
    {
        return id >= 2 && id <= 6;
    }
    
    public boolean isRight()
    {
        return id >= 5 && id <= 9;
    }
    
    public boolean isTop()
    {
        return id == 0 || id >= 8;
    }
    
    public boolean downwards()
    {
        return id == 0 || id == 8;
    }
    
    public boolean upwards()
    {
        return id == 2 || id == 6;
    }
    
    public boolean rightwards()
    {
        return id == 3 || id == 11;
    }
    
    public boolean leftwards()
    {
        return id == 5 || id == 9;
    }
    
    public boolean centered()
    {
        return id % 3 == 1;
    }
    
    public int startPositionX(int guiLeft, int guiTop, int guiWidth, int guiHeight, int frameWidth, int frameHeight, int frames)
    {
        if(isVertical())
        {
            if(isLeft())
            {
                return guiLeft - frameWidth;
            }
            else if(isRight())
            {
                return guiLeft + guiWidth;
            }
        }
        else
        {
            if(isLeft())
            {
                return guiLeft;
            }
            else if(isRight())
            {
                return guiLeft + guiWidth - frames * frameWidth;
            }
            else
            {
                return guiLeft + (guiWidth - frames * frameWidth) / 2;
            }
        }
        
        return -1;
    }
    
    public int startPositionY(int guiLeft, int guiTop, int guiWidth, int guiHeight, int frameWidth, int frameHeight, int frames)
    {
        if(isHorizontal())
        {
            if(isTop())
            {
                return guiTop - frameHeight;
            }
            else if(isBottom())
            {
                return guiTop + guiHeight;
            }
        }
        else
        {
            if(isTop())
            {
                return guiTop;
            }
            else if(isBottom())
            {
                return guiTop + guiHeight - frames * frameHeight;
            }
            else
            {
                return guiTop + (guiHeight - frames * frameHeight) / 2;
            }
        }
        
        return -1;
    }
    
    public int incrementX(int frameWidth, int frameHeight)
    {
        return isHorizontal() ? frameWidth : 0;
    }
    
    public int incrementY(int frameWidth, int frameHeight)
    {
        return isVertical() ? frameHeight : 0;
    }
    
    public static SlotsPosition fromId(int id)
    {
        return values()[id];
    }
}
