package de.mephisto.vpin;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VPinServiceTest {
  @Test
  public void testTableRepository() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGameInfos();
    for (GameInfo table : tables) {
      assertTrue(table.getGameFile().exists());
    }
  }

  @Test
  public void testTableRepositoryGetGamesWithoutRoms() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGamesWithEmptyRoms();
    assertFalse(tables.isEmpty());
  }
}
