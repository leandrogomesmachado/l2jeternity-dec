package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _105_SkirmishWithOrcs extends Quest {
   private static final String qn = "_105_SkirmishWithOrcs";
   private static final int KENDELL = 30218;
   private static final int KENDNELLS_ORDER1 = 1836;
   private static final int KENDNELLS_ORDER2 = 1837;
   private static final int KENDNELLS_ORDER3 = 1838;
   private static final int KENDNELLS_ORDER4 = 1839;
   private static final int KENDNELLS_ORDER5 = 1840;
   private static final int KENDNELLS_ORDER6 = 1841;
   private static final int KENDNELLS_ORDER7 = 1842;
   private static final int KENDNELLS_ORDER8 = 1843;
   private static final int KABOO_CHIEF_TORC1 = 1844;
   private static final int KABOO_CHIEF_TORC2 = 1845;
   private static final int RED_SUNSET_SWORD = 981;
   private static final int RED_SUNSET_STAFF = 754;
   private static final int KabooChiefUoph = 27059;
   private static final int KabooChiefKracha = 27060;
   private static final int KabooChiefBatoh = 27061;
   private static final int KabooChiefTanukia = 27062;
   private static final int KabooChiefTurel = 27064;
   private static final int KabooChiefRoko = 27065;
   private static final int KabooChiefKamut = 27067;
   private static final int KabooChiefMurtika = 27068;
   private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_NO_GRADE_FOR_BEGINNERS = 5789;
   private static final int LESSER_HEALING_POT = 1060;
   private static final int[] MOBS = new int[]{27059, 27060, 27061, 27062, 27064, 27065, 27067, 27068};

   public _105_SkirmishWithOrcs(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30218);
      this.addTalkId(30218);

      for(int npcId : MOBS) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{1836, 1837, 1838, 1839, 1840, 1841, 1842, 1843, 1844, 1845};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_105_SkirmishWithOrcs");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30218-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(1836) + st.getQuestItemsCount(1837) + st.getQuestItemsCount(1838) + st.getQuestItemsCount(1839) == 0L) {
               int n = getRandom(100);
               if (n < 25) {
                  st.giveItems(1836, 1L);
               } else if (n < 50) {
                  st.giveItems(1837, 1L);
               } else if (n < 75) {
                  st.giveItems(1838, 1L);
               } else {
                  st.giveItems(1839, 1L);
               }
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_105_SkirmishWithOrcs");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int cond = st.getInt("cond");
         if (cond == 0) {
            if (player.getRace().ordinal() != 1) {
               htmltext = "30218-00.htm";
               st.exitQuest(true);
            } else if (player.getLevel() < 10) {
               htmltext = "30218-10.htm";
               st.exitQuest(true);
            } else {
               htmltext = "30218-02.htm";
            }
         } else if (cond == 1 && st.getQuestItemsCount(1836) + st.getQuestItemsCount(1837) + st.getQuestItemsCount(1838) + st.getQuestItemsCount(1839) != 0L) {
            htmltext = "30218-05.htm";
         } else if (cond == 2 && st.getQuestItemsCount(1844) != 0L) {
            htmltext = "30218-06.htm";
            if (st.getQuestItemsCount(1836) > 0L) {
               st.takeItems(1836, -1L);
            }

            if (st.getQuestItemsCount(1837) > 0L) {
               st.takeItems(1837, -1L);
            }

            if (st.getQuestItemsCount(1838) > 0L) {
               st.takeItems(1838, -1L);
            }

            if (st.getQuestItemsCount(1839) > 0L) {
               st.takeItems(1839, -1L);
            }

            st.takeItems(1844, 1L);
            int n = getRandom(100);
            if (n < 25) {
               st.giveItems(1840, 1L);
            } else if (n < 50) {
               st.giveItems(1841, 1L);
            } else if (n < 75) {
               st.giveItems(1842, 1L);
            } else {
               st.giveItems(1843, 1L);
            }

            st.set("cond", "3");
            st.setState((byte)1);
         } else if (cond == 3 && st.getQuestItemsCount(1840) + st.getQuestItemsCount(1841) + st.getQuestItemsCount(1842) + st.getQuestItemsCount(1843) == 1L) {
            htmltext = "30218-07.htm";
         } else if (cond == 4 && st.getQuestItemsCount(1845) > 0L) {
            htmltext = "30218-08.htm";
            if (st.getQuestItemsCount(1840) > 0L) {
               st.takeItems(1840, -1L);
            }

            if (st.getQuestItemsCount(1841) > 0L) {
               st.takeItems(1841, -1L);
            }

            if (st.getQuestItemsCount(1842) > 0L) {
               st.takeItems(1842, -1L);
            }

            if (st.getQuestItemsCount(1843) > 0L) {
               st.takeItems(1843, -1L);
            }

            for(int ECHO_CHRYTSAL = 4412; ECHO_CHRYTSAL <= 4417; ++ECHO_CHRYTSAL) {
               st.giveItems(ECHO_CHRYTSAL, 10L);
            }

            st.takeItems(1845, -1L);
            st.giveItems(1060, 100L);
            if (player.getClassId().isMage()) {
               st.giveItems(754, 1L);
               st.giveItems(5790, 3000L);
               st.playTutorialVoice("tutorial_voice_027");
            } else {
               st.giveItems(981, 1L);
               st.giveItems(5789, 7000L);
               st.playTutorialVoice("tutorial_voice_026");
            }

            showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
            st.giveItems(57, 17599L);
            st.addExpAndSp(41478, 3555);
            st.unset("cond");
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_105_SkirmishWithOrcs");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (cond == 1 && st.getQuestItemsCount(1844) == 0L) {
            if (npcId == 27059 && st.getQuestItemsCount(1836) > 0L) {
               st.giveItems(1844, 1L);
            } else if (npcId == 27060 && st.getQuestItemsCount(1837) > 0L) {
               st.giveItems(1844, 1L);
            } else if (npcId == 27061 && st.getQuestItemsCount(1838) > 0L) {
               st.giveItems(1844, 1L);
            } else if (npcId == 27062 && st.getQuestItemsCount(1839) > 0L) {
               st.giveItems(1844, 1L);
            }

            if (st.getQuestItemsCount(1844) > 0L) {
               st.set("cond", "2");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (cond == 3 && st.getQuestItemsCount(1845) == 0L) {
            if (npcId == 27064 && st.getQuestItemsCount(1840) > 0L) {
               st.giveItems(1845, 1L);
            } else if (npcId == 27065 && st.getQuestItemsCount(1841) > 0L) {
               st.giveItems(1845, 1L);
            } else if (npcId == 27067 && st.getQuestItemsCount(1842) > 0L) {
               st.giveItems(1845, 1L);
            } else if (npcId == 27068 && st.getQuestItemsCount(1843) > 0L) {
               st.giveItems(1845, 1L);
            }

            if (st.getQuestItemsCount(1845) > 0L) {
               st.set("cond", "4");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _105_SkirmishWithOrcs(105, "_105_SkirmishWithOrcs", "");
   }
}
