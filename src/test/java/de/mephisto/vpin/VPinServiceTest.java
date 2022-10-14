package de.mephisto.vpin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
    service.shutdown();
  }

  @Test
  public void testTableRepositoryGetGamesWithoutRoms() throws VPinServiceException {
    VPinService service = VPinService.create(true);
    List<GameInfo> tables = service.getGamesWithEmptyRoms();
    assertFalse(tables.isEmpty());
    service.shutdown();
  }

//  @Test
//  public void testProcesses() {
//    ProcessHandle.allProcesses()
//        .forEach(process -> System.out.println(processDetails(process)));
//  }

  private static String processDetails(ProcessHandle process) {
    return String.format("%8d %8s %10s %26s %-40s",
        process.pid(),
        text(process.parent().map(ProcessHandle::pid)),
        text(process.info().user()),
        text(process.info().command()),
        text(process.info().commandLine()));
  }

  private static String text(Optional<?> optional) {
    return optional.map(Object::toString).orElse("-");
  }
}
