package ch.epfl.mmspg.testbed360;

/**
 * Contains vertex, normal and color data.
 *
 * @author Louis-Maxence Garret <louis-maxence.garret@epfl.ch>
 * @date 12/10/2017
 */

public final class WorldLayoutData {
    public final static int SPHERE_RINGS_COUNT = 50;
    public final static int SPHERE_SECTORS_PER_RING = 10;
    private final static int SPHERE_RADIUS = 20;

    public static float[] SPHERE_VERTICES;
    public static float[] SPHERE_NORMALS;
    public static float[] SPHERE_TEXTURE;
    public static char[] SPHERE_INDEXES;

    public static void initSphereData() {
        SPHERE_VERTICES = new float[SPHERE_RINGS_COUNT * SPHERE_SECTORS_PER_RING * 3];
        SPHERE_NORMALS = new float[SPHERE_RINGS_COUNT * SPHERE_SECTORS_PER_RING * 3];
        SPHERE_TEXTURE = new float[SPHERE_RINGS_COUNT * SPHERE_SECTORS_PER_RING * 2];
        SPHERE_INDEXES = new char[SPHERE_RINGS_COUNT * SPHERE_SECTORS_PER_RING * 6];

        float R = 1f / (float) (SPHERE_RINGS_COUNT - 1);
        float S = 1f / (float) (SPHERE_SECTORS_PER_RING - 1);

        float x;
        float y;
        float z;

        int vertexIndex = 0;
        int textureIndex = 0;
        int indexIndex = 0;
        int normalIndex = 0;

        int r;
        int s;

        for (r = 0; r < SPHERE_RINGS_COUNT; r++) {
            for (s = 0; s < SPHERE_SECTORS_PER_RING; s++) {
                y = (float) Math.sin((-Math.PI / 2f) + Math.PI * r * R);
                x = (float) Math.cos(2f * Math.PI * s * S) * (float) Math.sin(Math.PI * r * R);
                z = (float) Math.sin(2f * Math.PI * s * S) * (float) Math.sin(Math.PI * r * R);

                if (SPHERE_TEXTURE != null) {
                    SPHERE_TEXTURE[textureIndex] = s * S;
                    SPHERE_TEXTURE[textureIndex + 1] = r * R;
                    textureIndex += 2;
                }
                SPHERE_VERTICES[vertexIndex] = x * SPHERE_RADIUS;
                SPHERE_VERTICES[vertexIndex + 1] = y * SPHERE_RADIUS;
                SPHERE_VERTICES[vertexIndex + 2] = z * SPHERE_RADIUS;

                vertexIndex += 3;

                SPHERE_NORMALS[normalIndex] = x;
                SPHERE_NORMALS[normalIndex + 1] = y;
                SPHERE_NORMALS[normalIndex + 2] = z;

                normalIndex += 3;
            }
        }


        int r1, s1;
        for (r = 0; r < SPHERE_RINGS_COUNT; r++) {
            for (s = 0; s < SPHERE_SECTORS_PER_RING; s++) {
                r1 = (r + 1 == SPHERE_RINGS_COUNT) ? 0 : r + 1;
                s1 = (s + 1 == SPHERE_SECTORS_PER_RING) ? 0 : s + 1;

                SPHERE_INDEXES[indexIndex] = (char) (r * SPHERE_SECTORS_PER_RING + s);
                SPHERE_INDEXES[indexIndex + 1] = (char) (r * SPHERE_SECTORS_PER_RING + (s1));
                SPHERE_INDEXES[indexIndex + 2] = (char) ((r1) * SPHERE_SECTORS_PER_RING + (s1));

                SPHERE_INDEXES[indexIndex + 3] = (char) ((r1) * SPHERE_SECTORS_PER_RING + s);
                SPHERE_INDEXES[indexIndex + 4] = (char) ((r1) * SPHERE_SECTORS_PER_RING + (s1));
                SPHERE_INDEXES[indexIndex + 5] = (char) (r * SPHERE_SECTORS_PER_RING + s);
                indexIndex += 6;
            }
        }
    }


    public static final float[] CUBE_COORDS = new float[]{
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
    };

    public static final float[] CUBE_COLORS = new float[]{
            // front, green
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,

            // right, blue
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,

            // back, also green
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,
            0f, 0.5273f, 0.2656f, 1.0f,

            // left, also blue
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,

            // top, red
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,

            // bottom, also red
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
            0.8359375f, 0.17578125f, 0.125f, 1.0f,
    };

    public static final float[] CUBE_NORMALS = new float[]{
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
    };
}
