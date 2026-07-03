package arc.util;

import arc.Core;
import arc.struct.ObjectMap;

/**Simple bundle/internationalization handler.*/
public class Bundles{
    private static ObjectMap<String, String> strings = new ObjectMap<>();

    public static void load(String bundleData){
        strings.clear();
        if(bundleData == null) return;
        String[] lines = bundleData.split("\n");
        for(String line : lines){
            int idx = line.indexOf('=');
            if(idx > 0){
                strings.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }
    }

    public static String get(String key){
        String result = strings.get(key);
        return result != null ? result : key;
    }

    public static String get(String key, String defaultValue){
        String result = strings.get(key);
        return result != null ? result : defaultValue;
    }

    public static String getNotNull(String key){
        String result = strings.get(key);
        return result != null ? result : key;
    }

    public static String getOrNull(String key){
        return strings.get(key);
    }

    public static boolean has(String key){
        return strings.containsKey(key);
    }

    public static String format(String key, Object... args){
        return Strings.format(get(key), args);
    }
}
