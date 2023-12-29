package l2e.scripts.hellbound;

import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Quarry extends Quest {
   private static final int SLAVE = 32299;
   private static final int TRUST = 50;
   private static final int ZONE = 40107;
   protected static final int[][] DROPLIST = new int[][]{{9628, 261}, {9630, 175}, {9629, 145}, {1876, 6667}, {1877, 1333}, {1874, 2222}};

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("time_limit")) {
         for(ZoneType zone : ZoneManager.getInstance().getZones(npc)) {
            if (zone.getId() == 40108) {
               npc.setTarget(null);
               npc.getAI().setIntention(CtrlIntention.ACTIVE);
               npc.setAutoAttackable(false);
               npc.setRHandId(0);
               npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), true);
               return null;
            }
         }

         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.HUN_HUNGRY), 2000);
         npc.doDie(npc);
         return null;
      } else if (event.equalsIgnoreCase("FollowMe")) {
         npc.getAI().setIntention(CtrlIntention.FOLLOW, player);
         npc.setTarget(player);
         npc.setAutoAttackable(true);
         npc.setRHandId(9136);
         npc.setWalking();
         if (this.getQuestTimer("time_limit", npc, null) == null) {
            this.startQuestTimer("time_limit", 900000L, npc, null);
         }

         return "32299-02.htm";
      } else {
         return event;
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      npc.setAutoAttackable(false);
      if (npc instanceof QuestGuardInstance) {
         ((QuestGuardInstance)npc).setPassive(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (HellboundManager.getInstance().getLevel() != 5) {
         return "32299.htm";
      } else {
         if (player.getQuestState(this.getName()) == null) {
            this.newQuestState(player);
         }

         return "32299-01.htm";
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      npc.setAutoAttackable(false);
      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onEnterZone(Creature character, ZoneType zone) {
      if (character instanceof Attackable) {
         Attackable npc = (Attackable)character;
         if (npc.getId() == 32299
            && !npc.isDead()
            && !npc.isDecayed()
            && npc.getAI().getIntention() == CtrlIntention.FOLLOW
            && HellboundManager.getInstance().getLevel() == 5) {
            ThreadPoolManager.getInstance().schedule(new Quarry.Decay(npc), 1000L);

            try {
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.THANK_YOU_FOR_THE_RESCUE_ITS_A_SMALL_GIFT), 2000);
            } catch (Exception var5) {
            }
         }
      }

      return null;
   }

   public Quarry(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addSpawnId(new int[]{32299});
      this.addFirstTalkId(32299);
      this.addStartNpc(32299);
      this.addTalkId(32299);
      this.addKillId(32299);
      this.addEnterZoneId(new int[]{40107});
   }

   public static void main(String[] args) {
      new Quarry(-1, "Quarry", "hellbound");
   }

   private final class Decay implements Runnable {
      private final Npc _npc;

      public Decay(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         if (this._npc != null && !this._npc.isDead()) {
            if (this._npc.getTarget().isPlayer()) {
               for(int[] i : Quarry.DROPLIST) {
                  if (Quest.getRandom(10000) < i[1]) {
                     ((Attackable)this._npc).dropItem((Player)this._npc.getTarget(), i[0], (long)((int)Config.RATE_DROP_ITEMS));
                     break;
                  }
               }
            }

            this._npc.setAutoAttackable(false);
            this._npc.deleteMe();
            this._npc.getSpawn().decreaseCount(this._npc);
            HellboundManager.getInstance().updateTrust(50, true);
         }
      }
   }
}
