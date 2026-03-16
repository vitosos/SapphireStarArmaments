package vitosos.sapphireweapons.util;

public enum ShopCategory {
    CONSUMABLES("Consumables Shop"),
    MAINTENANCE("Guild Maintenance"),
    EXPERIENCE("Experience Exchange"),
    CANTINE("Cantine"),
    COMING_SOON("Coming Soon!");

    private final String displayName;

    ShopCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}