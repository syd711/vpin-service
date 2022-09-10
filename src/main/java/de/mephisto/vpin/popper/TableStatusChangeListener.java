package de.mephisto.vpin.popper;

public interface TableStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);
}
