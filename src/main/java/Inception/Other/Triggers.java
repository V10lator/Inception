/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Inception.Other;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Xaymar
 */
public enum Triggers {
    ChunkLoadUnload("ChunkLoadUnload"),
    BlockPlace("BlockPlace"),
    BlockBreak("BlockBreak"),
    BlockBurn("BlockBurn"),
    BlockFade("BlockFade"),
    BlockForm("BlockForm"),
    BlockGrow("BlockGrow"),
    BlockSpread("BlockSpread");

    private static final Map<String, Triggers> NAME_MAP = new HashMap<String, Triggers>();
    
    private String name;
    
    static {
        for (Triggers type : values()) {
            if (type.name != null) {
                NAME_MAP.put(type.name.toLowerCase(), type);
            }
        }
    }
    
    private Triggers(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Triggers fromName(String name) {
        if (name == null) {
            return null;
        }
        return NAME_MAP.get(name.toLowerCase());
    }
}
