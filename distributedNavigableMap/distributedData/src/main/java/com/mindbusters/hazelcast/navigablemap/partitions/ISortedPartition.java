package com.mindbusters.hazelcast.navigablemap.partitions;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by kic on 01.06.15.
 */
public interface ISortedPartition<K extends Comparable, V> extends Serializable {
    K firstKey();

    K lastKey();

    V put(K key, V value);

    Map<? extends K, ? extends V> getDataMap();

    void putAll(Map<K, V> partitionData);

    int size();
}
