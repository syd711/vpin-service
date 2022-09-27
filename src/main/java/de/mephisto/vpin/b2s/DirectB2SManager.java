package de.mephisto.vpin.b2s;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.VPinServiceException;

import java.io.File;

public class DirectB2SManager {
  private VPinService service;

  public DirectB2SManager(VPinService service) {
    this.service = service;
  }

  public File getB2SImage(GameInfo game, B2SImageRatio ratio) throws VPinServiceException {
    if(game.getDirectB2SFile().exists()) {
      B2SThumbnailExtractor extractor = new B2SThumbnailExtractor(game);
      File file = extractor.extractImage(game.getDirectB2SFile());
      if(file != null) {

      }
      return file;
    }
    return null;
  }
}
