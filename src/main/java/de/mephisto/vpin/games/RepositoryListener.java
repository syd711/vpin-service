package de.mephisto.vpin.games;

public interface RepositoryListener {

  void gameScanned(GameInfo info);

  void highscoreChanged();

  void notifyRomChanged();
}
