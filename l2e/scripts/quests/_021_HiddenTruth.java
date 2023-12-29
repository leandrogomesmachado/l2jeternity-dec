package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _021_HiddenTruth extends Quest {
   public Npc _ghostPage;
   public Npc _ghost;

   public _021_HiddenTruth(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31522);
      this.addTalkId(31522);
      this.addTalkId(31523);
      this.addTalkId(31524);
      this.addTalkId(31525);
      this.addTalkId(31526);
      this.addTalkId(31348);
      this.addTalkId(31350);
      this.addTalkId(31349);
      this.addTalkId(31328);
      this.questItemIds = new int[]{7140};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31522-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31328-05.htm")) {
            htmltext = "31328-05a.htm";
            st.takeItems(7140, 1L);
            if (st.getQuestItemsCount(7141) == 0L) {
               st.giveItems(7141, 1L);
            }

            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("31523-03.htm")) {
            st.setCond(2, true);
            this.despawnGhost(st);
            this.spawnGhost(st);
         } else if (event.equalsIgnoreCase("31524-06.htm")) {
            st.setCond(3, true);
            this.despawnGhostPage(st);
            this.spawnGhostPage(st);
            this.startQuestTimer("1", 4000L, this._ghostPage, player);
         } else if (event.equalsIgnoreCase("31526-03.htm")) {
            st.playSound("ItemSound.item_drop_equip_armor_cloth");
         } else if (event.equalsIgnoreCase("31526-08.htm")) {
            st.playSound("AmdSound.ed_chimes_05");
            st.setCond(5, false);
         } else if (event.equalsIgnoreCase("31526-14.htm")) {
            st.giveItems(7140, 1L);
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("1")) {
            this._ghostPage.getAI().setIntention(CtrlIntention.MOVING, new Location(52373, -54296, -3136, 0));
            st.startQuestTimer("2", 5000L, this._ghostPage);
         } else if (event.equalsIgnoreCase("2")) {
            this._ghostPage.getAI().setIntention(CtrlIntention.MOVING, new Location(52451, -52921, -3152, 0));
            st.startQuestTimer("3", 12000L, this._ghostPage);
         } else if (event.equalsIgnoreCase("3")) {
            this._ghostPage.getAI().setIntention(CtrlIntention.MOVING, new Location(51909, -51725, -3125, 0));
            st.startQuestTimer("4", 15000L, this._ghostPage);
         } else if (event.equalsIgnoreCase("4")) {
            this._ghostPage.getAI().setIntention(CtrlIntention.MOVING, new Location(52438, -51240, -3097, 0));
            st.startQuestTimer("5", 5000L, this._ghostPage);
         } else if (event.equalsIgnoreCase("5")) {
            this._ghostPage.getAI().setIntention(CtrlIntention.MOVING, new Location(52143, -51418, -3085, 0));
            return null;
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
               if (player.getLevel() > 54) {
                  htmltext = "31522-01.htm";
               } else {
                  htmltext = "31522-03.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31328:
                     switch(cond) {
                        case 0:
                           return "31328-06.htm";
                        case 7:
                           if (st.getQuestItemsCount(7140) != 0L) {
                              htmltext = "31328-01.htm";
                           }

                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31348:
                     if (st.getQuestItemsCount(7140) >= 1L) {
                        switch(cond) {
                           case 6:
                              if (st.getInt("DOMINIC") == 1 && st.getInt("BENEDICT") == 1) {
                                 htmltext = "31348-02.htm";
                                 st.setCond(7, true);
                              } else {
                                 st.set("AGRIPEL", "1");
                                 htmltext = "31348-0" + getRandom(3) + ".htm";
                              }

                              return htmltext;
                           case 7:
                              htmltext = "31348-03.htm";
                        }
                     }

                     return htmltext;
                  case 31349:
                     if (st.getQuestItemsCount(7140) >= 1L) {
                        switch(cond) {
                           case 6:
                              if (st.getInt("AGRIPEL") == 1 && st.getInt("DOMINIC") == 1) {
                                 htmltext = "31349-02.htm";
                                 st.setCond(7, true);
                              } else {
                                 st.set("BENEDICT", "1");
                                 htmltext = "31349-0" + getRandom(3) + ".htm";
                              }

                              return htmltext;
                           case 7:
                              htmltext = "31349-03.htm";
                        }
                     }

                     return htmltext;
                  case 31350:
                     if (st.getQuestItemsCount(7140) >= 1L) {
                        switch(cond) {
                           case 6:
                              if (st.getInt("AGRIPEL") == 1 && st.getInt("BENEDICT") == 1) {
                                 htmltext = "31350-02.htm";
                                 st.setCond(7, true);
                              } else {
                                 st.set("DOMINIC", "1");
                                 htmltext = "31350-0" + getRandom(3) + ".htm";
                              }

                              return htmltext;
                           case 7:
                              htmltext = "31350-03.htm";
                        }
                     }

                     return htmltext;
                  case 31522:
                     if (cond == 1) {
                        htmltext = "31522-05.htm";
                     }

                     return htmltext;
                  case 31523:
                     switch(cond) {
                        case 1:
                           return "31523-01.htm";
                        case 2:
                        case 3:
                           htmltext = "31523-04.htm";
                           st.playSound("SkillSound5.horror_02");
                           this.despawnGhost(st);
                           this.spawnGhost(st);
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31524:
                     switch(cond) {
                        case 2:
                           return "31524-01.htm";
                        case 3:
                           return "31524-07b.htm";
                        case 4:
                           return "31524-07c.htm";
                        default:
                           return htmltext;
                     }
                  case 31525:
                     switch(cond) {
                        case 3:
                        case 4:
                           htmltext = "31525-01.htm";
                           if (this._ghostPage.isMoving()) {
                              return "31525-01.htm";
                           }

                           htmltext = "31525-02.htm";
                           if (cond == 3) {
                              st.setCond(4, true);
                           }

                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31526:
                     switch(cond) {
                        case 3:
                        case 4:
                           htmltext = "31525-01.htm";
                           if (!this._ghostPage.isMoving()) {
                              this.despawnGhostPage(st);
                              this.despawnGhost(st);
                              st.setCond(5, true);
                              htmltext = "31526-01.htm";
                           }

                           return htmltext;
                        case 5:
                           htmltext = "31526-10.htm";
                           st.playSound("AmdSound.ed_chimes_05");
                           return htmltext;
                        case 6:
                           htmltext = "31526-15.htm";
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

   private void spawnGhostPage(QuestState st) {
      this._ghostPage = st.addSpawn(31525, 51462, -54539, -3176, getRandom(0, 20), true, 0);
      NpcSay packet = new NpcSay(this._ghostPage.getObjectId(), 22, this._ghostPage.getId(), NpcStringId.MY_MASTER_HAS_INSTRUCTED_ME_TO_BE_YOUR_GUIDE_S1);
      packet.addStringParameter(st.getPlayer().getName().toString());
      this._ghostPage.broadcastPacket(packet, 2000);
   }

   private void despawnGhostPage(QuestState st) {
      if (this._ghostPage != null) {
         this._ghostPage.deleteMe();
      }

      this._ghostPage = null;
   }

   private void spawnGhost(QuestState st) {
      this._ghost = st.addSpawn(31524, 51432, -54570, -3136, getRandom(0, 20), false, 0);
      this._ghost.broadcastPacket(new NpcSay(this._ghost.getObjectId(), 0, this._ghost.getId(), NpcStringId.WHO_AWOKE_ME), 2000);
   }

   private void despawnGhost(QuestState st) {
      if (this._ghost != null) {
         this._ghost.deleteMe();
      }

      this._ghost = null;
   }

   public static void main(String[] args) {
      new _021_HiddenTruth(21, _021_HiddenTruth.class.getSimpleName(), "");
   }
}
