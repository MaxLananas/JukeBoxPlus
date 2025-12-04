package com.example.jukeboxplus.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicDatabase {
    
    private static final Map<String, MusicInfo> MUSIC_DATA = new HashMap<>();
    
    static {
        // ═══════════════════════════════════════════
        // MUSIC DISCS
        // ═══════════════════════════════════════════
        
        addDisc("music_disc.13", "13", "C418", 178, 0xBE7F2A);
        addDisc("music_disc.cat", "Cat", "C418", 185, 0x4EBF2A);
        addDisc("music_disc.blocks", "Blocks", "C418", 345, 0xD97F34);
        addDisc("music_disc.chirp", "Chirp", "C418", 185, 0xC43C3C);
        addDisc("music_disc.far", "Far", "C418", 174, 0x6FB564);
        addDisc("music_disc.mall", "Mall", "C418", 197, 0x9065CC);
        addDisc("music_disc.mellohi", "Mellohi", "C418", 96, 0xD48ED4);
        addDisc("music_disc.stal", "Stal", "C418", 150, 0x343434);
        addDisc("music_disc.strad", "Strad", "C418", 188, 0xE8E8E8);
        addDisc("music_disc.ward", "Ward", "C418", 251, 0x42B34A);
        addDisc("music_disc.11", "11", "C418", 71, 0x343434);
        addDisc("music_disc.wait", "Wait", "C418", 238, 0x4AB3D4);
        addDisc("music_disc.pigstep", "Pigstep", "Lena Raine", 149, 0xD4A44A);
        addDisc("music_disc.otherside", "Otherside", "Lena Raine", 195, 0x36BCD4);
        addDisc("music_disc.5", "5", "Samuel Aberg", 178, 0x36D4C4);
        addDisc("music_disc.relic", "Relic", "Aaron Cherof", 218, 0xC4A036);
        addDisc("music_disc.creator", "Creator", "Lena Raine", 176, 0xD436C4);
        addDisc("music_disc.creator_music_box", "Creator (Music Box)", "Lena Raine", 73, 0xE8D4C4);
        addDisc("music_disc.precipice", "Precipice", "Aaron Cherof", 299, 0x364AD4);
        
        // ═══════════════════════════════════════════
        // AMBIENT MUSIC
        // ═════════════════════════��═══���═════════════
        
        addAmbient("music.game", "Minecraft", "C418", 180, MusicInfo.MusicType.AMBIENT);
        addAmbient("music.creative", "Creative Mode", "C418", 180, MusicInfo.MusicType.CREATIVE);
        addAmbient("calm1", "Minecraft", "C418", 270, MusicInfo.MusicType.AMBIENT);
        addAmbient("calm2", "Clark", "C418", 198, MusicInfo.MusicType.AMBIENT);
        addAmbient("calm3", "Sweden", "C418", 234, MusicInfo.MusicType.AMBIENT);
        addAmbient("hal1", "Subwoofer Lullaby", "C418", 210, MusicInfo.MusicType.AMBIENT);
        addAmbient("hal2", "Living Mice", "C418", 180, MusicInfo.MusicType.AMBIENT);
        addAmbient("hal3", "Haggstrom", "C418", 204, MusicInfo.MusicType.AMBIENT);
        addAmbient("hal4", "Danny", "C418", 252, MusicInfo.MusicType.AMBIENT);
        addAmbient("nuance1", "Key", "C418", 60, MusicInfo.MusicType.AMBIENT);
        addAmbient("nuance2", "Oxygene", "C418", 60, MusicInfo.MusicType.AMBIENT);
        addAmbient("piano1", "Dry Hands", "C418", 60, MusicInfo.MusicType.AMBIENT);
        addAmbient("piano2", "Wet Hands", "C418", 90, MusicInfo.MusicType.AMBIENT);
        addAmbient("piano3", "Mice on Venus", "C418", 282, MusicInfo.MusicType.AMBIENT);
        
        // Nether
        addAmbient("nether1", "Concrete Halls", "C418", 240, MusicInfo.MusicType.NETHER);
        addAmbient("nether2", "Dead Voxel", "C418", 300, MusicInfo.MusicType.NETHER);
        addAmbient("nether3", "Warmth", "C418", 240, MusicInfo.MusicType.NETHER);
        addAmbient("nether4", "Ballad of the Cats", "C418", 240, MusicInfo.MusicType.NETHER);
        addAmbient("rubedo", "Rubedo", "Lena Raine", 300, MusicInfo.MusicType.NETHER);
        addAmbient("chrysopoeia", "Chrysopoeia", "Lena Raine", 300, MusicInfo.MusicType.NETHER);
        addAmbient("so_below", "So Below", "Lena Raine", 300, MusicInfo.MusicType.NETHER);
        
        // End
        addAmbient("end", "The End", "C418", 900, MusicInfo.MusicType.END);
        addAmbient("boss", "Boss", "C418", 360, MusicInfo.MusicType.END);
        
        // Credits
        addAmbient("credits", "Alpha", "C418", 600, MusicInfo.MusicType.CREDITS);
        
        // Menu
        addAmbient("menu1", "Mutation", "C418", 180, MusicInfo.MusicType.MENU);
        addAmbient("menu2", "Moog City 2", "C418", 180, MusicInfo.MusicType.MENU);
        addAmbient("menu3", "Beginning 2", "C418", 180, MusicInfo.MusicType.MENU);
        addAmbient("menu4", "Floating Trees", "C418", 180, MusicInfo.MusicType.MENU);
    }
    
    private static void addDisc(String id, String title, String artist, int duration, int color) {
        MUSIC_DATA.put(id.toLowerCase(), new MusicInfo(
            id, title, artist, MusicInfo.MusicType.DISC, duration, color
        ));
    }
    
    private static void addAmbient(String id, String title, String artist, int duration, MusicInfo.MusicType type) {
        MUSIC_DATA.put(id.toLowerCase(), new MusicInfo(
            id, title, artist, type, duration, getTypeColor(type)
        ));
    }
    
    private static int getTypeColor(MusicInfo.MusicType type) {
        return switch (type) {
            case AMBIENT -> 0x4A90D9;
            case CREATIVE -> 0x7BD94A;
            case NETHER -> 0xD94A4A;
            case END -> 0xD9D94A;
            case CREDITS -> 0xD94AD9;
            case MENU -> 0x9E9E9E;
            default -> 0xAAAAAA;
        };
    }
    
    public static MusicInfo getByIdentifier(String id) {
        if (id == null) return null;
        
        String searchId = id.toLowerCase();
        
        // Recherche directe
        if (MUSIC_DATA.containsKey(searchId)) {
            return cloneWithNewStartTime(MUSIC_DATA.get(searchId));
        }
        
        // Recherche partielle
        for (Map.Entry<String, MusicInfo> entry : MUSIC_DATA.entrySet()) {
            if (searchId.contains(entry.getKey()) || entry.getKey().contains(searchId)) {
                return cloneWithNewStartTime(entry.getValue());
            }
        }
        
        // Recherche par nom de fichier
        String[] parts = searchId.split("[/:]");
        String filename = parts[parts.length - 1].replace(".ogg", "");
        
        for (Map.Entry<String, MusicInfo> entry : MUSIC_DATA.entrySet()) {
            if (entry.getKey().contains(filename) || filename.contains(entry.getKey())) {
                return cloneWithNewStartTime(entry.getValue());
            }
        }
        
        return null;
    }
    
    private static MusicInfo cloneWithNewStartTime(MusicInfo original) {
        MusicInfo clone = new MusicInfo(
            original.getId(),
            original.getTitle(),
            original.getArtist(),
            original.getType(),
            original.getDurationSeconds(),
            original.getDiscColor()
        );
        clone.setStartTime(System.currentTimeMillis());
        return clone;
    }
    
    public static MusicInfo createUnknown(String id) {
        String title = id;
        if (id.contains("/")) {
            title = id.substring(id.lastIndexOf('/') + 1);
        }
        if (id.contains(":")) {
            title = id.substring(id.lastIndexOf(':') + 1);
        }
        title = title.replace(".ogg", "").replace("_", " ");
        title = capitalizeWords(title);
        
        return new MusicInfo(id, title, "Unknown", MusicInfo.MusicType.UNKNOWN, 180, 0x888888);
    }
    
    private static String capitalizeWords(String str) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    // ═══════════════════════════════════════════
    // NOUVELLE MÉTHODE - Liste complète
    // ═══════════════════════════════════════════
    
    public static List<MusicInfo> getAllMusic() {
        List<MusicInfo> allMusic = new ArrayList<>();
        
        for (Map.Entry<String, MusicInfo> entry : MUSIC_DATA.entrySet()) {
            allMusic.add(new MusicInfo(
                entry.getValue().getId(),
                entry.getValue().getTitle(),
                entry.getValue().getArtist(),
                entry.getValue().getType(),
                entry.getValue().getDurationSeconds(),
                entry.getValue().getDiscColor()
            ));
        }
        
        // Trier par type puis par titre
        allMusic.sort((a, b) -> {
            int typeCompare = a.getType().compareTo(b.getType());
            if (typeCompare != 0) return typeCompare;
            return a.getTitle().compareTo(b.getTitle());
        });
        
        return allMusic;
    }
}