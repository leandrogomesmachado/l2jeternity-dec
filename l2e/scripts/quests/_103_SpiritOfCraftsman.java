package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _103_SpiritOfCraftsman extends Quest {
   private static final String qn = "_103_SpiritOfCraftsman";
   private static final int KAROYDS_LETTER_ID = 968;
   private static final int CECKTINONS_VOUCHER1_ID = 969;
   private static final int CECKTINONS_VOUCHER2_ID = 970;
   private static final int BONE_FRAGMENT1_ID = 1107;
   private static final int SOUL_CATCHER_ID = 971;
   private static final int PRESERVE_OIL_ID = 972;
   private static final int ZOMBIE_HEAD_ID = 973;
   private static final int STEELBENDERS_HEAD_ID = 974;
   private static final int BLOODSABER_ID = 975;
   private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;
   private static final int SPIRITSHOT_NO_GRADE = 2509;
   private static final int SOULSHOT_NO_GRADE = 1835;

   public _103_SpiritOfCraftsman(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(30307);
      this.addTalkId(30307);
      this.addTalkId(30132);
      this.addTalkId(30144);
      this.addKillId(20015);
      this.addKillId(20020);
      this.addKillId(20455);
      this.addKillId(20517);
      this.addKillId(20518);
      this.questItemIds = new int[]{968, 969, 970, 1107, 971, 972, 973, 974};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_103_SpiritOfCraftsman");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30307-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(968, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_103_SpiritOfCraftsman");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 30008 && id == 0) {
            if (player.getRace().ordinal() != 2) {
               htmltext = "30307-00.htm";
            } else if (player.getLevel() >= 10) {
               htmltext = "30307-03.htm";
            } else {
               st.exitQuest(true);
               htmltext = "30307-02.htm";
            }
         } else if (id == 1) {
            if (npcId != 30307
               || st.getInt("cond") < 1
               || st.getQuestItemsCount(968) < 1L && st.getQuestItemsCount(969) < 1L && st.getQuestItemsCount(970) < 1L) {
               if (npcId == 30132 && st.getInt("cond") == 1 && st.getQuestItemsCount(968) == 1L) {
                  htmltext = "30132-01.htm";
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
                  st.takeItems(968, 1L);
                  st.giveItems(969, 1L);
               } else if ((npcId != 30132 || st.getInt("cond") < 2 || st.getQuestItemsCount(969) < 1L) && st.getQuestItemsCount(970) < 1L) {
                  if (npcId == 30144 && st.getInt("cond") == 2 && st.getQuestItemsCount(969) >= 1L) {
                     htmltext = "30144-01.htm";
                     st.set("cond", "3");
                     st.playSound("ItemSound.quest_middle");
                     st.takeItems(969, 1L);
                     st.giveItems(970, 1L);
                  } else if (npcId == 30144 && st.getInt("cond") == 3 && st.getQuestItemsCount(970) >= 1L && st.getQuestItemsCount(1107) < 10L) {
                     htmltext = "30144-02.htm";
                  } else if (npcId == 30144 && st.getInt("cond") == 3 && st.getQuestItemsCount(970) == 1L && st.getQuestItemsCount(1107) >= 10L) {
                     htmltext = "30144-03.htm";
                     st.set("cond", "5");
                     st.playSound("ItemSound.quest_middle");
                     st.takeItems(970, 1L);
                     st.takeItems(1107, 10L);
                     st.giveItems(971, 1L);
                  } else if (npcId == 30144 && st.getInt("cond") == 5 && st.getQuestItemsCount(971) == 1L) {
                     htmltext = "30144-04.htm";
                  } else if (npcId == 30132 && st.getInt("cond") == 5 && st.getQuestItemsCount(971) == 1L) {
                     htmltext = "30132-03.htm";
                     st.set("cond", "6");
                     st.playSound("ItemSound.quest_middle");
                     st.takeItems(971, 1L);
                     st.giveItems(972, 1L);
                  } else if (npcId == 30132
                     && st.getInt("cond") == 6
                     && st.getQuestItemsCount(972) == 1L
                     && st.getQuestItemsCount(973) == 0L
                     && st.getQuestItemsCount(974) == 0L) {
                     htmltext = "30132-04.htm";
                  } else if (npcId == 30132 && st.getInt("cond") == 6 && st.getQuestItemsCount(973) == 1L) {
                     htmltext = "30132-05.htm";
                     st.set("cond", "8");
                     st.playSound("ItemSound.quest_middle");
                     st.takeItems(973, 1L);
                     st.giveItems(974, 1L);
                  } else if (npcId == 30132 && st.getInt("cond") == 8 && st.getQuestItemsCount(974) == 1L) {
                     htmltext = "30132-06.htm";
                  } else if (npcId == 30307 && st.getInt("cond") == 8 && st.getQuestItemsCount(974) == 1L) {
                     htmltext = "30307-07.htm";
                     st.giveItems(57, 19799L);
                     st.addExpAndSp(46663, 3999);
                     st.takeItems(974, 1L);
                     st.giveItems(975, 1L);
                     st.giveItems(1060, 100L);
                     st.giveItems(1060, 100L);
                     st.giveItems(4412, 10L);
                     st.giveItems(4413, 10L);
                     st.giveItems(4414, 10L);
                     st.giveItems(4415, 10L);
                     st.giveItems(4416, 10L);
                     st.giveItems(4417, 10L);
                     if (player.getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                        st.giveItems(2509, 500L);
                     } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                        st.giveItems(1835, 1000L);
                     }

                     showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                     st.unset("cond");
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
               } else {
                  htmltext = "30132-02.htm";
               }
            } else {
               htmltext = "30307-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_103_SpiritOfCraftsman");
      if (st == null) {
         return null;
      } else {
         if (st.getState() == 1) {
            int npcId = npc.getId();
            if (npcId == 20517 || npcId == 20518 || npcId == 20455) {
               if (st.getQuestItemsCount(970) == 1L && st.getQuestItemsCount(1107) < 10L && getRandom(10) < 3) {
                  st.giveItems(1107, 1L);
                  if (st.getQuestItemsCount(1107) == 10L) {
                     st.playSound("ItemSound.quest_itemget");
                     st.set("cond", "4");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if ((npcId == 20015 || npcId == 20020) && st.getQuestItemsCount(972) == 1L && getRandom(10) < 3) {
               st.set("cond", "7");
               st.giveItems(973, 1L);
               st.playSound("ItemSound.quest_middle");
               st.takeItems(972, 1L);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _103_SpiritOfCraftsman(103, "_103_SpiritOfCraftsman", "");
   }
}
