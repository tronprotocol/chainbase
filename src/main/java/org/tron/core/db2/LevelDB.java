package org.tron.core.db2;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.iq80.leveldb.WriteOptions;
import org.tron.impl.LevelDbDataSourceImpl;
import org.tron.common.WrappedByteArray;
import org.tron.common.DBIterator;

public class LevelDB implements DB<byte[], byte[]>, Flusher, Instance<LevelDB> {
  @Getter
  private LevelDbDataSourceImpl db;
  private WriteOptions writeOptions;

  public LevelDB(LevelDbDataSourceImpl db, WriteOptions writeOptions) {
    this.db = db;
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

  @Override
  public LevelDB newInstance() {
    return new LevelDB(db.newInstance(), writeOptions);
  }
}
