package vitosos.sapphireweapons.util;

public enum CantineDish {
    FIERY_LAVA_CHICKEN(500),
    SUGARY_BEE_APPLES(250),
    HEARTY_BEEF(250),
    LUCKY_FISH_SALAD(150),
    SPECIAL_RATION(50);

    private final int cost;

    CantineDish(int cost) {
        this.cost = cost;
    }

    public int getCost() { return this.cost; }
    public String getTranslationKey() { return "cantine.sapphire-star-armaments." + this.name().toLowerCase(); }
    public String getDescKey() { return "cantine.sapphire-star-armaments." + this.name().toLowerCase() + ".desc"; }
}