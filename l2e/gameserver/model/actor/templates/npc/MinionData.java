package l2e.gameserver.model.actor.templates.npc;

import java.util.ArrayList;
import java.util.List;

public class MinionData {
   private List<MinionTemplate> _minions = new ArrayList<>();

   public MinionData(MinionTemplate template) {
      this._minions.add(template);
   }

   public MinionData(List<MinionTemplate> minions) {
      this._minions = minions;
   }

   public List<MinionTemplate> getMinions() {
      return this._minions;
   }
}
