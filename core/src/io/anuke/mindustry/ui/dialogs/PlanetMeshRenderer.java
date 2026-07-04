package io.anuke.mindustry.ui.dialogs;

import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.campaign.CampaignRegistry.PlanetDefinition;

public class PlanetMeshRenderer{
    public static class HoverData{
        public Sector sector;
        public float x, y;
        public boolean selected;
    }

    public void rebuildPlanetModels(PlanetDefinition planet){
    }

    public HoverData render(float x, float y, float width, float height, PlanetDefinition planet,
                            float rotLon, float rotLat, float zoom, Sector selected){
        return new HoverData();
    }

    public void dispose(){
    }
}
