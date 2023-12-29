package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.ClassType;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;

public final class VillageMasterFighterInstance extends VillageMasterInstance {
   public VillageMasterFighterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   protected boolean checkVillageMasterRace(PlayerClass pclass) {
      if (pclass == null) {
         return false;
      } else {
         return pclass.isOfRace(Race.Human) || pclass.isOfRace(Race.Elf);
      }
   }

   @Override
   protected boolean checkVillageMasterTeachType(PlayerClass pclass) {
      return pclass == null ? false : pclass.isOfType(ClassType.Fighter);
   }
}
