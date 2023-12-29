package l2e.scripts.quests;

import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.RadarControl;

public class _307_ControlDeviceOfTheGiants extends Quest {
   private static final String qn = "_307_ControlDeviceOfTheGiants";
   private static int DROPH = 32711;
   private static int HEKATON_PRIME = 25687;
   private static final int DROPHS_ITEMS = 14850;
   private static final int CAVETEXT1SHEET = 14851;
   private static final int CAVETEXT2SHEET = 14852;
   private static final int CAVETEXT3SHEET = 14853;
   private static final long HEKATON_PRIME_RESPAWN = 43200000L;
   private static final Location GORGOLOS_LOC = new Location(186096, 61501, -4075, 0);
   private static final Location LAST_TITAN_UTENUS_LOC = new Location(186730, 56456, -4555, 0);
   private static final Location GIANT_MARPANAK_LOC = new Location(194057, 53722, -4259, 0);
   private static final Location HEKATON_PRIME_LOC = new Location(192328, 56120, -7651, 0);

   public _307_ControlDeviceOfTheGiants(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(DROPH);
      this.addTalkId(DROPH);
      this.addKillId(HEKATON_PRIME);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_307_ControlDeviceOfTheGiants");
      String htmltext = event;
      if (event.equalsIgnoreCase("32711-02.htm")) {
         st.set("cond", "1");
         st.setState((byte)1);
         st.playSound("ItemSound.quest_accept");
      } else if (event.equalsIgnoreCase("loc1")) {
         htmltext = "32711-02a_1.htm";
         RadarControl rc = new RadarControl(0, 1, GORGOLOS_LOC.getX(), GORGOLOS_LOC.getY(), GORGOLOS_LOC.getZ());
         player.sendPacket(rc);
      } else if (event.equalsIgnoreCase("loc2")) {
         htmltext = "32711-02a_2.htm";
         RadarControl rc = new RadarControl(0, 1, LAST_TITAN_UTENUS_LOC.getX(), LAST_TITAN_UTENUS_LOC.getY(), LAST_TITAN_UTENUS_LOC.getZ());
         player.sendPacket(rc);
      } else if (event.equalsIgnoreCase("loc3")) {
         htmltext = "32711-02a_3.htm";
         RadarControl rc = new RadarControl(0, 1, GIANT_MARPANAK_LOC.getX(), GIANT_MARPANAK_LOC.getY(), GIANT_MARPANAK_LOC.getZ());
         player.sendPacket(rc);
      } else if (event.equalsIgnoreCase("summon_rb")) {
         if (ServerVariables.getLong("HekatonPrimeRespawn", 0L) < System.currentTimeMillis()
            && st.getQuestItemsCount(14851) >= 1L
            && st.getQuestItemsCount(14852) >= 1L
            && st.getQuestItemsCount(14853) >= 1L) {
            st.takeItems(14851, 1L);
            st.takeItems(14852, 1L);
            st.takeItems(14853, 1L);
            ServerVariables.set("HekatonPrimeRespawn", System.currentTimeMillis() + 43200000L);
            st.addSpawn(HEKATON_PRIME, HEKATON_PRIME_LOC.getX(), HEKATON_PRIME_LOC.getY(), HEKATON_PRIME_LOC.getZ(), 0);
            htmltext = "32711-03a.htm";
         } else {
            htmltext = "32711-02b.htm";
         }
      }

      return htmltext;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_307_ControlDeviceOfTheGiants");
      int id = st.getState();
      int cond = st.getInt("cond");
      int npcId = npc.getId();
      if (npcId == DROPH) {
         if (id == 0 && cond == 0) {
            if (player.getLevel() >= 79) {
               htmltext = "32711-01.htm";
            } else {
               st.exitQuest(true);
               htmltext = "32711-00.htm";
            }
         } else if (id == 1 && npcId == DROPH) {
            if (cond == 1) {
               if (st.getQuestItemsCount(14851) < 1L || st.getQuestItemsCount(14852) < 1L || st.getQuestItemsCount(14853) < 1L) {
                  htmltext = "32711-02a.htm";
               } else if (ServerVariables.getLong("HekatonPrimeRespawn", 0L) < System.currentTimeMillis()) {
                  htmltext = "32711-03.htm";
               } else {
                  htmltext = "32711-04.htm";
               }
            } else if (cond == 2) {
               htmltext = "32711-05.htm";
               st.giveItems(14850, 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            }
         }
      }

      return htmltext;
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_307_ControlDeviceOfTheGiants");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && npc.getId() == HEKATON_PRIME) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _307_ControlDeviceOfTheGiants(307, "_307_ControlDeviceOfTheGiants", "");
   }
}
