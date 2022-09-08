package de.mephisto.vpin.games;

public class HighscoreChangedEventImpl implements HighscoreChangedEvent {

  private GameInfo gameInfo;

  public HighscoreChangedEventImpl(GameInfo gameInfo) {
    this.gameInfo = gameInfo;
  }

  @Override
  public GameInfo getGameInfo() {
    return gameInfo;
  }
}
