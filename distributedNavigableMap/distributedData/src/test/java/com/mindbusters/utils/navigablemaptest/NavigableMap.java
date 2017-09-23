package com.mindbusters.utils.navigablemaptest;

import com.hazelcast.core.Hazelcast;
import com.mindbusters.hazelcast.navigablemap.DistributedNavigableMap;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by kic on 01.06.15.
 */
public class NavigableMap {
    public static void main(String[] args) throws Exception {

        DistributedNavigableMap<Integer, DateTime, Object> dnm = new DistributedNavigableMap<>(
                "DATASOURCES::TestMap",
                () -> Hazelcast.newHazelcastInstance(),
                k -> ((DateTime) k).getDayOfMonth()
        );

        DateTime from = new DateTime("2001-01-01");
        DateTime to = new DateTime("2001-02-01");

        dnm.setMapStore(new TestNavigableMapStore<Integer, DateTime, Object>());
        Map<DateTime, Object> dateTimeObjectMap = dnm.subMap(from, to, false);

        System.out.println("map size: " + dnm.size());

        System.out.println("submap: " + dateTimeObjectMap);
    }
}
