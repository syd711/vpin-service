package de.mephisto.vpin.dof;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DOFUnitsTest {
  private final static Logger LOG = LoggerFactory.getLogger(DOFUnitsTest.class);

  private final static String WIZ_TEST = "  LedWiz unit 1: name pins outputs";
  private final static String PINSCAPE_TEST = "  Pinscape unit 1: name pins outputs";

  @Test
  public void testUnitParsing() {
    Unit unit = DOFCommandExecutor.parseUnit(WIZ_TEST);
    assertNotNull(unit);
    unit = DOFCommandExecutor.parseUnit(PINSCAPE_TEST);
    assertNotNull(unit);
  }

}
