package de.mephisto.vpin.popper;

import de.mephisto.vpin.util.SqliteConnector;
import de.mephisto.vpin.util.SystemInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionsTest {

  @Test
  public void testFunctions() {
    SqliteConnector connector = new SqliteConnector();
    List<PinUPFunction> functions = connector.getFunctions();
    assertFalse(functions.isEmpty());
  }

  @Test
  public void testFunction() {
    SqliteConnector connector = new SqliteConnector();
    PinUPFunction function = connector.getFunction(PinUPFunction.FUNCTION_SHOW_FLYER);
    assertNotNull(function);
  }

}
