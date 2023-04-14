package java.util;

public interface NavigableMap<K,V> extends SortedMap<K,V> {

    Map.Entry<K,V> lowerEntry(K key);

    K lowerKey(K key);

    Map.Entry<K,V> floorEntry(K key);

    K floorKey(K key);

    /**
     * 返回等于或最接近最小值的K,V
     * @param key
     * @return
     */
    Map.Entry<K,V> ceilingEntry(K key);

    K ceilingKey(K key);

    Map.Entry<K,V> higherEntry(K key);

    K higherKey(K key);

    Map.Entry<K,V> firstEntry();

    Map.Entry<K,V> lastEntry();

    Map.Entry<K,V> pollFirstEntry();

    Map.Entry<K,V> pollLastEntry();

    /**
     * 降序Map
     */
    NavigableMap<K,V> descendingMap();

    /**
     * 升序K
     */
    NavigableSet<K> navigableKeySet();

    /**
     * 降序K
     */
    NavigableSet<K> descendingKeySet();

    NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                             K toKey,   boolean toInclusive);

    NavigableMap<K,V> headMap(K toKey, boolean inclusive);

    NavigableMap<K,V> tailMap(K fromKey, boolean inclusive);

    SortedMap<K,V> subMap(K fromKey, K toKey);

    SortedMap<K,V> headMap(K toKey);

    SortedMap<K,V> tailMap(K fromKey);
}
