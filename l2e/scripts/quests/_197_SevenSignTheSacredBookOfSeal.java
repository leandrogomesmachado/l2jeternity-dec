package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _197_SevenSignTheSacredBookOfSeal extends Quest {
   private static final String qn = "_197_SevenSignTheSacredBookOfSeal";
   private static final int WOOD = 32593;
   private static final int ORVEN = 30857;
   private static final int LEOPARD = 32594;
   private static final int LAWRENCE = 32595;
   private static final int SOFIA = 32596;
   private static final int SHILENSEVIL = 27396;
   private static final int TEXT = 13829;
   private static final int SCULPTURE = 14355;
   private boolean ShilensevilOnSpawn = false;

   public _197_SevenSignTheSacredBookOfSeal(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32593);
      this.addTalkId(32593);
      this.addTalkId(30857);
      this.addTalkId(32594);
      this.addTalkId(32595);
      this.addTalkId(32596);
      this.addKillId(27396);
      this.questItemIds = new int[]{13829, 14355};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_197_SevenSignTheSacredBookOfSeal");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32593) {
            if (event.equalsIgnoreCase("32593-04.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            } else if (event.equalsIgnoreCase("32593-08.htm")) {
               st.takeItems(13829, 1L);
               st.takeItems(14355, 1L);
               st.addExpAndSp(25000000, 2500000);
               st.unset("cond");
               st.setState((byte)2);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            }
         } else if (npc.getId() == 30857) {
            if (event.equalsIgnoreCase("30857-04.htm")) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 32594) {
            if (event.equalsIgnoreCase("32594-03.htm")) {
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 32595) {
            if (event.equalsIgnoreCase("32595-04.htm")) {
               if (!this.ShilensevilOnSpawn) {
                  MonsterInstance monster = (MonsterInstance)addSpawn(27396, 152520, -57486, -3430, 0, false, 300000L, true);
                  monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM), 2000);
                  monster.setRunning();
                  monster.addDamageHate(player, 0, 999);
                  monster.getAI().setIntention(CtrlIntention.ATTACK, player);
                  this.ShilensevilOnSpawn = true;
                  this.startQuestTimer("spawnS", 300000L, npc, player);
               }
            } else if (event.equalsIgnoreCase("spawnS")) {
               if (this.ShilensevilOnSpawn) {
                  this.ShilensevilOnSpawn = false;
                  npc.broadcastPacket(new NpcSay(27396, 0, 27396, NpcStringId.NEXT_TIME_YOU_WILL_NOT_ESCAPE), 2000);
                  htmltext = "";
               } else {
                  htmltext = "";
               }
            } else if (event.equalsIgnoreCase("32595-08.htm")) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 32596 && event.equalsIgnoreCase("32596-04.htm")) {
            st.set("cond", "6");
            st.giveItems(13829, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_197_SevenSignTheSacredBookOfSeal");
      if (st == null) {
         return htmltext;
      } else {
         QuestState fourth = player.getQuestState("_196_SevenSignSealOfTheEmperor");
         if (npc.getId() == 32593) {
            switch(st.getState()) {
               case 0:
                  if (fourth != null && fourth.getState() == 2 && player.getLevel() >= 79) {
                     htmltext = "32593-01.htm";
                  } else {
                     htmltext = "32593-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getInt("cond") >= 1 && st.getInt("cond") <= 5) {
                     htmltext = "32593-05.htm";
                  } else if (st.getInt("cond") == 6) {
                     htmltext = "32593-06.htm";
                  }
            }
         } else if (npc.getId() == 30857) {
            if (st.getState() == 1) {
               if (st.getInt("cond") == 1) {
                  htmltext = "30857-01.htm";
               } else if (st.getInt("cond") >= 2) {
                  htmltext = "30857-05.htm";
               }
            }
         } else if (npc.getId() == 32594) {
            if (st.getState() == 1) {
               if (st.getInt("cond") == 2) {
                  htmltext = "32594-01.htm";
               } else if (st.getInt("cond") >= 3) {
                  htmltext = "32594-04.htm";
               }
            }
         } else if (npc.getId() == 32595) {
            if (st.getState() == 1) {
               if (st.getInt("cond") == 3) {
                  htmltext = "32595-01.htm";
               } else if (st.getInt("cond") == 4) {
                  htmltext = "32595-05.htm";
               } else if (st.getInt("cond") >= 5) {
                  htmltext = "32595-09.htm";
               }
            }
         } else if (npc.getId() == 32596 && st.getState() == 1) {
            if (st.getInt("cond") == 5) {
               htmltext = "32596-01.htm";
            } else if (st.getInt("cond") == 6) {
               htmltext = "32596-05.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_197_SevenSignTheSacredBookOfSeal");
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         if (npc.getId() == 27396 && st.getInt("cond") == 3) {
            this.ShilensevilOnSpawn = false;
            npc.broadcastPacket(
               new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU), 2000
            );
            st.giveItems(14355, 1L);
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _197_SevenSignTheSacredBookOfSeal(197, "_197_SevenSignTheSacredBookOfSeal", "");
   }
}
