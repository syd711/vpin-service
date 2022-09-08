package de.mephisto.vpin.highscores;

import de.mephisto.vpin.games.GameInfo;
import de.mephisto.vpin.games.GameRepository;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class HighscoreManager {

  private final Cache<Integer, Highscore> cache;
  private HighscoreResolver highscoreResolver;

  private final HighscoreFilesWatcher highscoreWatcher;
  private GameRepository gameRepository;

  public HighscoreManager(GameRepository gameRepository) {
    this.gameRepository = gameRepository;

    this.highscoreResolver = new HighscoreResolver();
    this.highscoreResolver.refresh();

    SystemInfo info = SystemInfo.getInstance();
    List<File> watching = Arrays.asList(info.getNvramFolder(), info.getVPRegFile().getParentFile());
    this.highscoreWatcher = new HighscoreFilesWatcher(gameRepository, this, watching);
    this.highscoreWatcher.start();

    CachingProvider cachingProvider = Caching.getCachingProvider();
    CacheManager cacheManager = cachingProvider.getCacheManager();
    MutableConfiguration<Integer, Highscore> config = new MutableConfiguration<>();
    cache = cacheManager.createCache("highscoreCache", config);
  }

  public void destroy() {
    this.highscoreWatcher.setRunning(false);
  }

  public Highscore getHighscore(GameInfo game, boolean reload) {
    if(reload) {
      cache.getAndRemove(game.getId());
    }

    if(StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    Highscore highscore = cache.get(game.getId());
    if(highscore == null) {
      highscore = highscoreResolver.loadHighscore(game);
      if(highscore != null) {
        cache.put(game.getId(), highscore);
      }
    }

    return cache.get(game.getId());
  }

  public void invalidateHighscore(GameInfo game) {
    cache.getAndRemove(game.getId());
  }

  public void refreshHighscores() {
    this.highscoreResolver.refresh();
  }
}
