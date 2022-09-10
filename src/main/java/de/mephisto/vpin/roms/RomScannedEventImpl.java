package de.mephisto.vpin.roms;

import de.mephisto.vpin.GameInfo;

public class RomScannedEventImpl implements RomScannedEvent {

  private GameInfo gameInfo;

  public RomScannedEventImpl(GameInfo gameInfo) {
    this.gameInfo = gameInfo;
  }

  @Override
  public GameInfo getGameInfo() {
    return gameInfo;
  }
}
