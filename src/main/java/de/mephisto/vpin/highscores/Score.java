package de.mephisto.vpin.highscores;

import org.apache.commons.lang3.StringUtils;

public class Score {
  private String userInitials = "???";
  private String score;
  private int position;

  public Score(String userInitials, String score, int position) {
    this.userInitials = userInitials;
    this.score = score;
    this.position = position;
  }

  public String getUserInitials() {
    return userInitials;
  }

  public void setUserInitials(String userInitials) {
    if(!StringUtils.isEmpty(userInitials)) {
      this.userInitials = userInitials;
    }
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }
}
