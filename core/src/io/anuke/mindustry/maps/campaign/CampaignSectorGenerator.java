package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.type.Item;

public interface CampaignSectorGenerator{
    Array<Item> getOres(int x, int y, Array<Item> defaultOres);
    void initSector(Sector sector);
}
