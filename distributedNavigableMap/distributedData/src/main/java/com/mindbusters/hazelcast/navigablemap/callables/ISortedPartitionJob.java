package com.mindbusters.hazelcast.navigablemap.callables;

import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by kic on 02.06.15.
 */
public interface ISortedPartitionJob<P, C> extends Callable<C>, Serializable, HazelcastInstanceAware {
    ISortedPartitionJob setPartitionKey(P partitionKey);
}
