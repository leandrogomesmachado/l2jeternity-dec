package l2e.gameserver.model.olympiad;

public enum CompetitionType {
   CLASSED("classed"),
   NON_CLASSED("non-classed"),
   TEAMS("teams"),
   OTHER("other");

   private final String _name;

   private CompetitionType(String name) {
      this._name = name;
   }

   @Override
   public final String toString() {
      return this._name;
   }
}
