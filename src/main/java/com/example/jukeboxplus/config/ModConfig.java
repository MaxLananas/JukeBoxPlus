package com.example.jukeboxplus.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    
    private static ModConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("jukeboxplus.json");
    
    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    
    // Options
    private boolean overlayEnabled = true;
    private Position position = Position.TOP_RIGHT;
    private boolean showAmbientMusic = true;
    private boolean showDiscMusic = true;
    private int displayDuration = 5; // seconds
    private boolean animations = true;
    
    private ModConfig() {
        load();
    }
    
    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }
    
    // Getters
    public boolean isOverlayEnabled() { return overlayEnabled; }
    public Position getPosition() { return position; }
    public boolean showAmbientMusic() { return showAmbientMusic; }
    public boolean showDiscMusic() { return showDiscMusic; }
    public int getDisplayDuration() { return displayDuration; }
    public boolean useAnimations() { return animations; }
    
    // Setters
    public void setOverlayEnabled(boolean enabled) { this.overlayEnabled = enabled; save(); }
    public void setPosition(Position pos) { this.position = pos; save(); }
    public void setShowAmbientMusic(boolean show) { this.showAmbientMusic = show; save(); }
    public void setShowDiscMusic(boolean show) { this.showDiscMusic = show; save(); }
    public void setDisplayDuration(int seconds) { this.displayDuration = seconds; save(); }
    public void setAnimations(boolean anim) { this.animations = anim; save(); }
    
    private void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("overlayEnabled", overlayEnabled);
            json.addProperty("position", position.name());
            json.addProperty("showAmbientMusic", showAmbientMusic);
            json.addProperty("showDiscMusic", showDiscMusic);
            json.addProperty("displayDuration", displayDuration);
            json.addProperty("animations", animations);
            
            Files.writeString(CONFIG_PATH, GSON.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        
        try {
            String jsonString = Files.readString(CONFIG_PATH);
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            
            if (json.has("overlayEnabled")) overlayEnabled = json.get("overlayEnabled").getAsBoolean();
            if (json.has("position")) position = Position.valueOf(json.get("position").getAsString());
            if (json.has("showAmbientMusic")) showAmbientMusic = json.get("showAmbientMusic").getAsBoolean();
            if (json.has("showDiscMusic")) showDiscMusic = json.get("showDiscMusic").getAsBoolean();
            if (json.has("displayDuration")) displayDuration = json.get("displayDuration").getAsInt();
            if (json.has("animations")) animations = json.get("animations").getAsBoolean();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}