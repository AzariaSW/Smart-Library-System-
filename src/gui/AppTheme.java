package gui;

import java.awt.*;

/**
 * Centralized color palette, font, and dimension definitions.
 * Every UI component references these constants for a consistent look.
 */
public class AppTheme {

    // ── Primary Palette ────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK   = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT  = new Color(219, 234, 254);
    public static final Color ACCENT         = new Color(16, 185, 129);
    public static final Color ACCENT_DARK    = new Color(5, 150, 105);
    public static final Color DANGER         = new Color(239, 68, 68);
    public static final Color DANGER_DARK    = new Color(220, 38, 38);
    public static final Color WARNING        = new Color(245, 158, 11);
    public static final Color WARNING_LIGHT  = new Color(254, 243, 199);
    public static final Color SUCCESS        = new Color(34, 197, 94);
    public static final Color SUCCESS_LIGHT  = new Color(220, 252, 231);
    public static final Color PURPLE         = new Color(139, 92, 246);
    public static final Color PURPLE_LIGHT   = new Color(237, 233, 254);
    public static final Color GRAY           = new Color(107, 114, 128);
    public static final Color GRAY_LIGHT     = new Color(243, 244, 246);

    // ── Backgrounds ────────────────────────────────────────────────────────────
    public static final Color BG_MAIN        = new Color(248, 250, 252);
    public static final Color BG_SIDEBAR     = new Color(15, 23, 42);
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_TABLE_HEADER= new Color(241, 245, 249);
    public static final Color BG_TABLE_ALT   = new Color(248, 250, 252);

    // ── Text ───────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color TEXT_SIDEBAR   = new Color(203, 213, 225);
    public static final Color TEXT_SIDEBAR_ACTIVE = Color.WHITE;
    public static final Color TEXT_WHITE     = Color.WHITE;

    // ── Borders ────────────────────────────────────────────────────────────────
    public static final Color BORDER         = new Color(226, 232, 240);
    public static final Color BORDER_FOCUS   = PRIMARY;

    // ── Fonts ──────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_TABLE      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_SIDEBAR    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SIDEBAR_ACTIVE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CLOCK      = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_STAT_NUM   = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_STAT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);

    // ── Dimensions ─────────────────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH    = 230;
    public static final int CORNER_RADIUS    = 10;
    public static final Insets CARD_PADDING  = new Insets(20, 20, 20, 20);
}
