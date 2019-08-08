package org.tron.common.utils;

import java.util.Arrays;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.config.args.Parameter.ForkBlockVersionConsts;
import org.tron.core.config.args.Parameter.ForkBlockVersionEnum;
import org.tron.core.store.DynamicPropertiesStore;

@Slf4j(topic = "utils")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ForkUtils {

  private static final byte VERSION_DOWNGRADE = (byte) 0;
  private static final byte VERSION_UPGRADE = (byte) 1;
  private static final byte[] check;

  static {
    check = new byte[1024];
    Arrays.fill(check, VERSION_UPGRADE);
  }

  @Getter
  private DynamicPropertiesStore dynamicPropertiesStore;

  public void init(DynamicPropertiesStore dynamicPropertiesStore) {
    this.dynamicPropertiesStore = dynamicPropertiesStore;
  }

  public boolean pass(ForkBlockVersionEnum forkBlockVersionEnum) {
    return pass(forkBlockVersionEnum.getValue());
  }

  public synchronized boolean pass(int version) {
    if (version == ForkBlockVersionConsts.ENERGY_LIMIT) {
      return checkForEnergyLimit();
    }

    byte[] stats = dynamicPropertiesStore.statsByVersion(version);
    return check(stats);
  }

  // when block.version = 5,
  // it make block use new energy to handle transaction when block number >= 4727890L.
  // version !=5, skip this.
  private boolean checkForEnergyLimit() {
    long blockNum = dynamicPropertiesStore.getLatestBlockHeaderNumber();
    return blockNum >= DBConfig.getBlockNumForEneryLimit();
  }

  private boolean check(byte[] stats) {
    if (stats == null || stats.length == 0) {
      return false;
    }

    for (int i = 0; i < stats.length; i++) {
      if (check[i] != stats[i]) {
        return false;
      }
    }

    return true;
  }

  private void downgrade(int version, int slot) {
    for (ForkBlockVersionEnum versionEnum : ForkBlockVersionEnum.values()) {
      int versionValue = versionEnum.getValue();
      if (versionValue > version) {
        byte[] stats = dynamicPropertiesStore.statsByVersion(versionValue);
        if (!check(stats) && Objects.nonNull(stats)) {
          stats[slot] = VERSION_DOWNGRADE;
         dynamicPropertiesStore.statsByVersion(versionValue, stats);
        }
      }
    }
  }

  private void upgrade(int version, int slotSize) {
    for (ForkBlockVersionEnum versionEnum : ForkBlockVersionEnum.values()) {
      int versionValue = versionEnum.getValue();
      if (versionValue < version) {
        byte[] stats = dynamicPropertiesStore.statsByVersion(versionValue);
        if (!check(stats)) {
          if (stats == null || stats.length == 0) {
            stats = new byte[slotSize];
          }
          Arrays.fill(stats, VERSION_UPGRADE);
          dynamicPropertiesStore.statsByVersion(versionValue, stats);
        }
      }
    }
  }

  public synchronized void reset() {
    for (ForkBlockVersionEnum versionEnum : ForkBlockVersionEnum.values()) {
      int versionValue = versionEnum.getValue();
      byte[] stats = dynamicPropertiesStore.statsByVersion(versionValue);
      if (!check(stats) && Objects.nonNull(stats)) {
        Arrays.fill(stats, VERSION_DOWNGRADE);
        dynamicPropertiesStore.statsByVersion(versionValue, stats);
      }
    }
  }

  public static ForkUtils instance() {
    return ForkControllerEnum.INSTANCE.getInstance();
  }

  private enum ForkControllerEnum {
    INSTANCE;

    private ForkUtils instance;

    ForkControllerEnum() {
      instance = new ForkUtils();
    }

    private ForkUtils getInstance() {
      return instance;
    }
  }
}