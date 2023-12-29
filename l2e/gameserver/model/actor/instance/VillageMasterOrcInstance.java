package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;

public final class VillageMasterOrcInstance extends VillageMasterInstance {
   public VillageMasterOrcInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   protected boolean checkVillageMasterRace(PlayerClass pclass) {
      return pclass == null ? false : pclass.isOfRace(Race.Orc);
   }
}
