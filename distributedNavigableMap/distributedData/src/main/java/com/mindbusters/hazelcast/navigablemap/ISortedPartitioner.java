package com.mindbusters.hazelcast.navigablemap;

import java.io.Serializable;

/**
 * Created by kic on 01.06.15.
 */
public interface ISortedPartitioner<K extends Comparable> extends Serializable {
    K getPartitionKey(Object mapKey);
}
