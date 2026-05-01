package io.anuke.mindustry.maps.campaign;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CampaignRegistry{
    public static final String serpulo = "Serpulo";

    private static final ObjectMap<String, CampaignSectorGenerator> registered = new ObjectMap<>();
    private static final Array<PlanetDefinition> planets = new Array<>();

    static{
        register(serpulo, new SerpuloSectorGenerator());
        planets.add(new PlanetDefinition("Serpulo", serpulo, 0.22f, 0.44f, 0.85f, 24, 12, 2));
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

        public PlanetDefinition(String name, String campaign, float colorR, float colorG, float colorB, int gridLongitude, int gridLatitude, int subdivisions){
            this.name = name;
            this.campaign = campaign;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
            this.gridLongitude = gridLongitude;
            this.gridLatitude = gridLatitude;
            this.subdivisions = subdivisions;
        }
    }

    private CampaignRegistry(){}
}
