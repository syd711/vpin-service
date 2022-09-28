package de.mephisto.vpin;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface VPinServiceEvent {

  @NonNull
  GameInfo getGameInfo();
}
