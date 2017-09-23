package com.mindbusters.hazelcast.navigablemap.callables;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.mindbusters.hazelcast.navigablemap.partitions.ISortedPartition;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by kic on 01.06.15.
 */
public class SetKeyValue<P, K extends Comparable, V> implements Callable<V>, Serializable, HazelcastInstanceAware {
    private static final long serialVersionUID = -559688281000822030L;
    private transient HazelcastInstance hazelcastInstance;
    private final String iMapName;
    private final P partitonKey;
    private final K key;
    private final V value;
    private final boolean sendResult;

    public SetKeyValue(String iMapName, P partitonKey, K key, V value, boolean sendResult) {
        this.iMapName = iMapName;
        this.partitonKey = partitonKey;
        this.key = key;
        this.value = value;
        this.sendResult = sendResult;
    }

    @Override
    public V call() throws Exception {
        Object oldKey = null;
        IMap<P, ISortedPartition<K,V>> imap = hazelcastInstance.getMap(iMapName);
        V oldValue = null;

        imap.lock(partitonKey);
        try {
            ISortedPartition<K, V> sortedPartition = imap.get(partitonKey);

            oldValue = sortedPartition.put(key, value);
        } finally {
            imap.unlock(partitonKey);
        }

        return sendResult ? oldValue : null;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
