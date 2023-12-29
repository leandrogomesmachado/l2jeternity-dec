package l2e.gameserver.model.actor.templates.npc;

import gnu.trove.list.array.TIntArrayList;
import l2e.commons.util.TroveUtils;

public class Faction {
   public static final String none = "none";
   public static final Faction NONE = new Faction("none");
   public final String _factionId;
   public int _factionRange;
   public TIntArrayList ignoreId = TroveUtils.EMPTY_INT_ARRAY_LIST;

   public Faction(String factionId) {
      this._factionId = factionId;
   }

   public String getName() {
      return this._factionId;
   }

   public void setRange(int factionRange) {
      this._factionRange = factionRange;
   }

   public int getRange() {
      return this._factionRange;
   }

   public void addIgnoreNpcId(int npcId) {
      if (this.ignoreId.isEmpty()) {
         this.ignoreId = new TIntArrayList();
      }

      this.ignoreId.add(npcId);
   }

   public boolean isIgnoreNpcId(int npcId) {
      return this.ignoreId.contains(npcId);
   }

   public boolean isNone() {
      return this._factionId.isEmpty() || this._factionId.equals("none");
   }

   public boolean equals(Faction faction) {
      return !this.isNone() && faction.getName().equalsIgnoreCase(this._factionId);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o.getClass() != this.getClass() ? false : this.equals((Faction)o);
      }
   }

   @Override
   public String toString() {
      return this.isNone() ? "none" : this._factionId;
   }
}
