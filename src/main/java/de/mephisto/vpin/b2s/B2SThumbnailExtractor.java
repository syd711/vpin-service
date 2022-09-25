package de.mephisto.vpin.b2s;

import de.mephisto.vpin.util.SystemInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;

public class B2SThumbnailExtractor extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(B2SThumbnailExtractor.class);

  private String imageData;

  public B2SThumbnailExtractor() {
    File vpxTablesFolder = SystemInfo.getInstance().getVPXTablesFolder();
    File[] files = vpxTablesFolder.listFiles((dir, name) -> name.endsWith(".directb2s"));
    for (File file : files) {
      LOG.info("Extracting image from " + file.getAbsolutePath());
      String name = FilenameUtils.getBaseName(file.getName());
      File target = new File("./" , name + ".png");
      parseImage(file, target);
    }
  }

  private File parseImage(File file, File target) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(file.getAbsolutePath(), this);

      byte[] bytes = DatatypeConverter.parseBase64Binary(imageData);
      FileOutputStream out = new FileOutputStream(target);
      IOUtils.write(bytes, out);
      out.close();
    } catch (Exception e) {
      LOG.error("Failed to parse directb2s file '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public void startElement(String uri, String lName, String qName, Attributes attr) {
    switch (qName) {
      case "BackglassImage": {
        this.imageData = attr.getValue("Value");
        break;
      }
    }
  }

  public static void main(String args[]) {
    new B2SThumbnailExtractor();
  }
}
