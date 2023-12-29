package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _464_Oath extends Quest {
   private static final int[][] NPC = new int[][]{
      {32596, 0, 0, 0},
      {30657, 15449, 17696, 42910},
      {30839, 189377, 21692, 52599},
      {30899, 249180, 28542, 69210},
      {31350, 249180, 28542, 69210},
      {30539, 19408, 47062, 169442},
      {30297, 24146, 58551, 210806},
      {31960, 15449, 17696, 42910},
      {31588, 15449, 17696, 42910}
   };
   private static final int STRONGBOX = 15537;
   private static final int BOOK = 15538;
   private static final int BOOK2 = 15539;
   private static final int MIN_LEVEL = 82;
   private static final Map<Integer, Integer> MOBS = new HashMap<>();

   public _464_Oath(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int[] npc : NPC) {
         this.addTalkId(npc[0]);
      }

      this.addKillId(MOBS.keySet());
      this.registerQuestItems(new int[]{15538, 15539});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "32596-04.htm":
               if (!st.hasQuestItems(15538)) {
                  return getNoQuestMsg(player);
               }

               int cond = getRandom(2, 9);
               st.set("npc", String.valueOf(NPC[cond - 1][0]));
               st.setCond(cond, true);
               st.takeItems(15538, 1L);
               st.giveItems(15539, 1L);
               switch(cond) {
                  case 2:
                     return "32596-04.htm";
                  case 3:
                     return "32596-04a.htm";
                  case 4:
                     return "32596-04b.htm";
                  case 5:
                     return "32596-04c.htm";
                  case 6:
                     return "32596-04d.htm";
                  case 7:
                     return "32596-04e.htm";
                  case 8:
                     return "32596-04f.htm";
                  case 9:
                     htmltext = "32596-04g.htm";
                     return htmltext;
                  default:
                     return event;
               }
            case "end_quest":
               if (!st.hasQuestItems(15539)) {
                  return getNoQuestMsg(player);
               }

               int i = st.getCond() - 1;
               st.addExpAndSp(NPC[i][1], NPC[i][2]);
               st.rewardItems(57, (long)NPC[i][3]);
               st.exitQuest(QuestState.QuestType.DAILY, true);
               htmltext = npc.getId() + "-02.htm";
            case "32596-02.htm":
            case "32596-03.htm":
               break;
            default:
               htmltext = null;
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isStarted()) {
         int npcId = npc.getId();
         if (npcId == NPC[0][0]) {
            switch(st.getCond()) {
               case 1:
                  htmltext = "32596-01.htm";
                  break;
               case 2:
                  htmltext = "32596-05.htm";
                  break;
               case 3:
                  htmltext = "32596-05a.htm";
                  break;
               case 4:
                  htmltext = "32596-05b.htm";
                  break;
               case 5:
                  htmltext = "32596-05c.htm";
                  break;
               case 6:
                  htmltext = "32596-05d.htm";
                  break;
               case 7:
                  htmltext = "32596-05e.htm";
                  break;
               case 8:
                  htmltext = "32596-05f.htm";
                  break;
               case 9:
                  htmltext = "32596-05g.htm";
            }
         } else if (st.getCond() > 1 && st.getInt("npc") == npcId) {
            htmltext = npcId + "-01.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onItemTalk(ItemInstance item, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         boolean startQuest = false;
         switch(st.getState()) {
            case 0:
               startQuest = true;
               break;
            case 1:
               htmltext = "strongbox-02.htm";
               break;
            case 2:
               if (st.isNowAvailable()) {
                  st.setState((byte)0);
                  startQuest = true;
               } else {
                  htmltext = "strongbox-03.htm";
               }
         }

         if (startQuest) {
            if (player.getLevel() >= 82) {
               st.startQuest();
               st.takeItems(15537, 1L);
               st.giveItems(15538, 1L);
               htmltext = "strongbox-01.htm";
            } else {
               htmltext = "strongbox-00.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (getRandom(1000) < MOBS.get(npc.getId())) {
         ((MonsterInstance)npc).dropItem(killer, 15537, 1L);
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _464_Oath(464, _464_Oath.class.getSimpleName(), "");
   }

   static {
      MOBS.put(22799, 9);
      MOBS.put(22794, 6);
      MOBS.put(22800, 10);
      MOBS.put(22796, 9);
      MOBS.put(22798, 9);
      MOBS.put(22795, 8);
      MOBS.put(22797, 7);
      MOBS.put(22789, 5);
      MOBS.put(22791, 4);
      MOBS.put(22790, 5);
      MOBS.put(22792, 4);
      MOBS.put(22793, 5);
   }
}
