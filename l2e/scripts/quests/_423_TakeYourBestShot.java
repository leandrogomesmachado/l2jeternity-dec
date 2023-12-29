package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _423_TakeYourBestShot extends Quest {
   private static int _totalModifier;
   private static int _spawnChance;
   private static final int[] _mobs = new int[]{22768, 22769, 22770, 22771, 22772, 22773, 22774};

   public _423_TakeYourBestShot(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32740, 32744});
      this.addTalkId(new int[]{32740, 32744});
      this.addFirstTalkId(32740);
      this.addKillId(new int[]{18862, 22768, 22769, 22770, 22771, 22772, 22773, 22774});
      _spawnChance = this.getQuestParams(questId).getInteger("spawnChance");
      _totalModifier = this.getQuestParams(questId).getInteger("totalModifier");
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32744) {
            if (event.equalsIgnoreCase("32744-04.htm")) {
               st.startQuest();
            } else if (event.equalsIgnoreCase("32744-quit.htm")) {
               st.exitQuest(true);
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32744) {
            switch(st.getState()) {
               case 0:
                  QuestState _prev = player.getQuestState("_249_PoisonedPlainsOfTheLizardmen");
                  if (_prev == null || _prev.getState() != 2 || player.getLevel() < 82) {
                     htmltext = "32744-00.htm";
                  } else if (st.hasQuestItems(15496)) {
                     htmltext = "32744-07.htm";
                  } else {
                     htmltext = "32744-01.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(1)) {
                     htmltext = "32744-05.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "32744-06.htm";
                  }
            }
         } else if (npc.getId() == 32740) {
            if (st.getState() == 0) {
               if (st.hasQuestItems(15496)) {
                  htmltext = "32740-05.htm";
               } else {
                  htmltext = "32740-00.htm";
               }
            } else if (st.getState() == 1 && st.isCond(1)) {
               htmltext = "32740-02.htm";
            } else if (st.getState() == 1 && st.isCond(2)) {
               st.calcReward(this.getId());
               st.exitQuest(true, true);
               htmltext = "32740-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         Quest q = QuestManager.getInstance().getQuest(this.getName());
         st = q.newQuestState(player);
      }

      return npc.isInsideRadius(96782, 85918, 100, true) ? "32740-ugoros.htm" : "32740.htm";
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (Util.contains(_mobs, npc.getId()) && getRandom(_totalModifier) <= _spawnChance) {
            Npc guard = addSpawn(18862, npc, false);
            if (player != null) {
               guard.setIsRunning(true);
               ((Attackable)guard).addDamageHate(player, 0, 999);
               guard.getAI().setIntention(CtrlIntention.ATTACK, player);
            }
         } else if (npc.getId() == 18862 && st.isCond(1)) {
            st.setCond(2, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _423_TakeYourBestShot(423, _423_TakeYourBestShot.class.getSimpleName(), "");
   }
}
