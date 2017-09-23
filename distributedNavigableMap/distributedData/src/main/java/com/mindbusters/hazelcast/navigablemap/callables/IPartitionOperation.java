package com.mindbusters.hazelcast.navigablemap.callables;

import com.mindbusters.hazelcast.navigablemap.partitions.ISortedPartition;

import java.io.Serializable;

/**
 * Created by kic on 02.06.15.
 */
public interface IPartitionOperation<K extends Comparable, V> extends Serializable {
    Object call(ISortedPartition<K, V> partitionData);
}
