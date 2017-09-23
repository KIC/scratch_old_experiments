package com.mindbusters.hazelcast.navigablemap;

import com.hazelcast.core.HazelcastInstance;

import java.io.Serializable;

/**
 * Created by kic on 01.06.15.
 */
public interface IHazelcastInstanceFactory extends Serializable {
    public HazelcastInstance getInstance();
}
