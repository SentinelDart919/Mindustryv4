package arc.util;

import arc.entities.EntityCollisions.TileCollider;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;

public class Physics{
    private final static Vec2 vector = new Vec2();
    private final static Point2 point = new Point2();

    public static Point2 vectorCast(float x0f, float y0f, float x1f, float y1f, TileCollider collider){
        int x0 = (int) x0f;
        int y0 = (int) y0f;
        int x1 = (int) x1f;
        int y1 = (int) y1f;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int e2;
        while(true){
            if(collider.solid(x0, y0)) return point.set(x0, y0);
            if(x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if(e2 > -dy){ err = err - dy; x0 = x0 + sx; }
            if(e2 < dx){ err = err + dx; y0 = y0 + sy; }
        }
        return null;
    }

    public static Vec2 raycastRect(float startx, float starty, float endx, float endy, Rect rectangle){
        return raycastRect(startx, starty, endx, endy,
            rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2,
            rectangle.width / 2f, rectangle.height / 2f);
    }

    public static Vec2 raycastRect(float startx, float starty, float endx, float endy, float x, float y, float halfx, float halfy){
        float deltax = endx - startx, deltay = endy - starty;
        Vec2 hit = vector;
        float paddingX = 0f;
        float paddingY = 0f;
        float scaleX = 1.0f / deltax;
        float scaleY = 1.0f / deltay;
        int signX = Mathf.sign(scaleX);
        int signY = Mathf.sign(scaleY);
        float nearTimeX = (x - signX * (halfx + paddingX) - startx) * scaleX;
        float nearTimeY = (y - signY * (halfy + paddingY) - starty) * scaleY;
        float farTimeX = (x + signX * (halfx + paddingX) - startx) * scaleX;
        float farTimeY = (y + signY * (halfy + paddingY) - starty) * scaleY;

        if(nearTimeX > farTimeY || nearTimeY > farTimeX) return null;
        float nearTime = nearTimeX > nearTimeY ? nearTimeX : nearTimeY;
        float farTime = farTimeX < farTimeY ? farTimeX : farTimeY;
        if(nearTime >= 1 || farTime <= 0) return null;

        float htime = Mathf.clamp(nearTime);
        hit.x = startx + htime * deltax;
        hit.y = starty + htime * deltay;
        return hit;
    }

    public static Vec2 overlap(Rect a, Rect b, boolean x){
        float penetration = 0f;
        float ax = a.x + a.width / 2, bx = b.x + b.width / 2;
        float ay = a.y + a.height / 2, by = b.y + b.height / 2;
        float nx = ax - bx, ny = ay - by;
        float aex = a.width / 2, bex = b.width / 2;

        float xoverlap = aex + bex - Math.abs(nx);
        if(Math.abs(xoverlap) > 0){
            float aey = a.height / 2, bey = b.height / 2;
            float yoverlap = aey + bey - Math.abs(ny);
            if(Math.abs(yoverlap) > 0){
                if(Math.abs(xoverlap) < Math.abs(yoverlap)){
                    vector.x = nx < 0 ? 1 : -1;
                    vector.y = 0;
                    penetration = xoverlap;
                }else{
                    vector.x = 0;
                    vector.y = ny < 0 ? 1 : -1;
                    penetration = yoverlap;
                }
            }
        }

        float percent = 1f, slop = 0.0f, m = Math.max(penetration - slop, 0.0f);
        vector.x = -m * vector.x * percent;
        vector.y = -m * vector.y * percent;
        return vector;
    }
}