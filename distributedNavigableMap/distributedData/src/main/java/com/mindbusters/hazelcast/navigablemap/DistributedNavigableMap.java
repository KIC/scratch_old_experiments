package com.mindbusters.hazelcast.navigablemap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.mindbusters.hazelcast.navigablemap.callables.*;
import com.mindbusters.hazelcast.navigablemap.partitions.ISortedPartition;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by kic on 01.06.15.
 */
public class DistributedNavigableMap<P extends Comparable, K extends Comparable, V> implements Serializable {
    private static final long serialVersionUID = 7539672025665201230L;
    private static final String DISTRUBUTED_ACCESSOR_NAME = "distributedAccessor";
    private transient IMap<P, ISortedPartition<K, V>> sortedPartitions = null;
    private transient HazelcastInstance hazelcastInstance = null;
    private final String mapName;
    private final IHazelcastInstanceFactory hazelcastInstanceFactory;
    private final ISortedPartitioner<P> partitioner;
    private AbstractNavigableMapStore mapStore = null;

    public DistributedNavigableMap(String mapName, IHazelcastInstanceFactory hazelcastInstanceFactory, ISortedPartitioner<P> partitioner) {
        this.mapName = mapName;
        this.hazelcastInstanceFactory = hazelcastInstanceFactory;
        this.partitioner = partitioner;
    }

    public V put(K key, V value) {
        // calculate partition key and send data to partition key owner and add value there
        P partitionKey = partitioner.getPartitionKey(key);

        // do this because the partition key owner can hold the sorted partition data as object
        // and does not need to deserialize / serialize
        /*
        List<V> f = sentToPartitions(
                new SetKeyValue<P, K, V, V>(mapName, partitionKey, key, value, true),
                partitionKey);

        return  f != null && f.size()>0 ? (V) f.get(0) : null;
        */

        return null;
    }


    public Map<K, V> subMap(K fromKey, K toKey, boolean forceReLoad) {
        // get all partition-keys from IMap within the range fromKey, toKey
        TreeSet<P> sortedPartitionKeys = new TreeSet<>(getImap().keySet());

        // find missing from to key range (missing uppers, missing lowers)
        // FIXME we need the partitions  first last key not only the partionkey
        K minAvailKey = firstKey();
        K maxAvilKey = lastKey();

        // call mapstore on missing ranges (upper & lower or all in case of forceRelaod)
        if (mapStore != null) {
            mapStore.setExecutorService(
                    mapName,
                    getHazelcastInstance().getExecutorService(DISTRUBUTED_ACCESSOR_NAME)
            );

            if (forceReLoad || minAvailKey == null || maxAvilKey == null) {
                // fetch whole range
                mapStore.loadRange(fromKey, toKey, true, true, partitioner);
            } else {
                // fetch lower band
                if (fromKey.compareTo(minAvailKey) < 0) {
                    mapStore.loadRange(fromKey, minAvailKey, true, false, partitioner);
                }

                // fetch upper band
                if (toKey.compareTo(maxAvilKey) > 0) {
                    mapStore.loadRange(maxAvilKey, toKey, false, true, partitioner);
                }
            }

            // wait for all partition addeds to complete
            mapStore.waitForDone();
            sortedPartitionKeys = new TreeSet<>(getImap().keySet());
        }

        // get all partitions and copy them in a LinkedHashMap
        List<Map<K, V>> sortedPartitionList = sentToPartitions(
                new DoOnSortedPartition<P, K, V, Map<K, V>>(mapName, null, sp -> sp.getDataMap()),
                sortedPartitionKeys.toArray()
        );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map<K, V> m : sortedPartitionList) {
            result.putAll(m);
        }

        return result;
    }


    public K firstKey() {
        TreeSet<P> sortedPartitionKeys = new TreeSet<>(getImap().keySet());
        if (sortedPartitionKeys == null || sortedPartitionKeys.size()<1) return null;

        List keys = sentToPartitions(
                new DoOnSortedPartition<>(mapName, sortedPartitionKeys.first(), p -> p == null ? null : p.firstKey()),
                sortedPartitionKeys.first()
        );

        return keys.size() > 0 ? (K) keys.get(0) : null;
    }

    public K lastKey() {
        TreeSet<P> sortedPartitionKeys = new TreeSet<>(getImap().keySet());
        if (sortedPartitionKeys == null || sortedPartitionKeys.size()<1) return null;

        List<K> keys = sentToPartitions(
                new DoOnSortedPartition<P, K, V, K>(mapName, sortedPartitionKeys.last(), p -> p == null ? null : p.lastKey()),
                sortedPartitionKeys.last()
        );

        return keys.size() > 0 ? keys.get(0) : null;
    }


    public long size() {
        List<Integer> sizes = sentToPartitions(
                new DoOnSortedPartition<P, K, V, Integer>(mapName, null, sp -> sp.size()),
                sortedPartitions.keySet().toArray()
        );

        long result = 0;
        for (int s : sizes)
            if (s > 0) result += (int) s;

        return result;
    }

    public AbstractNavigableMapStore getMapStore() {
        return mapStore;
    }

    public void setMapStore(AbstractNavigableMapStore mapStore) {
        this.mapStore = mapStore;
    }

    private <C>List<C> sentToPartitions(ISortedPartitionJob<P, C> job, Object... partitionKeys) {
        List<Future<C>> futures = new ArrayList<>();
        List<C> result = new ArrayList<>();

        for (Object partitionKey : partitionKeys) {
            futures.add(
                    getHazelcastInstance().getExecutorService(DISTRUBUTED_ACCESSOR_NAME).submitToKeyOwner(
                            job.setPartitionKey((P) partitionKey),
                            partitionKey
                    )
            );
        }

        for (Future<C> f : futures) {
            try {
                result.add(f.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private IMap<P, ISortedPartition<K, V>> getImap() {
        return sortedPartitions != null ? sortedPartitions : (sortedPartitions = getHazelcastInstance().getMap(mapName));
    }

    private HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance != null ? hazelcastInstance : (hazelcastInstance = hazelcastInstanceFactory.getInstance());
    }
}
