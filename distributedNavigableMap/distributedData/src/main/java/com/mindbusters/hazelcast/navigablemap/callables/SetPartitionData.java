package com.mindbusters.hazelcast.navigablemap.callables;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.mindbusters.hazelcast.navigablemap.partitions.ISortedPartition;
import com.mindbusters.hazelcast.navigablemap.partitions.ConcurrentSkipListPartition;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by kic on 01.06.15.
 */
public class SetPartitionData<P extends Comparable, K extends Comparable, V> implements Callable<Boolean>, Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = 3116338354030234711L;
    private transient HazelcastInstance hazelcastInstance;
    private final String mapName;
    private final P partitionKey;
    private final Map<K, V> partitionData;

    public SetPartitionData(String mapName, P partitionKey, Map<K, V> partitionData) {
        this.mapName = mapName;
        this.partitionKey = partitionKey;
        this.partitionData = partitionData;
    }

    @Override
    public Boolean call() throws Exception {
        IMap<P, ISortedPartition<K,V>> map = hazelcastInstance.getMap(mapName);

        map.lock(partitionKey);
        try {

            ISortedPartition<K, V> sortedPartition = map.get(partitionKey);

            // get partition data
            if (sortedPartition == null) map.put(partitionKey, sortedPartition = new ConcurrentSkipListPartition<>());

            // add data
            sortedPartition.putAll(partitionData);

            // TODO remove print
            System.out.println("add: " + partitionData);

            // note we still need to lock the key, evan if we are on the keyowners host
            map.set(partitionKey, sortedPartition);
        } finally {
            map.unlock(partitionKey);
        }

        return true;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
