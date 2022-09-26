package de.mephisto.vpin.popper;

import de.mephisto.vpin.roms.RomManager;
import de.mephisto.vpin.util.SqliteConnector;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FunctionsTest {

  @Test
  public void testFunctions() throws FileNotFoundException {
    SqliteConnector connector = new SqliteConnector(new RomManager());
    List<PinUPFunction> functions = connector.getFunctions();
    assertFalse(functions.isEmpty());
  }

  @Test
  public void testFunction() throws FileNotFoundException {
    SqliteConnector connector = new SqliteConnector(new RomManager());
    PinUPFunction function = connector.getFunction(PinUPFunction.FUNCTION_SHOW_FLYER);
    assertNotNull(function);
  }

}
