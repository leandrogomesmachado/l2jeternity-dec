package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _064_CertifiedBerserker extends Quest {
   private static final int ORKURUS = 32207;
   private static final int TENAIN = 32215;
   private static final int GORT = 32252;
   private static final int ENTIEN = 32200;
   private static final int HARKILGAMED = 32253;
   private static final int BREKA_ORC = 20267;
   private static final int BREKA_ORC_ARCHER = 20268;
   private static final int BREKA_ORC_SHAMAN = 20269;
   private static final int BREKA_ORC_OVERLORD = 20270;
   private static final int BREKA_ORC_WARRIOR = 20271;
   private static final int ROAD_SCAVENGER = 20551;
   private static final int DEAD_SEEKER = 20202;
   private static final int MARSH_STAKATO_DRONE = 20234;
   private static final int DIVINE_EMISSARY = 27323;
   private static final int BREKA_ORC_HEAD = 9754;
   private static final int MESSAGE_PLATE = 9755;
   private static final int REPORT_EAST = 9756;
   private static final int REPORT_NORTH = 9757;
   private static final int HARKILGAMEDS_LETTER = 9758;
   private static final int TENAINS_RECOMMENDATION = 9759;
   private static final int ORKURUS_RECOMMENDATION = 9760;
   private static boolean _isSpawned = false;
   private static final int[] QUESTITEMS = new int[]{9754, 9755, 9756, 9757, 9758, 9759, 9760};

   public _064_CertifiedBerserker(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32207);
      this.addTalkId(32207);
      this.addTalkId(32215);
      this.addTalkId(32252);
      this.addTalkId(32200);
      this.addTalkId(32253);
      this.addKillId(20267);
      this.addKillId(20268);
      this.addKillId(20269);
      this.addKillId(20270);
      this.addKillId(20271);
      this.addKillId(20551);
      this.addKillId(20202);
      this.addKillId(20234);
      this.addKillId(27323);
      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32207-02.htm")) {
            st.startQuest();
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               giveItems(player, 7562, 48L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }
         } else if (event.equalsIgnoreCase("32215-02.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32252-02.htm")) {
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("32215-08.htm")) {
            st.takeItems(9755, -1L);
         } else if (event.equalsIgnoreCase("32215-10.htm")) {
            st.setCond(8, true);
         } else if (event.equalsIgnoreCase("Despawn_Harkilgamed")) {
            _isSpawned = false;
         } else if (event.equalsIgnoreCase("32236-02.htm")) {
            st.setCond(13, true);
            st.giveItems(9758, 1L);
         } else if (event.equalsIgnoreCase("32215-15.htm")) {
            st.takeItems(9758, -1L);
            st.giveItems(9759, 1L);
            st.setCond(14, true);
         } else if (event.equalsIgnoreCase("32207-05.htm")) {
            st.unset("kills");
            st.unset("spawned");
            st.takeItems(9759, -1L);
            st.addExpAndSp(174503, 11974);
            st.giveItems(57, 31552L);
            st.giveItems(9760, 1L);
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (st.getState() == 2) {
            htmltext = Quest.getAlreadyCompletedMsg(player);
         } else if (npcId == 32207) {
            if (player.getClassId().getId() != 125 || player.getLevel() < 39) {
               htmltext = "32207-00.htm";
               st.exitQuest(true);
            } else if (st.getState() == 0) {
               htmltext = "32207-01.htm";
            } else if (cond == 1) {
               htmltext = "32207-03.htm";
            } else if (cond == 14) {
               htmltext = "32207-04.htm";
            }
         } else if (npcId == 32215) {
            if (cond == 1) {
               htmltext = "32215-01.htm";
            } else if (cond == 2) {
               htmltext = "32215-03.htm";
            } else if (cond == 3) {
               htmltext = "32215-04.htm";
               st.takeItems(9754, -1L);
               st.setCond(4, true);
            } else if (cond == 4) {
               htmltext = "32215-05.htm";
            } else if (cond == 7) {
               htmltext = "32215-06.htm";
            } else if (cond == 8) {
               htmltext = "32215-11.htm";
            } else if (cond == 11) {
               htmltext = "32215-12.htm";
               st.setCond(12, true);
               st.set("kills", "0");
               st.set("spawned", "0");
            } else if (cond == 12) {
               htmltext = "32215-13.htm";
            } else if (cond == 13) {
               htmltext = "32215-14.htm";
            }
         } else if (npcId == 32252) {
            if (cond == 4) {
               htmltext = "32252-01.htm";
            } else if (cond == 5) {
               htmltext = "32252-03.htm";
            } else if (cond == 6) {
               htmltext = "32252-04.htm";
               st.setCond(7, true);
            } else if (cond == 7) {
               htmltext = "32252-05.htm";
            }
         } else if (npcId == 32200) {
            if (cond == 8) {
               htmltext = "32200-01.htm";
               st.setCond(9, true);
            } else if (cond == 9) {
               htmltext = "32200-02.htm";
            } else if (cond == 10) {
               htmltext = "32200-03.htm";
               st.takeItems(9756, -1L);
               st.takeItems(9757, -1L);
               st.setCond(11, true);
            } else if (cond == 11) {
               htmltext = "32200-04.htm";
            }
         } else if (npcId == 32253) {
            if (cond == 12) {
               htmltext = "32236-01.htm";
            } else if (cond == 13) {
               htmltext = "32236-03.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20267 || npcId == 20268 || npcId == 20269 || npcId == 20270 || npcId == 20271) {
            if (st.getQuestItemsCount(9754) < 20L && cond == 2) {
               st.giveItems(9754, 1L);
               if (st.getQuestItemsCount(9754) == 20L) {
                  st.setCond(3, true);
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20551) {
            if (st.getQuestItemsCount(9755) == 0L && st.getRandom(20) == 1 && cond == 5) {
               st.giveItems(9755, 1L);
               st.setCond(6, true);
            }
         } else if (npcId == 20202) {
            if (st.getQuestItemsCount(9756) == 0L && st.getRandom(30) == 1 && cond == 9) {
               st.giveItems(9756, 1L);
               st.playSound("ItemSound.quest_middle");
               if (st.getQuestItemsCount(9757) > 0L) {
                  st.setCond(10);
               }
            }
         } else if (npcId == 20234) {
            if (st.getQuestItemsCount(9757) == 0L && st.getRandom(30) == 1 && cond == 9) {
               st.giveItems(9757, 1L);
               st.playSound("ItemSound.quest_middle");
               if (st.getQuestItemsCount(9756) > 0L) {
                  st.setCond(10);
               }
            }
         } else if (npcId == 27323 && cond == 12 && !_isSpawned) {
            if (st.getInt("kills") < 5) {
               st.set("kills", String.valueOf(st.getInt("kills") + 1));
            } else {
               st.addSpawn(32253, 120000);
               st.set("kills", "0");
               _isSpawned = true;
               st.startQuestTimer("Despawn_Harkilgamed", 120000L);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _064_CertifiedBerserker(64, _064_CertifiedBerserker.class.getSimpleName(), "");
   }
}
