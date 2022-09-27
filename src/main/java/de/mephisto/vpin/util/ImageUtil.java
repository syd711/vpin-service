package de.mephisto.vpin.util;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ImageUtil.class);

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
    return Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
  }

  public static void write(BufferedImage image, File file) throws IOException {
    if(file.getName().endsWith(".png")) {
      writePNG(image, file);
    }
    if(file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
      writeJPG(image, file);
    }
  }

  private static void writeJPG(BufferedImage image, File file) throws IOException {
    long writeDuration = System.currentTimeMillis();
    BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    ImageIO.write(image, "JPG", imageOutputStream);
    imageOutputStream.close();
    long duration = System.currentTimeMillis() - writeDuration;
    LOG.info("Writing " + file.getAbsolutePath() + " took " + duration + "ms.");
  }

  private static void writePNG(BufferedImage image, File file) throws IOException {
    long writeDuration = System.currentTimeMillis();
    BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    ImageIO.write(image, "PNG", imageOutputStream);
    imageOutputStream.close();
    long duration = System.currentTimeMillis() - writeDuration;
    LOG.info("Writing " + file.getAbsolutePath() + " took " + duration + "ms.");
  }
}
