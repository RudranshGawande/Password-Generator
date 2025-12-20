package com.megaproject.passwordgenerator;

public class HistoryItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle;

    private String name; // Custom name (e.g. Gmail)
    private String password;
    private String timestamp;
    private int iconResId;
    private int colorResId; // For icon bg and dot
    private int iconTintResId; // For icon tint

    // Constructor for Header
    public HistoryItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor for Item
    public HistoryItem(String name, String password, String timestamp, int iconResId, int colorResId, int iconTintResId) {
        this.type = TYPE_ITEM;
        this.name = name;
        this.password = password;
        this.timestamp = timestamp;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.iconTintResId = iconTintResId;
    }

    public void setName(String name) { this.name = name; }
    public int getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getTimestamp() { return timestamp; }
    public int getIconResId() { return iconResId; }
    public int getColorResId() { return colorResId; }
    public int getIconTintResId() { return iconTintResId; }
}
