package io.anuke.mindustry.ui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.graphics.mesh.HexMesher;
import io.anuke.mindustry.graphics.mesh.MeshBuilder;
import io.anuke.mindustry.graphics.mesh.PlanetGrid;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.campaign.CampaignRegistry.PlanetDefinition;
import io.anuke.mindustry.maps.generation.WorldGenerator.GenResult;
import io.anuke.mindustry.world.ColorMapper;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.util.Mathf;

import io.anuke.mindustry.ui.dialogs.SectorsDialog;

import static io.anuke.mindustry.Vars.sectorSize;
import static io.anuke.mindustry.Vars.world;

public class PlanetMeshRenderer{ // All this class is a bullshit I hate java just using all the planet mesh will corrupt your game, crash your game, and more, so let's not touch more of this bullshit
    public static class HoverData{
        public Sector sector;
        public float x, y;
        public boolean selected;
    }

    private final ModelBatch modelBatch = new ModelBatch();
    private final Environment env = new Environment();
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final PerspectiveCamera cam = new PerspectiveCamera(55f, 1f, 1f);
    private final Vector3 camFromCenter = new Vector3();
    private final Vector3 tmpVec = new Vector3();
    private final HoverData hover = new HoverData();
    private final GenResult gen = new GenResult();
    private final LongMap<Vector3> cellBySector = new LongMap<>();
    private final LongMap<Integer> cellCountBySector = new LongMap<>();
    private final LongMap<Vector2> projectedBySector = new LongMap<>();
    private final Color tmpColor = new Color();

    private Model basePlanetModel;
    private ModelInstance basePlanet;
    private Mesh terrainMesh;
    private Mesh sectorGridMesh;
    private ShaderProgram terrainShader;
    private PlanetDefinition currentPlanet;

    public PlanetMeshRenderer(){
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.85f, 0.85f, 0.85f, 1f));
        terrainShader = new ShaderProgram(
            "attribute vec3 a_position;\n" +
            "attribute vec3 a_normal;\n" +
            "attribute vec4 a_color;\n" +
            "uniform mat4 u_proj;\n" +
            "varying vec3 v_normal;\n" +
            "varying vec4 v_color;\n" +
            "void main(){\n" +
            "  v_normal = normalize(a_normal);\n" +
            "  v_color = a_color;\n" +
            "  gl_Position = u_proj * vec4(a_position, 1.0);\n" +
            "}",
            "#ifdef GL_ES\nprecision mediump float;\n#endif\n" +
            "varying vec3 v_normal;\n" +
            "varying vec4 v_color;\n" +
            "uniform vec3 u_light;\n" +
            "uniform float u_alpha;\n" +
            "void main(){\n" +
            "  float lit = max(dot(normalize(v_normal), normalize(u_light)), 0.2);\n" +
            "  gl_FragColor = vec4(v_color.rgb * lit, v_color.a * u_alpha);\n" +
            "}"
        );
    }

    public void rebuildPlanetModels(PlanetDefinition planet){
        currentPlanet = planet;
        disposePlanet();
        if(planet == null) return;

        basePlanetModel = modelBuilder.createSphere(2f, 2f, 2f, 40, 28,
            new Material(ColorAttribute.createDiffuse(new Color(planet.colorR * 0.5f, planet.colorG * 0.5f, planet.colorB * 0.5f, 1f))),
            Usage.Position | Usage.Normal);
        basePlanet = new ModelInstance(basePlanetModel);

        buildSectorMap(planet);
        terrainMesh = MeshBuilder.buildPlanet(new HexMesher(){
            @Override
            public float getHeight(Vector3 position){
                return sampleHeight(position, planet);
            }

            @Override
            public void getColor(Vector3 position, Color out){
                out.set(sampleColor(position, planet));
            }
        }, Math.max(2, planet.subdivisions + 2), 1.2f, planet.meshHeightIntensity);

        sectorGridMesh = MeshBuilder.buildHex(new HexMesher(){
            @Override
            public float getHeight(Vector3 position){
                return 0f;
            }
            @Override
            public void getColor(Vector3 position, Color out){
                out.set(0.95f, 0.95f, 0.98f, 0.28f);
            }
        }, Math.max(0, planet.subdivisions), 1.23f, 0f);
    }

    public HoverData render(float x, float y, float width, float height, PlanetDefinition planet, float rotLon, float rotLat, float zoom, Sector selected){
        hover.sector = null;
        if(width <= 2f || height <= 2f || planet == null) return hover;
        if(currentPlanet != planet || terrainMesh == null || basePlanet == null){
            rebuildPlanetModels(planet);
        }
        if(terrainMesh == null || sectorGridMesh == null || basePlanet == null || !terrainShader.isCompiled()) return hover;

        float radius = 1.2f;
        float camDistance = Mathf.clamp(4.2f * zoom, 1.8f, 9f);
        float cy = MathUtils.cos(rotLat), sy = MathUtils.sin(rotLat);
        float cx = MathUtils.cos(rotLon), sx = MathUtils.sin(rotLon);
        cam.position.set(camDistance * cy * cx, camDistance * sy, camDistance * cy * sx);
        cam.up.set(0f, 1f, 0f);
        cam.lookAt(0f, 0f, 0f);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.viewportWidth = Gdx.graphics.getWidth();
        cam.viewportHeight = Gdx.graphics.getHeight();
        cam.update(true);
        camFromCenter.set(cam.position).nor();

        basePlanet.transform.idt().scale(radius, radius, radius);

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float best = Float.MAX_VALUE;
        float pickRadius = 26f + (1f - Mathf.clamp((zoom - 0.45f) / 1.85f, 0f, 1f)) * 14f;
        for(LongMap.Entry<Vector3> entry : cellBySector.entries()){
            int sxKey = (int)(entry.key >> 32);
            int syKey = (int)(entry.key);
            Sector sector = world.sectors.get(sxKey, syKey);
            if(sector == null || !SectorsDialog.isUnlockedStatic(sector)) continue;
            tmpVec.set(entry.value);
            if(tmpVec.dot(camFromCenter) <= 0f) continue;
            Vector3 projected = cam.project(new Vector3(tmpVec).scl(radius * 1.03f));
            float dst = Vector2.dst(mouseX, mouseY, projected.x, projected.y);
            if(dst < pickRadius && dst < best){
                best = dst;
                hover.sector = sector;
                hover.x = projected.x;
                hover.y = projected.y;
            }
        }
        hover.selected = hover.sector != null && hover.sector == selected;

        Graphics.end();
        try{
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

            modelBatch.begin(cam);
            modelBatch.render(basePlanet, env);
            modelBatch.end();
        }finally{
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            Graphics.begin();
        }

        projectedBySector.clear();
        for(LongMap.Entry<Vector3> entry : cellBySector.entries()){// Sectors previews
            int sxKey = (int)(entry.key >> 32);
            int syKey = (int)(entry.key);
            Sector sector = world.sectors.get(sxKey, syKey);
            if(sector == null) continue;

            boolean unlocked = SectorsDialog.isUnlockedStatic(sector);
            tmpVec.set(entry.value);
            if(tmpVec.dot(camFromCenter) <= 0f) continue;

            Vector3 projected = cam.project(new Vector3(tmpVec).scl(radius * 1.02f));
            float size = 32f / zoom;
            projectedBySector.put(entry.key, new Vector2(projected.x, projected.y));

            if(unlocked){
                if(sector.texture != null){
                    Draw.color(Color.WHITE);
                    Draw.rect(sector.texture, projected.x, projected.y, size, size);
                }else if(sector.complete){
                    Draw.color(Palette.accent);
                    Fill.poly(projected.x, projected.y, 4, size * 0.45f, 45f);
                }

                if(!sector.complete && sector.missions.size > 0){
                    float isize = size * 0.6f;
                    Draw.color(0f, 0f, 0f, 0.4f);
                    Fill.circle(projected.x, projected.y, isize / 2f + 2f);
                    Draw.color(Color.WHITE);
                    Draw.rect(sector.getDominantMission().getIcon(), projected.x, projected.y, isize - 1, isize - 1);
                }
                if(sector.hasSave()){
                    Draw.color(Palette.accent);
                    Draw.alpha(0.55f);
                    Draw.rect("sector-select", projected.x, projected.y, size + 8f, size + 8f);
                    Draw.alpha(1f);
                }
            }else{
                Draw.color(Color.GRAY);
                Draw.alpha(0.3f);
                Draw.rect("blank", projected.x, projected.y, size, size);
                Draw.alpha(1f);
            }
        }

        Lines.stroke(2f);// Draw lines connected to the sectors
        for(LongMap.Entry<Vector3> entry : cellBySector.entries()){
            int sxKey = (int)(entry.key >> 32);
            int syKey = (int)(entry.key);
            Sector sector = world.sectors.get(sxKey, syKey);
            if(sector == null || !sector.complete) continue;

            Vector2 v1 = projectedBySector.get(entry.key);
            if(v1 == null) continue;

            for(com.badlogic.gdx.math.GridPoint2 g : io.anuke.ucore.util.Geometry.d4){
                Sector other = world.sectors.get(sxKey + g.x, syKey + g.y);
                if(other == null || !SectorsDialog.isUnlockedStatic(other)) continue;
                
                Vector2 v2 = projectedBySector.get(key(other.x, other.y));
                if(v2 != null){
                    if(other.complete){
                        Draw.color(Color.GRAY);
                        Draw.alpha(0.2f);
                    }else{
                        Draw.color(Palette.accent);
                        Draw.alpha(0.5f);
                    }
                    Lines.line(v1.x, v1.y, v2.x, v2.y);
                }
            }
        }
        Lines.stroke(1f);
        Draw.alpha(1f);

        if(selected != null){
            Vector2 sel = projectedBySector.get(key(selected.x, selected.y));
            if(sel != null){
                Draw.color(Color.WHITE);
                Draw.alpha(0.95f);
                Draw.rect("sector-select", sel.x, sel.y, 44f, 44f);
                Draw.color(Palette.accent);
                Draw.alpha(0.85f);
                Draw.rect("sector-select", sel.x, sel.y, 50f, 50f);
                Draw.alpha(1f);
            }
        }else if(hover.sector != null){
            Draw.color(hover.selected ? Palette.accent : Color.WHITE);
            Draw.rect("sector-select", hover.x, hover.y, 34f, 34f);
        }
        Draw.reset();
        return hover;
    }

    private void buildSectorMap(PlanetDefinition planet){
        cellBySector.clear();
        cellCountBySector.clear();
        PlanetGrid grid = PlanetGrid.create(Math.max(0, planet.subdivisions));
        for(PlanetGrid.Cell cell : grid.cells){
            Vector3 p = cell.v.cpy().nor();
            float lon = MathUtils.atan2(p.z, p.x);
            float lat = (float)Math.asin(MathUtils.clamp(p.y, -1f, 1f));
            int sx = (int)(((lon + MathUtils.PI) / MathUtils.PI2) * planet.gridLongitude) - planet.gridLongitude / 2;
            int sy = (int)(((lat + MathUtils.PI / 2f) / MathUtils.PI) * planet.gridLatitude) - planet.gridLatitude / 2;

            long k = key(sx, sy);
            Vector3 acc = cellBySector.get(k);
            if(acc == null){
                acc = new Vector3();
                cellBySector.put(k, acc);
                cellCountBySector.put(k, 0);
            }
            acc.add(p);
            cellCountBySector.put(k, cellCountBySector.get(k, 0) + 1);
        }

        for(LongMap.Entry<Vector3> e : cellBySector.entries()){
            int count = cellCountBySector.get(e.key, 1);
            e.value.scl(1f / Math.max(1, count)).nor();
        }
    }

    private Color sampleColor(Vector3 position, PlanetDefinition planet){
        Vector3 n = new Vector3(position).nor();
        Vector3 tangent = Math.abs(n.y) < 0.99f ? new Vector3(0f, 1f, 0f).crs(n).nor() : new Vector3(1f, 0f, 0f).crs(n).nor();
        Vector3 bitangent = new Vector3(n).crs(tangent).nor();
        float ang = 0.05f;

        Color c0 = sampleRawColor(n, planet);
        Color c1 = sampleRawColor(new Vector3(n).mulAdd(tangent, ang).nor(), planet);
        Color c2 = sampleRawColor(new Vector3(n).mulAdd(tangent, -ang).nor(), planet);
        Color c3 = sampleRawColor(new Vector3(n).mulAdd(bitangent, ang).nor(), planet);
        Color c4 = sampleRawColor(new Vector3(n).mulAdd(bitangent, -ang).nor(), planet);

        tmpColor.set(c0).mul(0.40f);
        tmpColor.add(c1.r * 0.15f, c1.g * 0.15f, c1.b * 0.15f, 0f);
        tmpColor.add(c2.r * 0.15f, c2.g * 0.15f, c2.b * 0.15f, 0f);
        tmpColor.add(c3.r * 0.15f, c3.g * 0.15f, c3.b * 0.15f, 0f);
        tmpColor.add(c4.r * 0.15f, c4.g * 0.15f, c4.b * 0.15f, 0f);
        tmpColor.a = 1f;
        return tmpColor;
    }

    private float sampleHeight(Vector3 position, PlanetDefinition planet){
        Vector3 n = new Vector3(position).nor();
        Vector3 tangent = Math.abs(n.y) < 0.99f ? new Vector3(0f, 1f, 0f).crs(n).nor() : new Vector3(1f, 0f, 0f).crs(n).nor();
        Vector3 bitangent = new Vector3(n).crs(tangent).nor();

        float ang = 0.07f;
        float height = sampleRawHeight(n, planet) * 0.42f;
        height += sampleRawHeight(new Vector3(n).mulAdd(tangent, ang).nor(), planet) * 0.12f;
        height += sampleRawHeight(new Vector3(n).mulAdd(tangent, -ang).nor(), planet) * 0.12f;
        height += sampleRawHeight(new Vector3(n).mulAdd(bitangent, ang).nor(), planet) * 0.12f;
        height += sampleRawHeight(new Vector3(n).mulAdd(bitangent, -ang).nor(), planet) * 0.12f;
        height += sampleRawHeight(new Vector3(n).mulAdd(tangent, ang * 0.7f).mulAdd(bitangent, ang * 0.7f).nor(), planet) * 0.05f;
        height += sampleRawHeight(new Vector3(n).mulAdd(tangent, -ang * 0.7f).mulAdd(bitangent, -ang * 0.7f).nor(), planet) * 0.05f;
        return Mathf.clamp(height, -0.22f, 0.30f);
    }

    private float sampleRawHeight(Vector3 position, PlanetDefinition planet){
        mapAndSample(position, planet);
        return heightByFloor(gen.floor, planet);
    }

    private Color sampleRawColor(Vector3 position, PlanetDefinition planet){
        mapAndSample(position, planet);
        int terrainColor = ColorMapper.colorFor(gen.floor, gen.wall, io.anuke.mindustry.game.Team.none, gen.elevation, (byte)0);
        Color out = new Color();
        Color.rgba8888ToColor(out, terrainColor);
        return out;
    }
        //I hate java
    private float heightByFloor(Object floorObj, PlanetDefinition planet){
        if(!(floorObj instanceof Floor)) return 0f;
        Floor f = (Floor)floorObj;
        if(f.isLiquid) return -0.22f * planet.liquidDepthScale; // lowest

        String name = f.name == null ? "" : f.name.toLowerCase();
        if(name.contains("sand")) return -0.04f;               // level 1
        if(name.contains("grass") || name.contains("moss")) return 0.00f; // level 2
        if(name.contains("stone") && !name.contains("black")) return 0.08f; // level 3
        if(name.contains("black")) return 0.13f;               // level 4
        if(name.contains("ice")) return 0.18f;                 // high
        if(name.contains("snow")) return 0.24f * planet.snowHeightScale; // highest

        float e = smooth01((gen.elevation & 0xff) / 255f);
        return (e - 0.45f) * 0.12f;
    }

    private float smooth01(float x){
        x = Mathf.clamp(x, 0f, 1f);
        return x * x * (3f - 2f * x);
    }

    private void mapAndSample(Vector3 position, PlanetDefinition planet){
        float lon = MathUtils.atan2(position.z, position.x);
        float lat = (float)Math.asin(MathUtils.clamp(position.y, -1f, 1f));
        int sx = (int)(((lon + MathUtils.PI) / MathUtils.PI2) * planet.gridLongitude) - planet.gridLongitude / 2;
        int sy = (int)(((lat + MathUtils.PI / 2f) / MathUtils.PI) * planet.gridLatitude) - planet.gridLatitude / 2;
        int lx = Mathf.clamp((int)(((lon + MathUtils.PI) / MathUtils.PI2) * (sectorSize - 1)), 0, sectorSize - 1);
        int ly = Mathf.clamp((int)(((lat + MathUtils.PI / 2f) / MathUtils.PI) * (sectorSize - 1)), 0, sectorSize - 1);
        world.generator.generateTile(gen, sx, sy, lx, ly, false, null, null);
    }

    private long key(int x, int y){
        return (((long)x) << 32) | (y & 0xffffffffL);
    }

    private void disposePlanet(){
        if(basePlanetModel != null) basePlanetModel.dispose();
        if(terrainMesh != null) terrainMesh.dispose();
        if(sectorGridMesh != null) sectorGridMesh.dispose();
        basePlanetModel = null;
        basePlanet = null;
        terrainMesh = null;
        sectorGridMesh = null;
    }

    public void dispose(){
        disposePlanet();
        if(terrainShader != null){
            terrainShader.dispose();
            terrainShader = null;
        }
        modelBatch.dispose();
        cellBySector.clear();
        cellCountBySector.clear();
        currentPlanet = null;
    }
}
