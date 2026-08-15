package ar1hurgit.pearlsplus.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PearlsPlusConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pearlsplus.json");
    private static PearlsPlusConfig instance;

    public boolean showNametag = true;
    public float nametagScale = 0.5f;
    public double maxDistance = 90.0D;

    public static PearlsPlusConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static PearlsPlusConfig load() {
        try {
            if (Files.exists(PATH)) {
                PearlsPlusConfig loaded = GSON.fromJson(Files.readString(PATH), PearlsPlusConfig.class);
                if (loaded != null) return loaded;
            }
        } catch (IOException | RuntimeException ignored) {
            // fall back to defaults on any read/parse failure
        }
        return new PearlsPlusConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException ignored) {
            // best-effort save
        }
    }
}
