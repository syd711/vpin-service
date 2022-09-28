package de.mephisto.vpin.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageCropper {

  private BufferedImage image;

  public ImageCropper(File file) throws IOException {
    this(ImageIO.read(file));
  }

  public ImageCropper(BufferedImage image) {
    this.image = image;
  }

  public BufferedImage crop(int xRatio, int yRatio) {
    int width = image.getWidth();
    int height = image.getHeight();

    int targetWidth = width;
    int targetHeight = width / xRatio * yRatio;
    if (targetHeight > height) {
      targetWidth = image.getHeight() / yRatio * xRatio;
      targetHeight = height;
    }

    int x = 0;
    int y = 0;
    if (targetWidth < width) {
      x = (width / 2) - (targetWidth / 2);
    }

    return image.getSubimage(x, y, targetWidth, targetHeight);
  }
}
