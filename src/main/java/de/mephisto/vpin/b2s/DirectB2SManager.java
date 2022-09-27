package de.mephisto.vpin.b2s;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.VPinServiceException;
import de.mephisto.vpin.util.ImageCropper;
import de.mephisto.vpin.util.ImageUtil;
import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DirectB2SManager {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SManager.class);

  private VPinService service;

  public DirectB2SManager(VPinService service) {
    this.service = service;
  }

  public File getB2SImage(GameInfo game, B2SImageRatio ratio) throws VPinServiceException {
    try {
      String targetName = FilenameUtils.getBaseName(game.getGameFileName()) + ".png";
      File target = new File(SystemInfo.getInstance().getB2SImageExtractionFolder(), targetName);
      if (target.exists()) {
        return target;
      }

      if (game.getDirectB2SFile().exists()) {
        B2SThumbnailExtractor extractor = new B2SThumbnailExtractor(game);
        File file = extractor.extractImage(game.getDirectB2SFile());
        if (file != null && ratio != null) {
          ImageCropper cropper = new ImageCropper(file);
          BufferedImage crop = cropper.crop(ratio.getXRatio(), ratio.getYRatio());
          BufferedImage resized = ImageUtil.resizeImage(crop, 1280, 1280 / ratio.getXRatio() * ratio.getYRatio());
          ImageUtil.write(resized, target);
          file.delete();
          return target;
        }
        return file;
      }
    } catch (IOException e) {
      LOG.error("Error extracting directb2s image: " + e.getMessage(), e);
      throw new VPinServiceException(e);
    } catch (Exception e) {
      throw new VPinServiceException(e);
    }
    return null;
  }
}
