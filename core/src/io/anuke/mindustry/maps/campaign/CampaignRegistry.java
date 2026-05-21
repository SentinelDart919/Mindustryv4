package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CampaignRegistry{
    public static final String serpulo = "Serpulo";
    public static final String openworld = "Open World";

    private static final ObjectMap<String, CampaignSectorGenerator> registered = new ObjectMap<>();
    private static final Array<PlanetDefinition> planets = new Array<>();

    static{
        register(serpulo, new SerpuloSectorGenerator());
        register(openworld, new OpenWorldSectorGenerator());
        planets.add(new PlanetDefinition("Serpulo", serpulo, 0.22f, 0.44f, 0.85f, 48, 24, 3, 0.95f, 1.15f, 0.9f));
        planets.add(new PlanetDefinition("Open World", openworld, 0.4f, 0.6f, 0.4f, 64, 32, 2, 1.0f, 1.0f, 1.0f));
    }

    public static void register(String name, CampaignSectorGenerator generator){
        registered.put(name, generator);
    }

    public static CampaignSectorGenerator generator(String name){
        CampaignSectorGenerator gen = registered.get(name);
        if(gen != null) return gen;
        return registered.get(serpulo);
    }

    public static Array<String> all(){
        Array<String> names = new Array<>();
        for(String key : registered.keys()){
            names.add(key);
        }
        return names;
    }

    public static class Definition{
        public final String name;
        public final CampaignSectorGenerator generator;

        public Definition(String name, CampaignSectorGenerator generator){
            this.name = name;
            this.generator = generator;
        }
    }

    public static Array<Definition> definitions(){
        Array<Definition> defs = new Array<>();
        for(ObjectMap.Entry<String, CampaignSectorGenerator> entry : registered.entries()){
            defs.add(new Definition(entry.key, entry.value));
        }
        return defs;
    }

    public static boolean contains(String name){
        return registered.containsKey(name);
    }

    public static Array<PlanetDefinition> planets(){
        return planets;
    }

    public static PlanetDefinition planetForCampaign(String campaignName){
        for(PlanetDefinition def : planets){
            if(def.campaign.equals(campaignName)){
                return def;
            }
        }
        return planets.size == 0 ? null : planets.first();
    }

    public static class PlanetDefinition{
        public final String name;
        public final String campaign;
        public final float colorR, colorG, colorB;
        public final int gridLongitude, gridLatitude;
        public final int subdivisions;
        public final float meshHeightIntensity;
        public final float liquidDepthScale;
        public final float snowHeightScale;

        public PlanetDefinition(String name, String campaign, float colorR, float colorG, float colorB, int gridLongitude, int gridLatitude, int subdivisions, float meshHeightIntensity, float liquidDepthScale, float snowHeightScale){
            this.name = name;
            this.campaign = campaign;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
            this.gridLongitude = gridLongitude;
            this.gridLatitude = gridLatitude;
            this.subdivisions = subdivisions;
            this.meshHeightIntensity = meshHeightIntensity;
            this.liquidDepthScale = liquidDepthScale;
            this.snowHeightScale = snowHeightScale;
        }
    }

    private CampaignRegistry(){}
}
