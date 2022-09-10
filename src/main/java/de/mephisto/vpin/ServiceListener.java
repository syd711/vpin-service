package de.mephisto.vpin;

import de.mephisto.vpin.highscores.HighscoreChangedEvent;

public interface ServiceListener {

  void gameScanned(GameInfo info);

  void highscoreChanged(HighscoreChangedEvent event);
}
