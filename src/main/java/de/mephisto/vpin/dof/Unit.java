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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Unit unit = (Unit) o;

    if (id != unit.id) return false;
    return unitType == unit.unitType;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (unitType != null ? unitType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return unitType + " (ID " + id + ")";
  }
}
