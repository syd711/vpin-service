package de.mephistor.pinup;

public class PinUPPopper {

  private SystemInfo systemInfo;
  private GameRepository tableRepository;
  private SqliteConnector sqliteConnector;

  private PinUPPopper() {
    //force factory
  }

  static PinUPPopper create() {
    PinUPPopper popper = new PinUPPopper();
    popper.init();
    return popper;
  }

  private void init() {
    systemInfo = new SystemInfo();
    sqliteConnector = new SqliteConnector(systemInfo);
    tableRepository = new GameRepository(systemInfo, sqliteConnector);
  }
}
