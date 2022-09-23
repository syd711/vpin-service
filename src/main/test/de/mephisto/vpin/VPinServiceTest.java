package de.mephisto.vpin;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VPinServiceTest {
  private final static Logger LOG = LoggerFactory.getLogger(VPinServiceTest.class);

  @Test
  public void testTableRepository() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getGameFile().exists());
    }
  }

  @Test
  public void testTableRepositoryWithoutReset() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    assertFalse(service.getGameInfos().isEmpty());

    List<GameInfo> tables = service.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getGameFile().exists());
    }
    try {
      Thread.sleep(100000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    LOG.info("Loaded " + tables.size() + " tables.");

  }

  @Test
  public void testTableRepositoryGetGamesWithoutRoms() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGamesWithEmptyRoms();
    for (GameInfo table : tables) {
      LOG.info(table.getId() + ": " + table.getGameFile().getAbsolutePath());
    }
  }

  @Test
  public void testTableInvalidate() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGamesWithEmptyRoms();
    for (GameInfo table : tables) {
      if(table.getId() == 372) {
        LOG.info(table.getId() + ": " + table.getGameFile().getAbsolutePath());
        service.rescanRom(table);
      }
    }
  }
}
