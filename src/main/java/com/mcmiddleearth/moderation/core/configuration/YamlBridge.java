/*
 * Copyright (C) 2020 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.moderation.core.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eriol_Eandur
 */

@SuppressWarnings({"unchecked","unused"})
public class YamlBridge {
    
    private Map<String,Object> map = new HashMap<>();
    
    public void load(File file) {
        Yaml yaml = new Yaml();
        try(FileInputStream in =  new FileInputStream(file)){
            map = yaml.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(File file) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try(FileWriter out = new FileWriter(file)) {
            yaml.dump(map, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String,Object> getSection(String key) {
        Object value = getValue(key);
        return ((value != null) ? (Map<String, Object>) value : null);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Boolean)value:defaultValue);
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Integer)value:defaultValue);
    }
    
    public double getDouble(String key, double defaultValue) {
        Object value = getValue(key);
        return (value!=null?(Double)value:defaultValue);
    }
    
    public String getString(String key, String defaultValue) {
        Object value = getValue(key);
        return (value!=null?(String)value:defaultValue);
    }
    
    public List<String> getStringList(String key) {
        return (List<String>)getValue(key);
    }

    public List<Map<String,Object>> getList(String key) { return (List<Map<String,Object>>)getValue(key);}
    
    private Object getValue(String key) {
        return getValue(map, key.split("\\."));
    }
    
    private Object getValue(Map<String,Object> submap, String[] subkeys) {
        if(subkeys.length>1) {
            if(submap.containsKey(subkeys[0])) {
                return getValue((Map<String,Object>)submap.get(subkeys[0]),
                                Arrays.copyOfRange(subkeys, 1, subkeys.length));
            } else {
                return null;
            }
        } else {
            return submap.get(subkeys[0]);
        }
    }

    public void set(String key, Object value) {
        set(map, key.split("\\."), value);
    }

    public void set(Map<String,Object> submap, String[] subkeys, Object value) {
        if(subkeys.length>1) {
            if(submap.containsKey(subkeys[0]) && (submap.get(subkeys[0]) instanceof Map)) {
                set((Map<String,Object>)submap.get(subkeys[0]),
                        Arrays.copyOfRange(subkeys, 1, subkeys.length),value);
            } else {
                Map<String, Object> insertion = new HashMap<>();
                submap.put(subkeys[0],insertion);
                set(insertion, Arrays.copyOfRange(subkeys, 1, subkeys.length),value);
            }
        } else {
            submap.put(subkeys[0],value);
        }
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
