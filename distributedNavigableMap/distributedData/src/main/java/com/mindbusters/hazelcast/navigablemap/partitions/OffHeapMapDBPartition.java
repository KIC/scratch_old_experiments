package com.mindbusters.hazelcast.navigablemap.partitions;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by kic on 01.06.15.
 */
public class OffHeapMapDBPartition<K extends Comparable, V> implements ISortedPartition<K,V> {
    private static final long serialVersionUID = -3158441942676405723L;
    private final ConcurrentSkipListMap<K, V> sortedData = new ConcurrentSkipListMap<>();

    @Override
    public K firstKey() {
        return sortedData.firstKey();
    }

    @Override
    public K lastKey() {
        return sortedData.lastKey();
    }

    @Override
    public V put(K key, V value) {
        return sortedData.put(key, value);
    }

    @Override
    public Map<? extends K, ? extends V> getDataMap() {
        return sortedData;
    }

    @Override
    public void putAll(Map<K, V> partitionData) {
        sortedData.putAll(partitionData);
    }

    @Override
    public int size() {
        return 0;
    }
}
