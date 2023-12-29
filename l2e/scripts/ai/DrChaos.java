package l2e.scripts.ai;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;

public class DrChaos extends Quest {
   private static final int DOCTER_CHAOS = 32033;
   private static final int STRANGE_MACHINE = 32032;
   private static final int CHAOS_GOLEM = 25703;
   private static boolean _IsGolemSpawned;

   public DrChaos(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32033);
      _IsGolemSpawned = false;
   }

   public Npc findTemplate(int npcId) {
      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null && spawn.getId() == npcId) {
            return spawn.getLastSpawn();
         }
      }

      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("1")) {
         Npc machine_instance = this.findTemplate(32032);
         if (machine_instance != null) {
            npc.getAI().setIntention(CtrlIntention.ATTACK, machine_instance);
            machine_instance.broadcastPacket(new SpecialCamera(machine_instance, 1, -200, 15, 10000, 1000, 20000, 0, 0, 0, 0, 0));
         } else {
            this.startQuestTimer("2", 2000L, npc, player);
         }

         this.startQuestTimer("3", 10000L, npc, player);
      } else if (event.equalsIgnoreCase("2")) {
         npc.broadcastSocialAction(3);
      } else if (event.equalsIgnoreCase("3")) {
         npc.broadcastPacket(new SpecialCamera(npc, 1, -150, 10, 3000, 1000, 20000, 0, 0, 0, 0, 0));
         this.startQuestTimer("4", 2500L, npc, player);
      } else if (event.equalsIgnoreCase("4")) {
         npc.getAI().setIntention(CtrlIntention.MOVING, new Location(96055, -110759, -3312, 0));
         this.startQuestTimer("5", 2000L, npc, player);
      } else if (event.equalsIgnoreCase("5")) {
         player.teleToLocation(94832, -112624, -3304, true);
         npc.teleToLocation(-113091, -243942, -15536, true);
         if (!_IsGolemSpawned) {
            Npc golem = addSpawn(25703, 94640, -112496, -3336, 0, false, 0L);
            _IsGolemSpawned = true;
            this.startQuestTimer("6", 1000L, golem, player);
            player.sendPacket(new PlaySound(1, "Rm03_A", 0, 0, 0, 0, 0));
         }
      } else if (event.equalsIgnoreCase("6")) {
         npc.broadcastPacket(new SpecialCamera(npc, 30, -200, 20, 6000, 700, 8000, 0, 0, 0, 0, 0));
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getId() == 32033) {
         npc.getAI().setIntention(CtrlIntention.MOVING, new Location(96323, -110914, -3328, 0));
         this.startQuestTimer("1", 3000L, npc, player);
      }

      return "";
   }

   public static void main(String[] args) {
      new DrChaos(-1, "DrChaos", "ai");
   }
}
