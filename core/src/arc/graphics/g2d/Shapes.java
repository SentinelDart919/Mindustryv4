package arc.graphics.g2d;
import arc.Core;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;

public class Shapes{
    private static Draw draw = new Draw();

    public static void laser(String line, String edge, float x, float y, float x2, float y2){
        laser(line, edge, x, y, x2, y2, 12f);
    }

    public static void laser(String line, String edge, float x, float y, float x2, float y2, float thickness){
        TextureRegion lineRegion = Core.atlas.find(line);
        TextureRegion edgeRegion = Core.atlas.find(edge);

        float angle = Angles.angle(x, y, x2, y2);
        float dst = Mathf.dst(x, y, x2, y2);

        Draw.rect(lineRegion, x + Angles.trnsx(angle, dst/2f), y + Angles.trnsy(angle, dst/2f), dst, thickness, angle);
        Draw.rect(edgeRegion, x, y, thickness, thickness, angle + 180);
        Draw.rect(edgeRegion, x2, y2, thickness, thickness, angle);
    }

    public static void draw(Batch batch){
    }

    public static void rect(int x, int y, int width, int height){
    }

    public static void rect(int x, int y, int width, int height, float angle){
    }

    public static void filledRect(int x, int y, int width, int height){
    }

    public static void line(float x1, float y1, float x2, float y2){
    }

    public static void circle(float x, float y, float radius){
    }

    public static void filledCircle(float x, float y, float radius){
    }

    public static void poly(float[] vertices){
    }

    public static void filledPoly(float[] vertices){
    }

    public static void arc(float x, float y, float radius, float startAngle, float degrees){
    }

    public static void filledArc(float x, float y, float radius, float startAngle, float degrees){
    }

    public static void oval(float x, float y, float radiusX, float radiusY){
    }

    public static void filledOval(float x, float y, float radiusX, float radiusY){
    }

    public static void dashLine(float x1, float y1, float x2, float y2, int segments){
    }
}
