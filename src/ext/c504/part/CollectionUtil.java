package ext.c504.part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CollectionUtil {
    /**
     * Convert list to a map from the index map just like <"1", value>, <"2", value>...
     *
     * @param list     list
     * @param indexMap index map
     * @return new map
     */
    public static Map convertListFromIndexMap(List<String> list, Map<String, String> indexMap) {
        Map newMap = new HashMap();
        int listSize = list.size();
        int keySize = indexMap.keySet().size();
        int loopSize = Math.max(listSize, keySize);
        Object key;
        Object value;
        String strIndex;
        for (int i = 0; i < loopSize; i++) {
            strIndex = String.valueOf(i);
            key = indexMap.get(strIndex);
            if (key != null) {
                if (i < listSize) {
                    value = list.get(i);
                } else {
                    value = "";
                }
                newMap.put(key, value);
            }
        }
        return newMap;
    }

    /**
     * Private constructor
     */
    private CollectionUtil() {
    }

    /**
     * Convents a list to Vector instance to apply some  windchill instances.
     *
     * @param list a list
     * @return a vector instance.
     */
    @SuppressWarnings("unchecked")
    public static Vector toVector(List list) { 
        Vector vector = new Vector();
        if (list != null) {
            vector.addAll(list);
        }
        return vector;
    }

    /**
     * Convents a list to ArrayList instance to apply some  windchill instances.
     *
     * @param list a list
     * @return an ArrayList instance.
     */
    @SuppressWarnings("unchecked")
    public static ArrayList toArrayList(List list) { 
        if (list instanceof ArrayList) {
            return (ArrayList) list;
        }

        ArrayList result = new ArrayList();
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * Convents a map to Hashtable instance to apply some windchill instances.
     *
     * @param map the given map
     * @return an Hashtable instance.
     */
    @SuppressWarnings("unchecked")
    public static Hashtable toHashtable(Map map) { 
        Hashtable result = new Hashtable(); 
        if (map != null) {
            result.putAll(map);
        }
        return result;
    }

    /**
     * Convents a map to HashMap instance to apply some windchill instances.
     *
     * @param map the given map
     * @return an HashMap instance.
     */
    @SuppressWarnings("unchecked")
    public static HashMap toHashMap(Map map) { 
        if (map instanceof HashMap) {
            return (HashMap) map;
        }

        HashMap result = new HashMap();
        if (map != null) {
            result.putAll(map);
        }
        return result;
    }

    /**
     * Judge if the given list contains all the elements in the given array
     *
     * @param list  list
     * @param array array
     * @return is contain or not
     */
    public static boolean containsAll(List<String> list, String[] array) {
        boolean isContainsAll = true;
        if (list != null && array != null && list.size() >= array.length) {
            int size = array.length;
            String str;
            for (int i = 0; i < size; i++) {
                str = array[i];
                if (!list.contains(str)) {
                    isContainsAll = false;
                    break;
                }
            }
        } else {
            isContainsAll = false;
        }
        return isContainsAll;
    }

    /**
     * get the type Hashtable from object
     *
     * @param obj object
     * @return hash table instance
     */
    public static Hashtable getHashTable(Object obj) {
        return (Hashtable) obj;
    }

    /**
     * Remove duplicate and convert to set
     *
     * @param array array
     * @return set
     */
    public static Set convertArrayToSet(Object[] array) {
        Set set = new HashSet();
        if (array != null) {
            int size = array.length;
            Object obj;
            for (int i = 0; i < size; i++) {
                obj = array[i];
                set.add(obj);
            }
        }
        return set;
    }
}