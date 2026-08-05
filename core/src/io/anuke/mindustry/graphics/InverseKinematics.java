package io.anuke.mindustry.graphics;

import com.badlogic.gdx.math.Vector2;
import io.anuke.ucore.util.Mathf;

/**Two-segment inverse kinematics solver, used for leg unit movement.*/
public class InverseKinematics{
    private static final Vector2[] mat1 = {new Vector2(), new Vector2()}, mat2 = {new Vector2(), new Vector2()};
    private static final Vector2 temp = new Vector2(), temp2 = new Vector2(), at1 = new Vector2();

    public static boolean solve(float lengthA, float lengthB, Vector2 end, boolean side, Vector2 result){
        at1.set(end).rotate(side ? 1 : -1).setLength(lengthA + lengthB).add(end.x / 2f, end.y / 2f);
        return solve(lengthA, lengthB, end, at1, result);
    }

    /**
     * Solves the position of a joint between two segments of known length.
     *
     * @param lengthA first line segment length
     * @param lengthB second line segment length
     * @param end end position relative to the origin
     * @param attractor direction the result should be closer to (since there are usually 2 solutions)
     * @param result output point, the position of the joint relative to the origin
     * @return whether IK succeeded (this can fail if the end is too far, for example)
     */
    public static boolean solve(float lengthA, float lengthB, Vector2 end, Vector2 attractor, Vector2 result){
        Vector2 axis = mat2[0].set(end).nor();
        mat2[1].set(attractor).sub(temp2.set(axis).scl(attractor.dot(axis))).nor();
        mat1[0].set(mat2[0].x, mat2[1].x);
        mat1[1].set(mat2[0].y, mat2[1].y);
        result.set(mat2[0].dot(end), mat2[1].dot(end));
        float len = result.len();
        float dist = Math.max(0, Math.min(lengthA, (len + (lengthA * lengthA - lengthB * lengthB) / len) / 2));
        Vector2 src = temp.set(dist, Mathf.sqrt(lengthA * lengthA - dist * dist));
        result.set(mat1[0].dot(src), mat1[1].dot(src));

        return dist > 0 && dist < lengthA;
    }
}
