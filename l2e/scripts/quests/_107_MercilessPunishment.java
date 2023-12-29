package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _107_MercilessPunishment extends Quest {
   private static final String qn = "_107_MercilessPunishment";
   private static final int HATOSS_ORDER1_ID = 1553;
   private static final int HATOSS_ORDER2_ID = 1554;
   private static final int HATOSS_ORDER3_ID = 1555;
   private static final int LETTER_TO_HUMAN_ID = 1557;
   private static final int LETTER_TO_DARKELF_ID = 1556;
   private static final int LETTER_TO_ELF_ID = 1558;
   private static final int BUTCHER_ID = 1510;
   private static final int LESSER_HEALING_ID = 1060;
   private static final int CRYSTAL_BATTLE = 4412;
   private static final int CRYSTAL_LOVE = 4413;
   private static final int CRYSTAL_SOLITUDE = 4414;
   private static final int CRYSTAL_FEAST = 4415;
   private static final int CRYSTAL_CELEBRATION = 4416;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _107_MercilessPunishment(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30568);
      this.addTalkId(30568);
      this.addTalkId(30580);
      this.addKillId(27041);
      this.questItemIds = new int[]{1554, 1556, 1557, 1558, 1553, 1555};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_107_MercilessPunishment");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            htmltext = "30568-03.htm";
            st.giveItems(1553, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30568_1")) {
            htmltext = "30568-06.htm";
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         } else if (event.equalsIgnoreCase("30568_2")) {
            htmltext = "30568-07.htm";
            st.takeItems(1553, 1L);
            if (st.getQuestItemsCount(1554) == 0L) {
               st.giveItems(1554, 1L);
            }
         } else if (event.equalsIgnoreCase("30568_3")) {
            htmltext = "30568-06.htm";
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         } else if (event.equalsIgnoreCase("30568_4")) {
            htmltext = "30568-09.htm";
            st.takeItems(1554, 1L);
            if (st.getQuestItemsCount(1555) == 0L) {
               st.giveItems(1555, 1L);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_107_MercilessPunishment");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int npcId = npc.getId();
         int id = st.getState();
         if (npcId == 30568 && id == 0) {
            if (player.getRace().ordinal() != 3) {
               htmltext = "30568-00.htm";
               st.exitQuest(true);
            } else if (player.getLevel() >= 10) {
               htmltext = "30568-02.htm";
            } else {
               htmltext = "30568-01.htm";
               st.exitQuest(true);
            }
         } else if (npcId == 30568 && st.getInt("cond") == 1 && st.getQuestItemsCount(1553) > 0L && st.getQuestItemsCount(1557) == 0L) {
            htmltext = "30568-04.htm";
         } else if (npcId == 30568 && st.getInt("cond") == 1 && st.getQuestItemsCount(1553) > 0L && st.getQuestItemsCount(1557) >= 1L) {
            htmltext = "30568-05.htm";
         } else if (npcId == 30568 && st.getInt("cond") == 1 && st.getQuestItemsCount(1554) > 0L && st.getQuestItemsCount(1556) >= 1L) {
            htmltext = "30568-08.htm";
         } else if (npcId == 30568
            && st.getInt("cond") == 1
            && st.getQuestItemsCount(1555) > 0L
            && st.getQuestItemsCount(1558) + st.getQuestItemsCount(1557) + st.getQuestItemsCount(1556) == 3L) {
            if (st.getInt("id") != 107) {
               st.set("id", "107");
               htmltext = "30568-10.htm";
               st.takeItems(1556, 1L);
               st.takeItems(1557, 1L);
               st.takeItems(1558, 1L);
               st.takeItems(1555, 1L);
               st.giveItems(57, 14666L);
               st.giveItems(1060, 100L);
               st.giveItems(1510, 1L);
               st.giveItems(4412, 10L);
               st.giveItems(4413, 10L);
               st.giveItems(4414, 10L);
               st.giveItems(4415, 10L);
               st.giveItems(4416, 10L);
               st.addExpAndSp(34565, 2962);
               if (player.getClassId().isMage()) {
                  st.giveItems(5790, 3000L);
                  st.playTutorialVoice("tutorial_voice_027");
               } else {
                  st.giveItems(5789, 7000L);
                  st.playTutorialVoice("tutorial_voice_026");
               }

               showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
               st.unset("cond");
               player.sendPacket(new SocialAction(player.getObjectId(), 3));
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            }
         } else if (npcId == 30580 && st.getInt("cond") == 1 && id == 1 && st.getQuestItemsCount(1553) > 0L
            || st.getQuestItemsCount(1554) > 0L
            || st.getQuestItemsCount(1555) > 0L) {
            htmltext = "30580-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_107_MercilessPunishment");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 27041) {
            st.set("id", "0");
            if (cond == 1 && st.getQuestItemsCount(1553) > 0L && st.getQuestItemsCount(1557) == 0L) {
               st.giveItems(1557, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if (cond == 1 && st.getQuestItemsCount(1554) > 0L && st.getQuestItemsCount(1556) == 0L) {
               st.giveItems(1556, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if (cond == 1 && st.getQuestItemsCount(1555) > 0L && st.getQuestItemsCount(1558) == 0L) {
               st.giveItems(1558, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _107_MercilessPunishment(107, "_107_MercilessPunishment", "");
   }
}
