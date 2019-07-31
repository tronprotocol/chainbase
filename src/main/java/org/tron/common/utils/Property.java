package org.tron.common.utils;

import lombok.Getter;
import org.iq80.leveldb.Options;

public class Property {
  private String name;

  @Getter
  private String path;

  @Getter
  private Options dbOptions;
}
