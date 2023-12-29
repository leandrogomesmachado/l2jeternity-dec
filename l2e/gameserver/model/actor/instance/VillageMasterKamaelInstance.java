package l2e.gameserver.model.actor.instance;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.QuestState;

public final class VillageMasterKamaelInstance extends VillageMasterInstance {
   public VillageMasterKamaelInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   protected final String getSubClassMenu(Race race) {
      return !Config.ALT_GAME_SUBCLASS_EVERYWHERE && !Config.ALT_GAME_SUBCLASS_ALL_CLASSES && race != Race.Kamael
         ? "data/html/villagemaster/SubClass_NoKamael.htm"
         : "data/html/villagemaster/SubClass.htm";
   }

   @Override
   protected final String getSubClassFail() {
      return "data/html/villagemaster/SubClass_Fail_Kamael.htm";
   }

   @Override
   protected boolean checkQuests(Player player) {
      if (player.isNoble()) {
         return true;
      } else {
         QuestState qs = player.getQuestState("_234_FatesWhisper");
         if (qs != null && qs.isCompleted()) {
            qs = player.getQuestState("_236_SeedsOfChaos");
            return qs != null && qs.isCompleted();
         } else {
            return false;
         }
      }
   }

   @Override
   protected boolean checkVillageMasterRace(PlayerClass pclass) {
      return pclass == null ? false : pclass.isOfRace(Race.Kamael);
   }
}
