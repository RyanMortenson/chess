package ui;

public class Colors {
    // ────────────────────────────────────────────────────────────────────────────────
    // 1) Primary Foreground Colors (30–37), plus default (39)
    // ────────────────────────────────────────────────────────────────────────────────
    public static final String FG_BLACK       = "\u001B[30m";
    public static final String FG_RED         = "\u001B[31m";
    public static final String FG_GREEN       = "\u001B[32m";
    public static final String FG_ORANGE      = "\u001B[33m"; // often called Yellow
    public static final String FG_BLUE        = "\u001B[34m";
    public static final String FG_MAGENTA     = "\u001B[35m";
    public static final String FG_CYAN        = "\u001B[36m";
    public static final String FG_LIGHT_GRAY  = "\u001B[37m";
    public static final String FG_DEFAULT     = "\u001B[39m"; // fallback default

    // ────────────────────────────────────────────────────────────────────────────────
    // 2) Primary Background Colors (40–47), plus default (49)
    // ────────────────────────────────────────────────────────────────────────────────
    public static final String BG_BLACK       = "\u001B[40m";
    public static final String BG_RED         = "\u001B[41m";
    public static final String BG_GREEN       = "\u001B[42m";
    public static final String BG_ORANGE      = "\u001B[43m"; // often called Yellow
    public static final String BG_BLUE        = "\u001B[44m";
    public static final String BG_MAGENTA     = "\u001B[45m";
    public static final String BG_CYAN        = "\u001B[46m";
    public static final String BG_LIGHT_GRAY  = "\u001B[47m";
    public static final String BG_DEFAULT     = "\u001B[49m"; // fallback default

    // ────────────────────────────────────────────────────────────────────────────────
    // 3) Bright (High‐Intensity) Foregrounds (90–97) and Backgrounds (100–107)
    // ────────────────────────────────────────────────────────────────────────────────
    public static final String FG_DARK_GRAY   = "\u001B[90m";
    public static final String FG_LIGHT_RED   = "\u001B[91m";
    public static final String FG_LIGHT_GREEN = "\u001B[92m";
    public static final String FG_YELLOW      = "\u001B[93m";
    public static final String FG_LIGHT_BLUE  = "\u001B[94m";
    public static final String FG_LIGHT_MAGENTA = "\u001B[95m";
    public static final String FG_TEAL        = "\u001B[96m";
    public static final String FG_WHITE       = "\u001B[97m";

    public static final String BG_DARK_GRAY   = "\u001B[100m";
    public static final String BG_LIGHT_RED   = "\u001B[101m";
    public static final String BG_LIGHT_GREEN = "\u001B[102m";
    public static final String BG_YELLOW      = "\u001B[103m";
    public static final String BG_LIGHT_BLUE  = "\u001B[104m";
    public static final String BG_LIGHT_MAGENTA = "\u001B[105m";
    public static final String BG_TEAL        = "\u001B[106m";
    public static final String BG_WHITE       = "\u001B[107m";

    // ────────────────────────────────────────────────────────────────────────────────
    // 4) Modifiers (0–8)
    // ────────────────────────────────────────────────────────────────────────────────
    public static final String RESET         = "\u001B[0m";  // none / reset all
    public static final String BOLD          = "\u001B[1m";
    public static final String UNDERLINE     = "\u001B[4m";  // underscore
    public static final String BLINK         = "\u001B[5m";
    public static final String REVERSE       = "\u001B[7m";  // swap foreground/background
    public static final String CONCEALED     = "\u001B[8m";  // hidden text

    // ────────────────────────────────────────────────────────────────────────────────
    // 5) Helper methods (optional)
    // ────────────────────────────────────────────────────────────────────────────────

    /** Wraps `text` in the given color code, then resets. */
    public static String colorText(String colorCode, String text) {
        return colorCode + text + RESET;
    }

    /** Wraps `text` in the given color code and modifier, then resets. */
    public static String colorText(String colorCode, String modifier, String text) {
        return modifier + colorCode + text + RESET;
    }

    private Colors() {
        // prevent instantiation
    }
}
