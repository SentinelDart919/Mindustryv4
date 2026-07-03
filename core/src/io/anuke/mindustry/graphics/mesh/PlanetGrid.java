package io.anuke.mindustry.graphics.mesh;

import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.struct.LongMap;
import arc.math.geom.Vector3;

/** Simplified geodesic cell grid for planet meshes. */
public class PlanetGrid{
    public static class Cell{
        public final int id;
        public final Vector3 v;

        public Cell(int id, Vector3 v){
            this.id = id;
            this.v = v;
        }
    }

    public final int subdivisions;
    public final Cell[] cells;

    private PlanetGrid(int subdivisions, Cell[] cells){
        this.subdivisions = subdivisions;
        this.cells = cells;
    }

    public static PlanetGrid create(int subdivisions){
        Seq<Vector3> vertices = buildGeodesicVertices(Math.max(0, subdivisions));
        Cell[] out = new Cell[vertices.size];
        for(int i = 0; i < vertices.size; i++){
            out[i] = new Cell(i, vertices.get(i).cpy().nor());
        }
        return new PlanetGrid(subdivisions, out);
    }

    private static Seq<Vector3> buildGeodesicVertices(int subdivisions){
        Seq<Vector3> vertices = new Seq<>();
        Seq<int[]> faces = new Seq<>();

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
            Seq<int[]> newFaces = new Seq<>();
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

    private static int midpoint(Seq<Vector3> verts, LongMap<Integer> cache, int i1, int i2){
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

    private static void addVertex(Seq<Vector3> verts, float x, float y, float z){
        verts.add(new Vector3(x, y, z).nor());
    }
}

