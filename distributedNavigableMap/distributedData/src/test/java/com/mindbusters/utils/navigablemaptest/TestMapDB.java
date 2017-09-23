package com.mindbusters.utils.navigablemaptest;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.*;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created by kic on 01.06.15.
 */
public class TestMapDB {
    public static void main(String[] args) {
        DB db = DBMaker.newMemoryDB().make();

        ConcurrentNavigableMap treeMap = db.getTreeMap("map");
        treeMap.put("something","here");

        serialize(treeMap);

        db.commit();
        db.close();
    }

    public static byte[] serialize(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;

        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
