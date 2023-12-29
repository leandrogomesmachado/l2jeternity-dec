package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _242_PossessorOfAPreciousSoul_2 extends Quest {
   public _242_PossessorOfAPreciousSoul_2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31742);
      this.addTalkId(new int[]{31742, 31743, 31744, 31751, 31752, 30759, 30738, 31746, 31748, 31747});
      this.addKillId(27317);
      this.questItemIds = new int[]{7590, 7595, 7596};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else if (!player.isSubClassActive()) {
         return "sub.htm";
      } else {
         switch(event) {
            case "31742-02.htm":
               st.startQuest();
               st.takeItems(7677, -1L);
               break;
            case "31743-05.htm":
               st.setCond(2, true);
               break;
            case "31744-02.htm":
               st.setCond(3, true);
               break;
            case "31751-02.htm":
               st.setCond(4, true);
               break;
            case "30759-02.htm":
               st.setCond(7, true);
               break;
            case "30738-02.htm":
               st.setCond(8, true);
               st.giveItems(7596, 1L);
               break;
            case "30759-05.htm":
               st.takeItems(7590, -1L);
               st.takeItems(7596, -1L);
               st.set("awaitsDrops", "1");
               st.setCond(9, true);
               break;
            case "PURE_UNICORN":
               npc.getSpawn().stopRespawn();
               npc.deleteMe();
               Npc npc_pure = st.addSpawn(31747, 85884, -76588, -3470, 30000);
               this.startQuestTimer("FALLEN_UNICORN", 30000L, npc_pure, player);
               return null;
            case "FALLEN_UNICORN":
               Npc npc_fallen = st.addSpawn(31746, 85884, -76588, -3470, 0);
               npc_fallen.getSpawn().startRespawn();
               return null;
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else if (st.isStarted() && !player.isSubClassActive()) {
         return "sub.htm";
      } else {
         switch(npc.getId()) {
            case 30738:
               switch(st.getCond()) {
                  case 7:
                     return "30738-01.htm";
                  case 8:
                     return "30738-03.htm";
                  default:
                     return htmltext;
               }
            case 30759:
               switch(st.getCond()) {
                  case 6:
                     return "30759-01.htm";
                  case 7:
                     return "30759-03.htm";
                  case 8:
                     if (st.hasQuestItems(7596)) {
                        htmltext = "30759-04.htm";
                     }

                     return htmltext;
                  case 9:
                     return "30759-06.htm";
                  default:
                     return htmltext;
               }
            case 31742:
               switch(st.getState()) {
                  case 0:
                     if (st.hasQuestItems(7677)) {
                        htmltext = player.isSubClassActive() && player.getLevel() >= 60 ? "31742-01.htm" : "31742-00.htm";
                     }

                     return htmltext;
                  case 1:
                     switch(st.getCond()) {
                        case 1:
                           return "31742-03.htm";
                        case 11:
                           htmltext = "31742-04.htm";
                           st.calcExpAndSp(this.getId());
                           st.calcReward(this.getId());
                           st.exitQuest(false, true);
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 2:
                     return getAlreadyCompletedMsg(player);
                  default:
                     return htmltext;
               }
            case 31743:
               switch(st.getCond()) {
                  case 1:
                     return "31743-01.htm";
                  case 2:
                     return "31743-06.htm";
                  case 11:
                     return "31743-07.htm";
                  default:
                     return htmltext;
               }
            case 31744:
               switch(st.getCond()) {
                  case 2:
                     return "31744-01.htm";
                  case 3:
                     htmltext = "31744-03.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31746:
               switch(st.getCond()) {
                  case 9:
                     return "31746-01.htm";
                  case 10:
                     htmltext = "31746-02.htm";
                     this.startQuestTimer("PURE_UNICORN", 3000L, npc, player);
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31747:
               switch(st.getCond()) {
                  case 10:
                     st.setCond(11, true);
                     return "31747-01.htm";
                  case 11:
                     htmltext = "31747-02.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31748:
               if (st.isCond(9)) {
                  if (st.hasQuestItems(7595)) {
                     htmltext = "31748-02.htm";
                     st.takeItems(7595, 1L);
                     npc.doDie(npc);
                     st.set("cornerstones", Integer.toString(st.getInt("cornerstones") + 1));
                     if (st.getInt("cornerstones") == 4) {
                        st.setCond(10);
                     }

                     st.playSound("ItemSound.quest_middle");
                     npc.setTarget(player);
                     npc.doCast(SkillsParser.getInstance().getInfo(4546, 1));
                  } else {
                     htmltext = "31748-01.htm";
                  }
               }
               break;
            case 31751:
               switch(st.getCond()) {
                  case 3:
                     return "31751-01.htm";
                  case 4:
                     return "31751-03.htm";
                  case 5:
                     if (st.hasQuestItems(7590)) {
                        st.setCond(6, true);
                        htmltext = "31751-04.htm";
                     }

                     return htmltext;
                  case 6:
                     htmltext = "31751-05.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31752:
               switch(st.getCond()) {
                  case 4:
                     npc.doDie(npc);
                     if (Rnd.chance(30)) {
                        st.giveItems(7590, 1L);
                        st.setCond(5, true);
                        htmltext = "31752-01.htm";
                     } else {
                        htmltext = "31752-02.htm";
                     }
                     break;
                  case 5:
                     htmltext = "31752-02.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, "awaitsDrops", "1");
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(9) && st.calcDropItems(this.getId(), 7595, npc.getId(), 4)) {
            st.unset("awaitsDrops");
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _242_PossessorOfAPreciousSoul_2(242, _242_PossessorOfAPreciousSoul_2.class.getSimpleName(), "");
   }
}
