package nitis.gravillaso.graphics;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class GrPal{
    /** Any usage of this color should be temporal */
    public static Color todoColor = Color.red;

    public static Color outline = Pal.darkOutline;
    public static Color cobalt = Color.valueOf("9aa3bf");
    public static Color frost = Color.valueOf("9bd7ff");

    public static Color cobaltShot = Color.valueOf("b8c2e0");

    // debug view
    public static Color debugColdColor = Color.valueOf("4d9fff");
    public static Color debugHotColor = Color.valueOf("ff5f3c");
}
