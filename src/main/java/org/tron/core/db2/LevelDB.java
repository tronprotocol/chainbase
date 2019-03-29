package org.tron.core.db2;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteOptions;
import org.tron.common.LevelDbDataSourceImpl;
import org.tron.common.WrappedByteArray;
import org.tron.common.DBIterator;

public class LevelDB implements DB<byte[], byte[]>, Flusher {
  @Getter
  private LevelDbDataSourceImpl db;
  private WriteOptions writeOptions;

  public LevelDB(String parentName, String name, WriteOptions writeOptions, Options options) {
    db = new LevelDbDataSourceImpl(parentName, name, options);
    db.initDB();
    this.writeOptions = writeOptions;
  }

  @Override
  public byte[] get(byte[] key) {
    return db.getData(key);
  }

  @Override
  public void put(byte[] key, byte[] value) {
    db.putData(key, value);
  }

  @Override
  public long size() {
    return db.getTotal();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public void remove(byte[] key) {
    db.deleteData(key);
  }

  @Override
  public DBIterator iterator() {
    return db.iterator();
  }

  @Override
  public void flush(Map<WrappedByteArray, WrappedByteArray> batch) {
    Map<byte[], byte[]> rows = batch.entrySet().stream()
        .map(e -> Maps.immutableEntry(e.getKey().getBytes(), e.getValue().getBytes()))
        .collect(HashMap::new, (m, k) -> m.put(k.getKey(), k.getValue()), HashMap::putAll);
    db.updateByBatch(rows, writeOptions);
  }

  @Override
  public void close() {
    db.closeDB();
  }
  @Override
  public void reset() {
    db.resetDb();
  }
}
