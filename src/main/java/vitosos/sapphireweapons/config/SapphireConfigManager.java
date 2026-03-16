package vitosos.sapphireweapons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SapphireConfigManager {

    // Creates a pretty-printing JSON builder so the file is easily readable for users
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Locates the Minecraft /config/ folder and names your file
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapphire_star_armaments.json");

    // This is the active config that the rest of your mod will read from!
    public static SapphireConfig CONFIG = new SapphireConfig();

    public static void register() {
        if (CONFIG_FILE.exists()) {
            load();
        } else {
            save(); // Generates the default file if it doesn't exist
        }
    }

    private static void load() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            CONFIG = GSON.fromJson(reader, SapphireConfig.class);
        } catch (IOException e) {
            System.err.println("[Sapphire Star Armaments] Failed to load config! Using defaults.");
            e.printStackTrace();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException e) {
            System.err.println("[Sapphire Star Armaments] Failed to save config!");
            e.printStackTrace();
        }
    }
}