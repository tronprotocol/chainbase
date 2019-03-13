package org.tron.core.db2;

import java.util.Map;
import org.tron.common.WrappedByteArray;

public interface Flusher {
  void flush(Map<WrappedByteArray, WrappedByteArray> batch);

  void close();

  void reset();
}
