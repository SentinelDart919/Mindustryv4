package io.anuke.mindustry.graphics.mesh;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;

// this is just a Copy paste of the ARC meshBuilder. for now useless
public class MeshBuilder{
    /** Builds a smooth subdivided icosphere planet mesh with per-vertex height + color. */
    public static Mesh buildPlanet(HexMesher mesher, int divisions, float radius, float intensity){
        Array<Vector3> verts = new Array<>();
        Array<short[]> faces = new Array<>();
        buildIcosphere(Math.max(0, divisions), verts, faces);

        int maxVerts = Math.min(verts.size, 32767);
        float[] vertices = new float[maxVerts * (3 + 3 + 4)];
        Color color = new Color();
        int vptr = 0;

        for(int i = 0; i < maxVerts; i++){
            Vector3 n = new Vector3(verts.get(i)).nor();
            if(mesher.skip(n)){
                n.setZero();
            }
            mesher.getColor(n, color);
            float h = radius * (1f + mesher.getHeight(n) * intensity);
            Vector3 p = new Vector3(n).scl(h);
            vptr = putVertex(vertices, vptr, p, n, color);
        }

        int iptr = 0;
        for(short[] f : faces){
            int i0 = f[0] & 0xffff, i1 = f[1] & 0xffff, i2 = f[2] & 0xffff;
            if(i0 >= maxVerts || i1 >= maxVerts || i2 >= maxVerts) continue;
            iptr += 3;
        }

        short[] outIndices = new short[iptr];
        int curIdx = 0;
        for(short[] f : faces){
            int i0 = f[0] & 0xffff, i1 = f[1] & 0xffff, i2 = f[2] & 0xffff;
            if(i0 >= maxVerts || i1 >= maxVerts || i2 >= maxVerts) continue;

            Vector3 a = verts.get(i0), b = verts.get(i1), c = verts.get(i2);
            // enforce consistent outward winding to avoid missing faces with culling
            float winding = new Vector3(b).sub(a).crs(new Vector3(c).sub(a)).dot(a);
            if(winding < 0f){
                int t = i1;
                i1 = i2;
                i2 = t;
            }
            outIndices[curIdx++] = (short)i0;
            outIndices[curIdx++] = (short)i1;
            outIndices[curIdx++] = (short)i2;
        }

        float[] outVerts = new float[vptr];
        System.arraycopy(vertices, 0, outVerts, 0, vptr);

        Mesh mesh = new Mesh(true, maxVerts, iptr,
            new VertexAttribute(Usage.Position, 3, "a_position"),
            new VertexAttribute(Usage.Normal, 3, "a_normal"),
            new VertexAttribute(Usage.ColorUnpacked, 4, "a_color")
        );
        mesh.setVertices(outVerts);
        mesh.setIndices(outIndices);
        return mesh;
    }

    /** Builds a colored geodesic patch mesh around PlanetGrid cells. */
    public static Mesh buildHex(HexMesher mesher, int divisions, float radius, float intensity){
        PlanetGrid grid = PlanetGrid.create(divisions);
        int vertsPerCell = 1 + 6;
        int indicesPerCell = 6 * 3;

        float[] vertices = new float[grid.cells.length * vertsPerCell * (3 + 3 + 4)];
        short[] indices = new short[grid.cells.length * indicesPerCell];
        Color color = new Color();

        int vptr = 0;
        int iptr = 0;
        short base = 0;

        for(PlanetGrid.Cell cell : grid.cells){
            Vector3 n = new Vector3(cell.v).nor();
            if(mesher.skip(n)) continue;

            mesher.getColor(n, color);
            float h = radius * (1f + mesher.getHeight(n) * intensity);
            Vector3 center = new Vector3(n).scl(h);

            // center vertex
            vptr = putVertex(vertices, vptr, center, n, color);

            Vector3 tangent = orthogonal(n).nor();
            Vector3 bitangent = new Vector3(n).crs(tangent).nor();
            float ring = Math.max(0.02f, (MathUtils.PI2 * h) / Math.max(36f, grid.cells.length / 8f));

            short centerIdx = base;
            for(int i = 0; i < 6; i++){
                float a = MathUtils.PI2 * i / 6f;
                Vector3 off = new Vector3(tangent).scl(MathUtils.cos(a) * ring).add(new Vector3(bitangent).scl(MathUtils.sin(a) * ring));
                Vector3 p = new Vector3(center).add(off).nor().scl(h);
                vptr = putVertex(vertices, vptr, p, n, color);
            }

            for(int i = 0; i < 6; i++){
                short i1 = (short)(centerIdx + 1 + i);
                short i2 = (short)(centerIdx + 1 + ((i + 1) % 6));
                indices[iptr++] = centerIdx;
                indices[iptr++] = i1;
                indices[iptr++] = i2;
            }

            base += vertsPerCell;
            if(base > 65000) break;
        }

        Mesh mesh = new Mesh(true, base, iptr,
            new VertexAttribute(Usage.Position, 3, "a_position"),
            new VertexAttribute(Usage.Normal, 3, "a_normal"),
            new VertexAttribute(Usage.ColorUnpacked, 4, "a_color")
        );

        // trim arrays to written size
        float[] outVerts = new float[vptr];
        System.arraycopy(vertices, 0, outVerts, 0, vptr);
        short[] outIndices = new short[iptr];
        System.arraycopy(indices, 0, outIndices, 0, iptr);

        mesh.setVertices(outVerts);
        mesh.setIndices(outIndices);
        return mesh;
    }

    public static Mesh buildHex(Color color, int divisions, float radius){
        return buildHex(new HexMesher(){
            @Override
            public void getColor(Vector3 position, Color out){
                out.set(color);
            }
        }, divisions, radius, 0f);
    }

    private static int putVertex(float[] out, int p, Vector3 pos, Vector3 nor, Color c){
        out[p++] = pos.x;
        out[p++] = pos.y;
        out[p++] = pos.z;
        out[p++] = nor.x;
        out[p++] = nor.y;
        out[p++] = nor.z;
        out[p++] = c.r;
        out[p++] = c.g;
        out[p++] = c.b;
        out[p++] = c.a;
        return p;
    }

    private static Vector3 orthogonal(Vector3 n){
        return Math.abs(n.y) < 0.99f ? new Vector3(0, 1, 0).crs(n) : new Vector3(1, 0, 0).crs(n);
    }

    private static void buildIcosphere(int subdivisions, Array<Vector3> vertices, Array<short[]> faces){
        float t = (1f + (float)Math.sqrt(5f)) / 2f;
        addVertex(vertices, -1, t, 0); addVertex(vertices, 1, t, 0); addVertex(vertices, -1, -t, 0); addVertex(vertices, 1, -t, 0);
        addVertex(vertices, 0, -1, t); addVertex(vertices, 0, 1, t); addVertex(vertices, 0, -1, -t); addVertex(vertices, 0, 1, -t);
        addVertex(vertices, t, 0, -1); addVertex(vertices, t, 0, 1); addVertex(vertices, -t, 0, -1); addVertex(vertices, -t, 0, 1);

        short[][] base = {
            {0,11,5},{0,5,1},{0,1,7},{0,7,10},{0,10,11},
            {1,5,9},{5,11,4},{11,10,2},{10,7,6},{7,1,8},
            {3,9,4},{3,4,2},{3,2,6},{3,6,8},{3,8,9},
            {4,9,5},{2,4,11},{6,2,10},{8,6,7},{9,8,1}
        };
        for(short[] f : base) faces.add(new short[]{f[0], f[1], f[2]});

        for(int s = 0; s < subdivisions; s++){
            LongMap<Short> cache = new LongMap<>();
            Array<short[]> newFaces = new Array<>();
            for(short[] f : faces){
                short a = midpoint(vertices, cache, f[0], f[1]);
                short b = midpoint(vertices, cache, f[1], f[2]);
                short c = midpoint(vertices, cache, f[2], f[0]);
                newFaces.add(new short[]{f[0], a, c});
                newFaces.add(new short[]{f[1], b, a});
                newFaces.add(new short[]{f[2], c, b});
                newFaces.add(new short[]{a, b, c});
            }
            faces.clear();
            faces.addAll(newFaces);
        }
    }

    private static short midpoint(Array<Vector3> verts, LongMap<Short> cache, short i1, short i2){
        int a = Math.min(i1, i2), b = Math.max(i1, i2);
        long key = (((long)a) << 32) | (b & 0xffffffffL);
        Short idx = cache.get(key);
        if(idx != null) return idx;
        short out = (short)verts.size;
        verts.add(new Vector3(verts.get(i1)).add(verts.get(i2)).scl(0.5f).nor());
        cache.put(key, out);
        return out;
    }

    private static void addVertex(Array<Vector3> verts, float x, float y, float z){
        verts.add(new Vector3(x, y, z).nor());
    }
}
