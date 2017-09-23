package com.mindbusters.hazelcast.navigablemap;

import com.hazelcast.core.IExecutorService;
import com.mindbusters.hazelcast.navigablemap.callables.SetPartitionData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by kic on 01.06.15.
 */
public abstract class AbstractNavigableMapStore<P extends Comparable, K, V> implements Serializable {
    public abstract void loadRange(K fromKey, K toKey, boolean inclusiveFrom, boolean inclusiveTo, ISortedPartitioner<P> partitioner);
    private String mapName;
    private IExecutorService executorService;
    private List<Future<Boolean>> emittedPartitionFutures = new ArrayList<>();

    public void emitPartition(P partitionKey, Map<K, V> partitionData) {
        // send to partition owner
        emittedPartitionFutures.add(
                executorService.submitToKeyOwner(new SetPartitionData(mapName, partitionKey, partitionData), partitionKey)
        );
    }

    public void setExecutorService(String mapName, IExecutorService executorService) {
        this.mapName = mapName;
        this.executorService = executorService;
    }

    public void waitForDone() {
        for (Future<Boolean> f : emittedPartitionFutures) {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
