package java.util;

/**
 * 通过比较器实现K的比较
 */
public interface SortedMap<K,V> extends Map<K,V> {

    /**
     * 比较器
     */
    Comparator<? super K> comparator();

    /**
     * 返回指定区间子集合
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

    /**
     * 返回小于K的子集合
     * @param toKey
     * @return
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * 返回大于K的字集合
     */
    SortedMap<K,V> tailMap(K fromKey);

    /**
     * 返回最小的K
     */
    K firstKey();

    /**
     * 返回最大的K
     */
    K lastKey();

    /**
     * K的Set集合
     */
    Set<K> keySet();

    /**
     * V的集合
     */
    Collection<V> values();

    /**
     * K,V的集合
     */
    Set<Map.Entry<K, V>> entrySet();
}
