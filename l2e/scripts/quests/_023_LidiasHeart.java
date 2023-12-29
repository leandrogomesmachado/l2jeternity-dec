package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _023_LidiasHeart extends Quest {
   public Npc _ghost;

   public _023_LidiasHeart(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31328);
      this.addTalkId(31328);
      this.addTalkId(31526);
      this.addTalkId(31524);
      this.addTalkId(31523);
      this.addTalkId(31386);
      this.addTalkId(31530);
      this.questItemIds = new int[]{7063, 7149, 7148, 7064, 7150};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31328-02.htm")) {
            st.giveItems(7063, 1L);
            st.giveItems(7149, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31328-03.htm")) {
            st.setCond(2, false);
         } else if (event.equalsIgnoreCase("31526-01.htm")) {
            st.setCond(3, false);
         } else if (event.equalsIgnoreCase("31526-05.htm")) {
            st.giveItems(7148, 1L);
            if (st.getQuestItemsCount(7064) != 0L) {
               st.setCond(4, true);
            }
         } else if (event.equalsIgnoreCase("31526-11.htm")) {
            st.giveItems(7064, 1L);
            if (st.getQuestItemsCount(7148) != 0L) {
               st.setCond(4, true);
            }
         } else if (event.equalsIgnoreCase("31328-19.htm")) {
            st.setCond(6, false);
         } else if (event.equalsIgnoreCase("31524-04.htm")) {
            st.setCond(7, true);
            st.takeItems(7064, -1L);
         } else if (event.equalsIgnoreCase("31523-02.htm")) {
            this.despawnGhost(st);
            this.spawnGhost(st);
            st.playSound("SkillSound5.horror_02");
         } else if (event.equalsIgnoreCase("31523-05.htm")) {
            st.startQuestTimer("viwer_timer", 10000L);
         } else if (event.equalsIgnoreCase("viwer_timer")) {
            htmltext = "31523-06.htm";
            st.setCond(8, true);
         } else if (event.equalsIgnoreCase("31530-02.htm")) {
            st.takeItems(7149, -1L);
            st.giveItems(7150, 1L);
            st.setCond(10, true);
         } else if (event.equalsIgnoreCase("i7064-02.htm")) {
            htmltext = "i7064-02.htm";
         } else if (event.equalsIgnoreCase("31526-13.htm")) {
            st.startQuestTimer("read_book", 120000L);
         } else if (event.equalsIgnoreCase("read_book")) {
            htmltext = "i7064.htm";
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_022_TragedyInVonHellmannForest");
               if (qs != null && qs.isCompleted()) {
                  htmltext = "31328-01.htm";
               } else {
                  htmltext = "31328-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31328:
                     switch(cond) {
                        case 1:
                           return "31328-03.htm";
                        case 2:
                           return "31328-07.htm";
                        case 3:
                        case 5:
                        default:
                           return htmltext;
                        case 4:
                           return "31328-08.htm";
                        case 6:
                           return "31328-19.htm";
                     }
                  case 31386:
                     switch(cond) {
                        case 8:
                           htmltext = "31386-01.htm";
                           st.setCond(9, false);
                           return htmltext;
                        case 9:
                           return "31386-02.htm";
                        case 10:
                           if (st.getQuestItemsCount(7150) != 0L) {
                              htmltext = "31386-03.htm";
                              st.takeItems(7150, -1L);
                              st.calcExpAndSp(this.getId());
                              st.calcReward(this.getId());
                              st.exitQuest(false, true);
                           } else {
                              htmltext = "31386-03a.htm";
                           }

                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31523:
                     switch(cond) {
                        case 6:
                           if (st.getQuestTimer("spawn_timer") != null) {
                              htmltext = "31523-03.htm";
                           } else {
                              htmltext = "31523-01.htm";
                           }

                           return htmltext;
                        case 7:
                           return "31523-04.htm";
                        case 8:
                           return "31523-06.htm";
                        default:
                           return htmltext;
                     }
                  case 31524:
                     switch(cond) {
                        case 6:
                           return "31524-01.htm";
                        case 7:
                           return "31524-05.htm";
                        default:
                           return htmltext;
                     }
                  case 31526:
                     switch(cond) {
                        case 2:
                           if (st.getQuestItemsCount(7149) != 0L) {
                              htmltext = "31526-00.htm";
                           }

                           return htmltext;
                        case 3:
                           if (st.getQuestItemsCount(7148) == 0L) {
                              if (st.getQuestItemsCount(7064) == 0L) {
                                 htmltext = "31526-02.htm";
                              } else {
                                 htmltext = "31526-12.htm";
                              }

                              return htmltext;
                           } else {
                              if (st.getQuestItemsCount(7064) == 0L) {
                                 return "31526-06.htm";
                              }

                              return htmltext;
                           }
                        case 4:
                           return "31526-13.htm";
                        default:
                           return htmltext;
                     }
                  case 31530:
                     switch(cond) {
                        case 9:
                           if (st.getQuestItemsCount(7149) != 0L) {
                              htmltext = "31530-01.htm";
                           } else {
                              htmltext = "31530-01a.htm";
                           }

                           return htmltext;
                        case 10:
                           htmltext = "31386-03.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   private void spawnGhost(QuestState st) {
      this._ghost = st.addSpawn(31524, 51432, -54570, -3136, getRandom(0, 20), false, 180000);
      this._ghost.broadcastPacket(new NpcSay(this._ghost.getObjectId(), 0, this._ghost.getId(), NpcStringId.WHO_AWOKE_ME), 2000);
   }

   private void despawnGhost(QuestState st) {
      if (this._ghost != null) {
         this._ghost.deleteMe();
      }

      this._ghost = null;
   }

   public static void main(String[] args) {
      new _023_LidiasHeart(23, _023_LidiasHeart.class.getSimpleName(), "");
   }
}
