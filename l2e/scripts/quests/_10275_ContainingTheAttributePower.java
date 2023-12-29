package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10275_ContainingTheAttributePower extends Quest {
   public _10275_ContainingTheAttributePower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{30839, 31307});
      this.addTalkId(new int[]{30839, 31307, 32325, 32326});
      this.addKillId(new int[]{27381, 27380});
      this.questItemIds = new int[]{13845, 13881, 13861, 13862};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30839-02.htm":
            case "31307-02.htm":
               st.startQuest();
               break;
            case "30839-05.htm":
               st.setCond(2, true);
               break;
            case "31307-05.htm":
               st.setCond(7, true);
               break;
            case "32325-03.htm":
               st.giveItems(13845, 1L, (byte)0, 10);
               st.setCond(3, true);
               break;
            case "32326-03.htm":
               st.giveItems(13881, 1L, (byte)3, 10);
               st.setCond(8, true);
               break;
            case "32325-06.htm":
               if (st.hasQuestItems(13845)) {
                  st.takeItems(13845, 1L);
                  htmltext = "32325-07.htm";
               }

               st.giveItems(13845, 1L, (byte)0, 10);
               break;
            case "32326-06.htm":
               if (st.hasQuestItems(13881)) {
                  st.takeItems(13881, 1L);
                  htmltext = "32326-07.htm";
               }

               st.giveItems(13881, 1L, (byte)3, 10);
               break;
            case "32325-09.htm":
               SkillsParser.getInstance().getInfo(2635, 1).getEffects(player, player, false);
               st.giveItems(13845, 1L, (byte)0, 10);
               st.setCond(5, true);
               break;
            case "32326-09.htm":
               SkillsParser.getInstance().getInfo(2636, 1).getEffects(player, player, false);
               st.giveItems(13881, 1L, (byte)3, 10);
               st.setCond(10, true);
         }

         if (Util.isDigit(event)) {
            st.giveItems(10520 + Integer.valueOf(event), 2L);
            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
            htmltext = Integer.toString(npc.getId()) + "-1" + event + ".htm";
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
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 76) {
                  if (npcId == 30839) {
                     htmltext = "30839-01.htm";
                  } else {
                     htmltext = "31307-01.htm";
                  }
               } else if (npcId == 30839) {
                  htmltext = "30839-00.htm";
               } else {
                  htmltext = "31307-00.htm";
               }
               break;
            case 2:
               if (npcId == 30839) {
                  htmltext = "30839-0a.htm";
               } else if (npcId == 31307) {
                  htmltext = "31307-0a.htm";
               }
               break;
            default:
               switch(npcId) {
                  case 30839:
                     switch(cond) {
                        case 1:
                           return "30839-03.htm";
                        case 2:
                           return "30839-05.htm";
                        default:
                           return htmltext;
                     }
                  case 31307:
                     switch(cond) {
                        case 1:
                           return "31307-03.htm";
                        case 7:
                           htmltext = "31307-05.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 32325:
                     switch(cond) {
                        case 2:
                           htmltext = "32325-01.htm";
                           break;
                        case 3:
                        case 5:
                           htmltext = "32325-04.htm";
                           break;
                        case 4:
                           htmltext = "32325-08.htm";
                           st.takeItems(13845, 1L);
                           st.takeItems(13861, -1L);
                           break;
                        case 6:
                           htmltext = "32325-10.htm";
                     }
                  case 32326:
                     switch(cond) {
                        case 7:
                           htmltext = "32326-01.htm";
                           break;
                        case 8:
                        case 10:
                           htmltext = "32326-04.htm";
                           break;
                        case 9:
                           htmltext = "32326-08.htm";
                           st.takeItems(13881, 1L);
                           st.takeItems(13862, -1L);
                           break;
                        case 11:
                           htmltext = "32326-10.htm";
                     }
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
         int cond = st.getCond();
         switch(npc.getId()) {
            case 27380:
               if (st.getItemEquipped(5) == 13845 && (cond >= 3 || cond <= 5) && st.calcDropItems(this.getId(), 13861, npc.getId(), 6)) {
                  st.setCond(cond + 1);
               }
               break;
            case 27381:
               if (st.getItemEquipped(5) == 13881 && (cond == 8 || cond == 10) && st.calcDropItems(this.getId(), 13862, npc.getId(), 6)) {
                  st.setCond(cond + 1);
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10275_ContainingTheAttributePower(10275, _10275_ContainingTheAttributePower.class.getSimpleName(), "");
   }
}
