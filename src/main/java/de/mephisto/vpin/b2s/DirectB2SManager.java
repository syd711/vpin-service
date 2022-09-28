package de.mephisto.vpin.b2s;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinServiceException;
import de.mephisto.vpin.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DirectB2SManager {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SManager.class);

  @Nullable
  public File extractDirectB2SBackgroundImage(@NonNull GameInfo game) throws VPinServiceException {
    if (game.getDirectB2SFile().exists()) {
      B2SThumbnailExtractor extractor = new B2SThumbnailExtractor(game);
      return extractor.extractImage(game.getDirectB2SFile());
    }
    return null;
  }

  public void generateB2SImage(@NonNull GameInfo game, @NonNull B2SImageRatio ratio, int cropWidth) throws VPinServiceException {
    try {
      if (game.getDirectB2SFile().exists()) {
        B2SThumbnailExtractor extractor = new B2SThumbnailExtractor(game);
        File file = extractor.extractImage(game.getDirectB2SFile());
        if (file != null) {
          BufferedImage image = ImageIO.read(file);
          BufferedImage crop = ImageUtil.crop(image, ratio.getXRatio(), ratio.getYRatio());
          BufferedImage resized = ImageUtil.resizeImage(crop, cropWidth, cropWidth / ratio.getXRatio() * ratio.getYRatio());
          ImageUtil.write(resized, game.getDirectB2SImage());
          file.delete();
        }
      }
    } catch (IOException e) {
      LOG.error("Error extracting directb2s image: " + e.getMessage(), e);
      throw new VPinServiceException(e);
    } catch (Exception e) {
      throw new VPinServiceException(e);
    }
  }
}
