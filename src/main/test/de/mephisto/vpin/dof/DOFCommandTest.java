package de.mephisto.vpin.dof;

import de.mephisto.vpin.util.SystemInfo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class DOFCommandTest {
  private final static Logger LOG = LoggerFactory.getLogger(DOFCommandTest.class);

  @Test
  public void testCmd() {
    DOFCommandResult execute = new DOFCommand(8, 3, true).execute();
    assertFalse(execute.isSuccessful());
    LOG.info(execute.getOutput());
  }

}
