package l2e.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public final class OlympiadAnnouncer implements Runnable {
   private static final int OLY_MANAGER = 31688;
   private final List<Spawner> _managers = new ArrayList<>();
   private int _currentStadium = 0;

   public OlympiadAnnouncer() {
      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null && spawn.getId() == 31688) {
            this._managers.add(spawn);
         }
      }
   }

   @Override
   public void run() {
      for(int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; ++this._currentStadium) {
         if (this._currentStadium >= OlympiadGameManager.getInstance().getNumberOfStadiums()) {
            this._currentStadium = 0;
         }

         OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(this._currentStadium);
         if (task != null && task.getGame() != null && task.needAnnounce()) {
            String arenaId = String.valueOf(task.getGame().getStadiumId() + 1);
            NpcStringId npcString;
            switch(task.getGame().getType()) {
               case NON_CLASSED:
                  npcString = NpcStringId.OLYMPIAD_CLASS_FREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
                  break;
               case CLASSED:
                  npcString = NpcStringId.OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
                  break;
               case TEAMS:
                  npcString = NpcStringId.OLYMPIAD_CLASS_FREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
                  break;
               default:
                  continue;
            }

            for(Spawner spawn : this._managers) {
               Npc manager = spawn.getLastSpawn();
               if (manager != null) {
                  NpcSay packet = new NpcSay(manager.getObjectId(), 23, manager.getId(), npcString);
                  packet.addStringParameter(arenaId);
                  manager.broadcastPacket(packet);
               }
            }
            break;
         }
      }
   }
}
