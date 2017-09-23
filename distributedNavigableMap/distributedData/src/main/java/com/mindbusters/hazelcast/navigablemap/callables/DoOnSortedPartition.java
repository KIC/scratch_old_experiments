package com.mindbusters.hazelcast.navigablemap.callables;

import com.hazelcast.core.HazelcastInstance;
import com.mindbusters.hazelcast.navigablemap.partitions.ISortedPartition;

import java.io.Serializable;

/**
 * Created by kic on 01.06.15.
 */
public class DoOnSortedPartition<P, K extends Comparable, V, C> implements ISortedPartitionJob<P, C> {
    private static final long serialVersionUID = 1166249409139544900L;
    private transient HazelcastInstance hazelcastInstance;
    private final String iMapName;
    private final P partitionKey;
    private final IPartitionOperation<K, V> partitionOperation;

    public DoOnSortedPartition(String iMapName, P partitionKey, IPartitionOperation<K, V> partitionOperation) {
        this.iMapName = iMapName;
        this.partitionKey = partitionKey;
        this.partitionOperation = partitionOperation;
    }

    @Override
    public C call() throws Exception {
        if (iMapName == null) throw new RuntimeException("MapName must not be null!");
        if (partitionKey == null) throw new RuntimeException("PartitionKey must not be null!");
        if (partitionOperation == null) throw new RuntimeException("PartitionOperation must not be null!");

        return (C) partitionOperation.call((ISortedPartition<K, V>) hazelcastInstance.getMap(iMapName).get(partitionKey));
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public ISortedPartitionJob setPartitionKey(P partitionKey) {
        return new DoOnSortedPartition<P, K, V, C>(iMapName, partitionKey, partitionOperation);
    }
}
