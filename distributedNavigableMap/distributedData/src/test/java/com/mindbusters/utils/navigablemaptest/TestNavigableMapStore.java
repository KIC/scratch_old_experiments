package com.mindbusters.utils.navigablemaptest;

import com.mindbusters.hazelcast.navigablemap.AbstractNavigableMapStore;
import com.mindbusters.hazelcast.navigablemap.ISortedPartitioner;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kic on 01.06.15.
 */
public class TestNavigableMapStore<P extends Comparable, K, V> extends AbstractNavigableMapStore<P, K, V> {
    private static final long serialVersionUID = -954427995681480546L;
    private final Connection connection;
    private final PreparedStatement statement;

    public TestNavigableMapStore() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/yahoo?" +
                "user=root&password=xxxx");

        statement = connection.prepareStatement("select * from quote where tst between ? and ? order by tst");
    }

    @Override
    public void loadRange(Object fromKey, Object toKey, boolean inclusiveFrom, boolean inclusiveTo, ISortedPartitioner partitioner) {
        try {
            statement.setDate(1, new Date(((DateTime) fromKey).getMillis()));
            statement.setDate(2, new Date(((DateTime) toKey).getMillis()));
            ResultSet resultSet = statement.executeQuery();

            Comparable lastPartitionKey = null;
            Map<K, V> partition = null;

            while (resultSet.next()) {
                K date = (K) new DateTime(resultSet.getDate(1).getTime());
                V aDouble = (V) (Double) resultSet.getDouble(2);
                P partitionKey = (P) partitioner.getPartitionKey(date);

                if (!partitionKey.equals(lastPartitionKey)) {
                    if (partition != null) emitPartition(partitionKey, partition);
                    partition = new HashMap<>();
                }

                partition.put(date, aDouble);

                lastPartitionKey = partitionKey;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
