package de.mephisto.vpin.dof;

public class Unit {
  private int id;
  private UnitType unitType;

  public Unit(int id, UnitType unitType) {
    this.id = id;
    this.unitType = unitType;
  }

  public int getId() {
    return id;
  }

  public UnitType getUnitType() {
    return unitType;
  }

  @Override
  public String toString() {
    return unitType + " (ID " + id + ")";
  }
}
