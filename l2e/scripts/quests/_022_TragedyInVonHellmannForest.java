package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _022_TragedyInVonHellmannForest extends Quest {
   private Npc _ghost = null;
   private Npc _soul = null;

   public _022_TragedyInVonHellmannForest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{31334, 31328});
      this.addTalkId(new int[]{31328, 31334, 31528, 31529, 31527});
      this.addAttackId(27217);
      this.addKillId(new int[]{27217, 21553, 21554, 21555, 21556, 21561});
      this.questItemIds = new int[]{7142, 7147, 7146, 7143, 7145, 7144};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31334-03.htm")) {
            QuestState st2 = player.getQuestState("_021_HiddenTruth");
            if (st2 != null && st2.isCompleted() && player.getLevel() >= 63) {
               htmltext = "31334-02.htm";
            }
         } else if (event.equalsIgnoreCase("31334-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31334-07.htm")) {
            if (!st.hasQuestItems(7141)) {
               st.setCond(2, false);
            } else {
               htmltext = "31334-06.htm";
            }
         } else if (event.equalsIgnoreCase("31334-08.htm")) {
            if (st.hasQuestItems(7141)) {
               st.setCond(4, true);
               st.takeItems(7141, 1L);
            } else {
               st.setCond(2, false);
               htmltext = "31334-07.htm";
            }
         } else if (event.equalsIgnoreCase("31334-13.htm")) {
            if (this._ghost != null) {
               st.set("cond", "6");
               htmltext = "31334-14.htm";
            } else {
               st.setCond(7, true);
               st.takeItems(7142, 1L);
               this._ghost = addSpawn(31528, 38418, -49894, -1104, 0, false, 120000L, true);
               this._ghost
                  .broadcastPacket(
                     new NpcSay(this._ghost.getObjectId(), 22, this._ghost.getId(), NpcStringId.DID_YOU_CALL_ME_S1).addStringParameter(player.getName()), 2000
                  );
               this.startQuestTimer("ghost_cleanup", 118000L, null, player, false);
            }
         } else if (event.equalsIgnoreCase("31528-08.htm")) {
            st.setCond(8, true);
            this.cancelQuestTimer("ghost_cleanup", null, player);
            if (this._ghost != null) {
               this._ghost.deleteMe();
               this._ghost = null;
            }
         } else if (event.equalsIgnoreCase("31328-10.htm")) {
            st.setCond(9, true);
            st.giveItems(7143, 1L);
         } else if (event.equalsIgnoreCase("31529-12.htm")) {
            st.setCond(10, true);
            st.takeItems(7143, 1L);
            st.giveItems(7144, 1L);
         } else if (event.equalsIgnoreCase("31527-02.htm")) {
            if (this._soul == null) {
               this._soul = addSpawn(27217, 34860, -54542, -2048, 0, false, 0L, true);
               ((Attackable)this._soul).addDamageHate(player, 0, 99999);
               this._soul.getAI().setIntention(CtrlIntention.ATTACK, player, Boolean.valueOf(true));
            }
         } else if (event.equalsIgnoreCase("attack_timer")) {
            st.setCond(11, true);
            st.takeItems(7144, 1L);
            st.giveItems(7145, 1L);
         } else if (event.equalsIgnoreCase("31328-13.htm")) {
            st.setCond(15, true);
            st.takeItems(7147, 1L);
         } else if (event.equalsIgnoreCase("31328-21.htm")) {
            st.setCond(16, true);
         } else if (event.equalsIgnoreCase("ghost_cleanup")) {
            this._ghost
               .broadcastPacket(new NpcSay(this._ghost.getObjectId(), 22, this._ghost.getId(), NpcStringId.IM_CONFUSED_MAYBE_ITS_TIME_TO_GO_BACK), 2000);
            this._ghost = null;
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
         switch(st.getState()) {
            case 0:
               switch(npc.getId()) {
                  case 31328:
                     QuestState st2 = player.getQuestState("_021_HiddenTruth");
                     if (st2 != null && st2.isCompleted()) {
                        if (!st.hasQuestItems(7141)) {
                           htmltext = "31328-01.htm";
                           st.giveItems(7141, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "31328-01b.htm";
                        }

                        return htmltext;
                     }

                     return htmltext;
                  case 31334:
                     return "31334-01.htm";
                  default:
                     return htmltext;
               }
            case 1:
               int cond = st.getCond();
               switch(npc.getId()) {
                  case 31328:
                     if (cond < 3) {
                        if (!st.hasQuestItems(7141)) {
                           htmltext = "31328-01.htm";
                           st.setCond(3);
                           st.playSound("ItemSound.quest_itemget");
                           st.giveItems(7141, 1L);
                        } else {
                           htmltext = "31328-01b.htm";
                        }

                        return htmltext;
                     } else if (cond == 3) {
                        return "31328-02.htm";
                     } else if (cond == 8) {
                        return "31328-03.htm";
                     } else if (cond == 9) {
                        return "31328-11.htm";
                     } else if (cond == 14) {
                        if (st.hasQuestItems(7147)) {
                           htmltext = "31328-12.htm";
                        } else {
                           st.set("cond", "13");
                        }

                        return htmltext;
                     } else {
                        if (cond == 15) {
                           htmltext = "31328-14.htm";
                        } else if (cond == 16) {
                           htmltext = player.getLevel() < 64 ? "31328-23.htm" : "31328-22.htm";
                           st.calcExpAndSp(this.getId());
                           st.exitQuest(false, true);
                        }

                        return htmltext;
                     }
                  case 31334:
                     if (cond == 1 || cond == 2 || cond == 3) {
                        return "31334-05.htm";
                     } else if (cond == 4) {
                        return "31334-09.htm";
                     } else if (cond != 5 && cond != 6) {
                        if (cond == 7) {
                           htmltext = this._ghost != null ? "31334-15.htm" : "31334-17.htm";
                        } else if (cond > 7) {
                           return "31334-18.htm";
                        }

                        return htmltext;
                     } else {
                        if (st.hasQuestItems(7142)) {
                           htmltext = this._ghost == null ? "31334-10.htm" : "31334-11.htm";
                        } else {
                           htmltext = "31334-09.htm";
                           st.set("cond", "4");
                        }

                        return htmltext;
                     }
                  case 31527:
                     if (cond == 10) {
                        return "31527-01.htm";
                     } else if (cond == 11) {
                        return "31527-03.htm";
                     } else {
                        if (cond == 12) {
                           htmltext = "31527-04.htm";
                           st.setCond(13, true);
                           st.giveItems(7146, 1L);
                        } else if (cond > 12) {
                           return "31527-05.htm";
                        }

                        return htmltext;
                     }
                  case 31528:
                     if (cond == 7) {
                        htmltext = "31528-01.htm";
                     } else if (cond == 8) {
                        return "31528-08.htm";
                     }

                     return htmltext;
                  case 31529:
                     if (cond == 9) {
                        if (st.hasQuestItems(7143)) {
                           htmltext = "31529-01.htm";
                        } else {
                           htmltext = "31529-10.htm";
                           st.set("cond", "8");
                        }

                        return htmltext;
                     } else if (cond == 10) {
                        return "31529-16.htm";
                     } else if (cond == 11) {
                        if (st.hasQuestItems(7145)) {
                           htmltext = "31529-17.htm";
                           st.setCond(12, true);
                           st.takeItems(7145, 1L);
                        } else {
                           htmltext = "31529-09.htm";
                           st.set("cond", "10");
                        }

                        return htmltext;
                     } else if (cond == 12) {
                        return "31529-17.htm";
                     } else if (cond == 13) {
                        if (st.hasQuestItems(7146)) {
                           htmltext = "31529-18.htm";
                           st.setCond(14, true);
                           st.takeItems(7146, 1L);
                           st.giveItems(7147, 1L);
                        } else {
                           htmltext = "31529-10.htm";
                           st.set("cond", "12");
                        }

                        return htmltext;
                     } else {
                        if (cond > 13) {
                           htmltext = "31529-19.htm";
                           return htmltext;
                        }

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

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
      QuestState st = attacker.getQuestState(this.getName());
      if (st == null || !st.isStarted() || isPet) {
         return null;
      } else if (this.getQuestTimer("attack_timer", null, attacker) != null) {
         return null;
      } else {
         if (st.getInt("cond") == 10) {
            this.startQuestTimer("attack_timer", 20000L, null, attacker, false);
         }

         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (npc.getId() != 27217) {
            if (st.getCond() == 4 && st.calcDropItems(this.getId(), 7142, npc.getId(), 1)) {
               st.setCond(5);
            }
         } else {
            this.cancelQuestTimer("attack_timer", null, player);
            this._soul = null;
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _022_TragedyInVonHellmannForest(22, _022_TragedyInVonHellmannForest.class.getSimpleName(), "");
   }
}
