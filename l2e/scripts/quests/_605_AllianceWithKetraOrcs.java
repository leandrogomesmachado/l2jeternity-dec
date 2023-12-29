package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _605_AllianceWithKetraOrcs extends Quest {
   private static final Map<Integer, _605_AllianceWithKetraOrcs.DropInfo> MOBS = new HashMap<>();
   private static final int[] KETRA_MARKS = new int[]{7211, 7212, 7213, 7214, 7215};
   private static final int[] VARKA_MARKS = new int[]{7221, 7222, 7223, 7224, 7225};
   private static final int[] SOLDIER_BADGE_COUNT = new int[]{100, 200, 300, 300, 400};
   private static final int[] OFFICER_BADGE_COUNT = new int[]{0, 100, 200, 300, 400};
   private static final int[] CAPTAIN_BADGE_COUNT = new int[]{0, 0, 100, 200, 200};

   private _605_AllianceWithKetraOrcs(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31371);
      this.addTalkId(31371);
      this.addKillId(
         new int[]{
            21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21364, 21365, 21366, 21368, 21369, 21370, 21371, 21372, 21373, 21374, 21375
         }
      );
      this.questItemIds = new int[]{7216, 7217, 7218};
   }

   private int getMaxItems(QuestState st, int itemId) {
      int count = 0;
      switch(itemId) {
         case 7216:
            count = SOLDIER_BADGE_COUNT[st.getCond() - 1];
            break;
         case 7217:
            count = OFFICER_BADGE_COUNT[st.getCond() - 1];
            break;
         case 7218:
            count = CAPTAIN_BADGE_COUNT[st.getCond() - 1];
      }

      return count;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "31371-12a.html":
            case "31371-12b.html":
            case "31371-25.html":
               break;
            case "31371-04.htm":
               if (this.hasAtLeastOneQuestItem(player, VARKA_MARKS)) {
                  return "31371-03.htm";
               }

               st.setState((byte)1);
               st.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ACCEPT);

               for(int i = 0; i < KETRA_MARKS.length; ++i) {
                  if (st.hasQuestItems(KETRA_MARKS[i])) {
                     st.setCond(i + 2);
                     return "31371-0" + (i + 5) + ".htm";
                  }
               }

               st.setCond(1);
               break;
            case "31371-12.html":
               if (st.getQuestItemsCount(7216) < (long)SOLDIER_BADGE_COUNT[0]) {
                  return getNoQuestMsg(player);
               }

               st.takeItems(7216, -1L);
               st.giveItems(KETRA_MARKS[0], 1L);
               st.setCond(2, true);
               break;
            case "31371-15.html":
               if (st.getQuestItemsCount(7216) < (long)SOLDIER_BADGE_COUNT[1] || st.getQuestItemsCount(7217) < (long)OFFICER_BADGE_COUNT[1]) {
                  return getNoQuestMsg(player);
               }

               takeItems(player, -1, new int[]{7216, 7217, KETRA_MARKS[0]});
               st.giveItems(KETRA_MARKS[1], 1L);
               st.setCond(3, true);
               break;
            case "31371-18.html":
               if (st.getQuestItemsCount(7216) < (long)SOLDIER_BADGE_COUNT[2]
                  || st.getQuestItemsCount(7217) < (long)OFFICER_BADGE_COUNT[2]
                  || st.getQuestItemsCount(7218) < (long)CAPTAIN_BADGE_COUNT[2]) {
                  return getNoQuestMsg(player);
               }

               takeItems(player, -1, new int[]{7216, 7217, 7218, KETRA_MARKS[1]});
               st.giveItems(KETRA_MARKS[2], 1L);
               st.setCond(4, true);
               break;
            case "31371-21.html":
               if (!st.hasQuestItems(7219)
                  || st.getQuestItemsCount(7216) < (long)SOLDIER_BADGE_COUNT[3]
                  || st.getQuestItemsCount(7217) < (long)OFFICER_BADGE_COUNT[3]
                  || st.getQuestItemsCount(7218) < (long)CAPTAIN_BADGE_COUNT[3]) {
                  return getNoQuestMsg(player);
               }

               takeItems(player, -1, new int[]{7216, 7217, 7218, 7219, KETRA_MARKS[2]});
               st.giveItems(KETRA_MARKS[3], 1L);
               st.setCond(5, true);
               break;
            case "31371-26.html":
               takeItems(player, -1, KETRA_MARKS);
               takeItems(player, -1, new int[]{7219, 7220});
               st.exitQuest(true, true);
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
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 74 ? "31371-01.htm" : "31371-02.htm";
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     htmltext = st.getQuestItemsCount(7216) >= (long)SOLDIER_BADGE_COUNT[0] ? "31371-11.html" : "31371-10.html";
                     break;
                  case 2:
                     htmltext = st.hasQuestItems(KETRA_MARKS[0])
                           && st.getQuestItemsCount(7216) >= (long)SOLDIER_BADGE_COUNT[1]
                           && st.getQuestItemsCount(7217) >= (long)OFFICER_BADGE_COUNT[1]
                        ? "31371-14.html"
                        : "31371-13.html";
                     break;
                  case 3:
                     htmltext = st.hasQuestItems(KETRA_MARKS[1])
                           && st.getQuestItemsCount(7216) >= (long)SOLDIER_BADGE_COUNT[2]
                           && st.getQuestItemsCount(7217) >= (long)OFFICER_BADGE_COUNT[2]
                           && st.getQuestItemsCount(7218) >= (long)CAPTAIN_BADGE_COUNT[2]
                        ? "31371-17.html"
                        : "31371-16.html";
                     break;
                  case 4:
                     htmltext = hasQuestItems(player, new int[]{KETRA_MARKS[2], 7219})
                           && st.getQuestItemsCount(7216) >= (long)SOLDIER_BADGE_COUNT[3]
                           && st.getQuestItemsCount(7217) >= (long)OFFICER_BADGE_COUNT[3]
                           && st.getQuestItemsCount(7218) >= (long)CAPTAIN_BADGE_COUNT[3]
                        ? "31371-20.html"
                        : "31371-19.html";
                     break;
                  case 5:
                     if (!st.hasQuestItems(KETRA_MARKS[3])
                        || !st.hasQuestItems(7220)
                        || st.getQuestItemsCount(7216) < (long)SOLDIER_BADGE_COUNT[4]
                        || st.getQuestItemsCount(7217) < (long)OFFICER_BADGE_COUNT[4]
                        || st.getQuestItemsCount(7218) < (long)CAPTAIN_BADGE_COUNT[4]) {
                        return "31371-22.html";
                     }

                     st.setCond(6, true);
                     takeItems(player, -1, new int[]{7216, 7217, 7218, 7220, KETRA_MARKS[3]});
                     st.giveItems(KETRA_MARKS[4], 1L);
                     htmltext = "31371-23.html";
                     break;
                  case 6:
                     if (st.hasQuestItems(KETRA_MARKS[4])) {
                        htmltext = "31371-24.html";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(killer, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            _605_AllianceWithKetraOrcs.DropInfo info = MOBS.get(npc.getId());
            if (st.getCond() >= info.getMinCond()) {
               int max = this.getMaxItems(st, info.getItemId());
               st.calcDropItems(this.getId(), info.getItemId(), npc.getId(), max);
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _605_AllianceWithKetraOrcs(605, _605_AllianceWithKetraOrcs.class.getSimpleName(), "");
   }

   static {
      MOBS.put(21350, new _605_AllianceWithKetraOrcs.DropInfo(1));
      MOBS.put(21351, new _605_AllianceWithKetraOrcs.DropInfo(1));
      MOBS.put(21353, new _605_AllianceWithKetraOrcs.DropInfo(1));
      MOBS.put(21354, new _605_AllianceWithKetraOrcs.DropInfo(1));
      MOBS.put(21355, new _605_AllianceWithKetraOrcs.DropInfo(1));
      MOBS.put(21357, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21358, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21360, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21361, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21362, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21364, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21365, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21366, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21368, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21369, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21370, new _605_AllianceWithKetraOrcs.DropInfo(2));
      MOBS.put(21371, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21372, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21373, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21374, new _605_AllianceWithKetraOrcs.DropInfo(3));
      MOBS.put(21375, new _605_AllianceWithKetraOrcs.DropInfo(3));
   }

   private static class DropInfo {
      private final int _minCond;
      private final int _itemId;

      public DropInfo(int minCond) {
         this._minCond = minCond;
         switch(this._minCond) {
            case 1:
               this._itemId = 7216;
               break;
            case 2:
               this._itemId = 7217;
               break;
            default:
               this._itemId = 7218;
         }
      }

      public int getMinCond() {
         return this._minCond;
      }

      public int getItemId() {
         return this._itemId;
      }
   }
}
