package de.mephisto.vpin.highscores;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HighscoreManager {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreManager.class);

  private final Map<Integer, Highscore> cache = new HashMap<>();
  private final HighscoreResolver highscoreResolver;

  private final VPinService VPinService;

  public HighscoreManager(VPinService service) {
    this.VPinService = service;

    this.highscoreResolver = new HighscoreResolver();
  }

  public Highscore getHighscore(GameInfo game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    if (!cache.containsKey(game.getId())) {
      Highscore highscore = highscoreResolver.loadHighscore(game);
      cache.put(game.getId(), highscore);
    }

    return cache.get(game.getId());
  }

  public void invalidateHighscore(GameInfo game) {
    highscoreResolver.refresh();
    cache.remove(game.getId());
    LOG.info("Invalidated cached highscore of " + game);
  }
}
