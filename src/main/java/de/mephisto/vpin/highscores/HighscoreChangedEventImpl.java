package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;

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
