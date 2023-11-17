package com.alexei.spaceshooter;

import static com.alexei.spaceshooter.SpaceShooter.drawer;
import static com.alexei.spaceshooter.SpaceShooter.batch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Alex on 03/07/2015.
 */
public class ItemStar extends Item {
    public static final float STAR_SIZE_OUTER = 20;
    public static final float STAR_SIZE_INNER = 10;
    public static final Color STAR_COLOR = Color.YELLOW;
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;
    private static float[] polys;
    private static ShortArray triangles;
    private static final Matrix4 matrix4 = new Matrix4();
    /**
     * Rotation speed of star, in degrees per deltaTime. deltaTime is the frequency of
     * of the render loop. This value is randomized at construction.
     */
    private final int rotationSpeed = MathUtils.random(45, 135);
    private int multiplier = 1;
    /**
     *
     * @param x
     * @param y
     * @param multiplier the amount that the default star-value is multiplied by to determine the value of a star. multiplier affects star size as well.
     */
    public ItemStar(float x, float y, int multiplier) {
        super(x, y, STAR_SIZE_OUTER*2*multiplier, STAR_SIZE_OUTER*2*multiplier);
        super.setColor(STAR_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        // randomize orientation at construction
        super.setOrientation(MathUtils.random(0, 359));
        super.setPickUpSound(PICK_UP_SOUND);
        this.multiplier = multiplier;

        if (polys == null) {
            polys = createStarPolygons(
                    5,
                    STAR_SIZE_OUTER * this.multiplier,
                    STAR_SIZE_INNER * this.multiplier);

            EarClippingTriangulator t = new EarClippingTriangulator();
            triangles = t.computeTriangles(polys);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float d = deltaTime / 1000f * rotationSpeed;
        setOrientation(getOrientation() + d);
    }

    /**
     * Draw a star by drawing two filled squares, one of which is rotated by 45 deg.
     * @param sr
     */
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch2) {
        float scale = getPickUpAnimationScale();
        if (scale==0) scale = 1;
        matrix4.idt();
        matrix4.setToTranslationAndScaling(getCenterX(), getCenterY(), 0, scale, scale, 1);
        matrix4.rotate(0, 0, 1f, getOrientation());
        batch.setTransformMatrix(matrix4);
        drawer.setColor(getColor());
        drawer.filledPolygon(polys, triangles);
    }

    private static float[] createStarPolygons(int arms, float rOuter, float rInner) {
        double angle = Math.PI / (arms * 2);

        int len = arms*4;
        float[] v = new float[len]; // 2 sets of arms and 2 coords per vertex = 4
        int j = 0;
        for (int i = 0; i < len; i+=2) {
            float r = (j & 1) == 0 ? rOuter : rInner;
            double t = i * angle;
            v[i] = (float)Math.cos(t) * r;
            v[i+1] = (float)Math.sin(t) * r;
            j++;
        }
        return v;
    }
}
