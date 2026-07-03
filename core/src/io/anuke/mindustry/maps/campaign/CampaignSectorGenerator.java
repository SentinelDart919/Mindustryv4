package io.anuke.mindustry.maps.campaign;

import arc.struct.Seq;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.type.Item;

public interface CampaignSectorGenerator{
    Seq<Item> getOres(int x, int y, Seq<Item> defaultOres);
    void initSector(Sector sector);
}
