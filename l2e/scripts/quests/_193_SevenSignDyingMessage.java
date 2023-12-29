package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _193_SevenSignDyingMessage extends Quest {
   private static final String qn = "_193_SevenSignDyingMessage";
   private static final int HOLLINT = 30191;
   private static final int CAIN = 32569;
   private static final int ERIC = 32570;
   private static final int ATHEBALDT = 30760;
   private static final int SHILENSEVIL = 27343;
   private static final int JACOB_NECK = 13814;
   private static final int DEADMANS_HERB = 13816;
   private static final int SCULPTURE = 14353;
   private boolean ShilensevilOnSpawn = false;

   public _193_SevenSignDyingMessage(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30191);
      this.addTalkId(30191);
      this.addTalkId(32569);
      this.addTalkId(32570);
      this.addTalkId(30760);
      this.addKillId(27343);
      this.questItemIds = new int[]{13814, 13816, 14353};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_193_SevenSignDyingMessage");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 30191) {
            if (event.equalsIgnoreCase("30191-02.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.giveItems(13814, 1L);
               st.playSound("ItemSound.quest_accept");
            }
         } else if (npc.getId() == 32569) {
            if (event.equalsIgnoreCase("32569-05.htm")) {
               st.set("cond", "2");
               st.takeItems(13814, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               if (event.equalsIgnoreCase("9")) {
                  st.takeItems(13816, 1L);
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
                  player.showQuestMovie(9);
                  return "";
               }

               if (event.equalsIgnoreCase("32569-09.htm")) {
                  if (this.ShilensevilOnSpawn) {
                     htmltext = getNoQuestMsg(player);
                  } else {
                     npc.broadcastPacket(
                        new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP), 2000
                     );
                     MonsterInstance monster = (MonsterInstance)addSpawn(27343, 82624, 47422, -3220, 0, false, 300000L, true);
                     monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM), 2000);
                     monster.setRunning();
                     monster.addDamageHate(player, 0, 999);
                     monster.getAI().setIntention(CtrlIntention.ATTACK, player);
                     this.ShilensevilOnSpawn = true;
                     this.startQuestTimer("spawnS", 301000L, npc, player);
                     this.startQuestTimer("aiplayer", 30000L, npc, player);
                     this.startQuestTimer("stopaiplayer", 301000L, npc, player);
                  }
               } else {
                  if (event.equalsIgnoreCase("spawnS")) {
                     this.ShilensevilOnSpawn = false;
                     return "";
                  }

                  if (event.equalsIgnoreCase("aiplayer")) {
                     npc.setTarget(player);
                     npc.doCast(SkillsParser.getInstance().getInfo(1011, 18));
                     this.startQuestTimer("aiplayer", 30000L, npc, player);
                     return "";
                  }

                  if (event.equalsIgnoreCase("stopaiplayer")) {
                     this.cancelQuestTimer("aiplayer", npc, player);
                     return "";
                  }

                  if (event.equalsIgnoreCase("32569-13.htm")) {
                     st.set("cond", "6");
                     st.takeItems(14353, 1L);
                     st.playSound("ItemSound.quest_middle");
                  }
               }
            }
         } else if (npc.getId() == 32570) {
            if (event.equalsIgnoreCase("32570-02.htm")) {
               st.set("cond", "3");
               st.giveItems(13816, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 30760 && event.equalsIgnoreCase("30760-02.htm")) {
            st.addExpAndSp(25000000, 2500000);
            st.unset("cond");
            st.setState((byte)2);
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_193_SevenSignDyingMessage");
      QuestState first = player.getQuestState("_192_SevenSignSeriesOfDoubt");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30191) {
            switch(st.getState()) {
               case 0:
                  if (first != null && first.getState() == 2 && player.getLevel() >= 79) {
                     htmltext = "30191-01.htm";
                  } else {
                     htmltext = "30191-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "30191-03.htm";
                  }
                  break;
               case 2:
                  htmltext = getAlreadyCompletedMsg(player);
            }
         } else if (npc.getId() == 32569) {
            if (st.getState() == 1) {
               switch(st.getInt("cond")) {
                  case 1:
                     htmltext = "32569-01.htm";
                     break;
                  case 2:
                     htmltext = "32569-06.htm";
                     break;
                  case 3:
                     htmltext = "32569-07.htm";
                     break;
                  case 4:
                     htmltext = "32569-08.htm";
                     break;
                  case 5:
                     htmltext = "32569-10.htm";
               }
            }
         } else if (npc.getId() == 32570) {
            if (st.getState() == 1) {
               switch(st.getInt("cond")) {
                  case 2:
                     htmltext = "32570-01.htm";
                     break;
                  case 3:
                     htmltext = "32570-03.htm";
               }
            }
         } else if (npc.getId() == 30760 && st.getState() == 1 && st.getInt("cond") == 6) {
            htmltext = "30760-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_193_SevenSignDyingMessage");
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         if (npc.getId() == 27343 && st.getInt("cond") == 4) {
            npc.broadcastPacket(
               new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU), 2000
            );
            st.giveItems(14353, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            this.ShilensevilOnSpawn = false;
            this.cancelQuestTimer("aiplayer", npc, player);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _193_SevenSignDyingMessage(193, "_193_SevenSignDyingMessage", "");
   }
}
