package io.anuke.mindustry.ui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.generation.WorldGenerator.GenResult;
import io.anuke.mindustry.maps.campaign.CampaignRegistry.PlanetDefinition;
import io.anuke.mindustry.world.ColorMapper;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.sectorSize;
import static io.anuke.mindustry.Vars.world;

public class PlanetMeshRenderer{
    public static class HoverData{
        public Sector sector;
        public float x, y;
        public boolean selected;
    }

    private final ModelBatch modelBatch = new ModelBatch();
    private final Environment env = new Environment();
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final PerspectiveCamera cam = new PerspectiveCamera(55f, 1f, 1f);
    private final Array<ModelInstance> markerInstances = new Array<>();
    private final Vector3 tmpVec = new Vector3();
    private final Vector3 tmpNormal = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Quaternion patchRot = new Quaternion();
    private final Matrix4 rotationMatrix = new Matrix4();
    private final Vector3 camFromCenter = new Vector3();
    private final HoverData hover = new HoverData();
    private final Color tmpColor = new Color();
    private final GenResult gen = new GenResult();
    private final Array<Cell> cells = new Array<>();
    private final LongMap<Vector3> cellBySector = new LongMap<>();

    private Model planetModel, gridModel;
    private Model patchCompleteModel, patchIncompleteModel, patchSelectedModel, patchHoverModel;
    private ModelInstance planetInstance, gridInstance;
    private PlanetDefinition currentPlanet;

    private static class Cell{
        Vector3 dir = new Vector3();
        int sectorX, sectorY;
        int localX, localY;
    }

    public PlanetMeshRenderer(){
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
    }

    public void rebuildPlanetModels(PlanetDefinition planet){
        currentPlanet = planet;
        disposeModels();
        rebuildCells(planet);
        if(planet == null) return;

        planetModel = modelBuilder.createSphere(2f, 2f, 2f, 56, 36,
            new Material(ColorAttribute.createDiffuse(new Color(planet.colorR, planet.colorG, planet.colorB, 1f))),
            Usage.Position | Usage.Normal);
        gridModel = modelBuilder.createSphere(2f, 2f, 2f, Math.max(planet.gridLongitude, 16), Math.max(planet.gridLatitude, 12),
            new Material(
                ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 0.15f)),
                new BlendingAttribute(true, 0.15f)
            ),
            Usage.Position | Usage.Normal);

        patchCompleteModel = modelBuilder.createCylinder(1f, 0.05f, 1f, 6,
            new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.95f, 0.45f, 1f))), Usage.Position | Usage.Normal);
        patchIncompleteModel = modelBuilder.createCylinder(1f, 0.05f, 1f, 6,
            new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.78f, 0.3f, 1f))), Usage.Position | Usage.Normal);
        patchSelectedModel = modelBuilder.createCylinder(1f, 0.06f, 1f, 6,
            new Material(ColorAttribute.createDiffuse(new Color(Palette.accent))), Usage.Position | Usage.Normal);
        patchHoverModel = modelBuilder.createCylinder(1f, 0.06f, 1f, 6,
            new Material(ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1f))), Usage.Position | Usage.Normal);

        planetInstance = new ModelInstance(planetModel);
        gridInstance = new ModelInstance(gridModel);
    }

    public HoverData render(float x, float y, float width, float height, PlanetDefinition planet, float rotLon, float rotLat, float zoom, Sector selected){
        hover.sector = null;
        if(width <= 2f || height <= 2f) return hover;
        if(planet == null) return hover;
        if(currentPlanet != planet || planetModel == null || gridModel == null){
            rebuildPlanetModels(planet);
        }
        if(planetModel == null || gridModel == null || planetInstance == null || gridInstance == null) return hover;

        float radius = 1.2f;
        float camDistance = Mathf.clamp(4.2f / zoom, 1.8f, 9f);
        rotation.idt()
            .setFromAxisRad(0f, 1f, 0f, 0f)
            .mul(new Quaternion().setFromAxisRad(1f, 0f, 0f, 0f));
        rotationMatrix.idt().rotate(rotation);

        cam.viewportWidth = Gdx.graphics.getWidth();
        cam.viewportHeight = Gdx.graphics.getHeight();
        float cy = MathUtils.cos(rotLat);
        float sy = MathUtils.sin(rotLat);
        float cx = MathUtils.cos(rotLon);
        float sx = MathUtils.sin(rotLon);
        cam.position.set(camDistance * cy * cx, camDistance * sy, camDistance * cy * sx);
        cam.lookAt(0f, 0f, 0f);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update(true);

        planetInstance.transform.idt().scale(radius, radius, radius);
        gridInstance.transform.idt().scale(radius * 1.01f, radius * 1.01f, radius * 1.01f);
        markerInstances.clear();
        camFromCenter.set(cam.position).nor();

        Vector2 mouse = Graphics.mouse();
        float bestDst = Float.MAX_VALUE;
        float cellScale = Mathf.clamp(radius * 2.2f / Mathf.sqrt(Math.max(1, cells.size)), 0.06f, 0.18f);

        for(Cell cell : cells){
            tmpVec.set(cell.dir);
            if(tmpVec.dot(camFromCenter) <= 0f) continue;

            Sector sector = world.sectors.get(cell.sectorX, cell.sectorY);
            if(sector == null){
                world.sectors.createSector(cell.sectorX, cell.sectorY);
                sector = world.sectors.get(cell.sectorX, cell.sectorY);
            }
            if(sector == null) continue;

            world.generator.generateTile(gen, cell.sectorX, cell.sectorY, cell.localX, cell.localY, false, null, null);
                int terrainColor = ColorMapper.colorFor(gen.floor, gen.wall, io.anuke.mindustry.game.Team.none, gen.elevation, (byte)0);
                Color.rgba8888ToColor(tmpColor, terrainColor);

                Vector3 projected = cam.project(new Vector3(tmpVec).scl(radius * 1.01f));
                float drawX = projected.x, drawY = projected.y;
                float cellSize = Math.max(10f, 18f * zoom * tmpVec.z);

                ModelInstance marker = new ModelInstance(sector.complete ? patchCompleteModel : patchIncompleteModel);
                marker.materials.get(0).set(ColorAttribute.createDiffuse(tmpColor));
                tmpNormal.set(tmpVec).nor();
                patchRot.setFromCross(Vector3.Y, tmpNormal);
                float elevationOffset = terrainElevationOffset(gen);
                marker.transform.idt().setToTranslation(new Vector3(tmpNormal).scl(radius * 1.02f)).rotate(patchRot)
                    .translate(0f, elevationOffset, 0f)
                    .scale(cellScale, 0.010f + elevationOffset * 0.03f, cellScale);
                markerInstances.add(marker);

                float dst = Vector2.dst(mouse.x, mouse.y, drawX, drawY);
                if(dst < Math.max(12f, cellSize * 0.8f) && dst < bestDst){
                    bestDst = dst;
                    hover.sector = sector;
                    hover.x = drawX;
                    hover.y = drawY;
                }
        }

        if(hover.sector != null){
            hover.selected = (selected == hover.sector);
            Vector3 hoveredWorld = sectorWorldPosition(hover.sector, radius);
            if(hoveredWorld != null){
                tmpNormal.set(hoveredWorld).nor();
                patchRot.setFromCross(Vector3.Y, tmpNormal);
                ModelInstance hoverMarker = new ModelInstance(hover.selected ? patchSelectedModel : patchHoverModel);
                float hoverScale = cellScale * 1.2f;
                hoverMarker.transform.idt().setToTranslation(hoveredWorld).rotate(patchRot).scale(hoverScale, 0.014f, hoverScale);
                markerInstances.add(hoverMarker);
            }
        }

        Graphics.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        modelBatch.begin(cam);
        modelBatch.render(planetInstance, env);
        for(ModelInstance marker : markerInstances){
            modelBatch.render(marker, env);
        }
        modelBatch.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Graphics.begin();

        if(hover.sector != null){
            Draw.color(hover.selected ? Palette.accent : Color.WHITE);
            Draw.rect("sector-select", hover.x, hover.y, 34f, 34f);
        }
        Draw.reset();
        return hover;
    }

    private Vector3 sectorWorldPosition(Sector sector, float radius){
        Vector3 dir = cellBySector.get(sectorKey(sector.x, sector.y));
        if(dir == null) return null;
        Vector3 v = new Vector3(dir);
        return v.scl(radius * 1.05f);
    }

    private void rebuildCells(PlanetDefinition planet){
        cells.clear();
        cellBySector.clear();
        if(planet == null) return;

        Array<Vector3> points = buildGeodesicVertices(Math.max(0, planet.subdivisions));
        int span = Math.max(1, Mathf.ceil(Mathf.sqrt(points.size)));

        for(int i = 0; i < points.size; i++){
            Vector3 p = points.get(i).nor();
            Cell cell = new Cell();
            cell.dir.set(p);
            cell.sectorX = i % span - span / 2;
            cell.sectorY = i / span - span / 2;

            float lon = MathUtils.atan2(p.z, p.x);
            float lat = (float)Math.asin(p.y);
            cell.localX = Mathf.clamp((int)(((lon + MathUtils.PI) / MathUtils.PI2) * (sectorSize - 1)), 0, sectorSize - 1);
            cell.localY = Mathf.clamp((int)(((lat + MathUtils.PI / 2f) / MathUtils.PI) * (sectorSize - 1)), 0, sectorSize - 1);

            cells.add(cell);
            cellBySector.put(sectorKey(cell.sectorX, cell.sectorY), new Vector3(cell.dir));
        }
    }

    private Array<Vector3> buildGeodesicVertices(int subdivisions){
        Array<Vector3> vertices = new Array<>();
        Array<int[]> faces = new Array<>();

        float t = (1f + (float)Math.sqrt(5f)) / 2f;
        addVertex(vertices, -1, t, 0); addVertex(vertices, 1, t, 0); addVertex(vertices, -1, -t, 0); addVertex(vertices, 1, -t, 0);
        addVertex(vertices, 0, -1, t); addVertex(vertices, 0, 1, t); addVertex(vertices, 0, -1, -t); addVertex(vertices, 0, 1, -t);
        addVertex(vertices, t, 0, -1); addVertex(vertices, t, 0, 1); addVertex(vertices, -t, 0, -1); addVertex(vertices, -t, 0, 1);

        int[][] base = {
            {0,11,5},{0,5,1},{0,1,7},{0,7,10},{0,10,11},
            {1,5,9},{5,11,4},{11,10,2},{10,7,6},{7,1,8},
            {3,9,4},{3,4,2},{3,2,6},{3,6,8},{3,8,9},
            {4,9,5},{2,4,11},{6,2,10},{8,6,7},{9,8,1}
        };
        for(int[] f : base) faces.add(f);

        for(int s = 0; s < subdivisions; s++){
            LongMap<Integer> cache = new LongMap<>();
            Array<int[]> newFaces = new Array<>();
            for(int[] f : faces){
                int a = midpoint(vertices, cache, f[0], f[1]);
                int b = midpoint(vertices, cache, f[1], f[2]);
                int c = midpoint(vertices, cache, f[2], f[0]);
                newFaces.add(new int[]{f[0], a, c});
                newFaces.add(new int[]{f[1], b, a});
                newFaces.add(new int[]{f[2], c, b});
                newFaces.add(new int[]{a, b, c});
            }
            faces = newFaces;
        }

        for(Vector3 v : vertices){
            v.nor();
        }
        return vertices;
    }

    private int midpoint(Array<Vector3> verts, LongMap<Integer> cache, int i1, int i2){
        int a = Math.min(i1, i2), b = Math.max(i1, i2);
        long key = (((long)a) << 32) | (b & 0xffffffffL);
        Integer idx = cache.get(key);
        if(idx != null) return idx;
        Vector3 v = new Vector3(verts.get(i1)).add(verts.get(i2)).scl(0.5f).nor();
        int out = verts.size;
        verts.add(v);
        cache.put(key, out);
        return out;
    }

    private void addVertex(Array<Vector3> verts, float x, float y, float z){
        verts.add(new Vector3(x, y, z).nor());
    }

    private long sectorKey(int x, int y){
        return (((long)x) << 32) | (y & 0xffffffffL);
    }

    private float terrainElevationOffset(GenResult result){
        float normalized = (result.elevation & 0xff) / 255f;
        float offset = normalized * 0.12f - 0.02f;

        if(result.floor instanceof Floor){
            Floor floor = (Floor)result.floor;
            if(floor.isLiquid){
                offset -= 0.07f;
            }
            String name = floor.name == null ? "" : floor.name.toLowerCase();
            if(name.contains("snow")){
                offset += 0.08f;
            }
        }
        return Mathf.clamp(offset, -0.08f, 0.12f);
    }

    private void disposeModels(){
        if(planetModel != null) planetModel.dispose();
        if(gridModel != null) gridModel.dispose();
        if(patchCompleteModel != null) patchCompleteModel.dispose();
        if(patchIncompleteModel != null) patchIncompleteModel.dispose();
        if(patchSelectedModel != null) patchSelectedModel.dispose();
        if(patchHoverModel != null) patchHoverModel.dispose();
        planetModel = null;
        gridModel = null;
        patchCompleteModel = null;
        patchIncompleteModel = null;
        patchSelectedModel = null;
        patchHoverModel = null;
    }
}
