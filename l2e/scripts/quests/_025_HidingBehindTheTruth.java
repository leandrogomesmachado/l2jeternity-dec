package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _025_HidingBehindTheTruth extends Quest {
   public _025_HidingBehindTheTruth(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31349);
      this.addTalkId(31348);
      this.addTalkId(31349);
      this.addTalkId(31533);
      this.addTalkId(31534);
      this.addTalkId(31535);
      this.addTalkId(31522);
      this.addTalkId(31532);
      this.addTalkId(31531);
      this.addTalkId(31536);
      this.addKillId(27218);
      this.questItemIds = new int[]{7157, 7066, 7155, 7158, 7156};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = event;
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31349-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31349-03.htm")) {
            if (st.getQuestItemsCount(7156) >= 1L) {
               htmltext = "31349-05.htm";
            } else {
               st.setCond(2, true);
            }
         } else if (event.equalsIgnoreCase("31349-10.htm")) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("31348-02.htm")) {
            st.takeItems(7156, -1L);
         } else if (event.equalsIgnoreCase("31348-07.htm")) {
            st.setCond(5, true);
            st.giveItems(7157, 1L);
         } else if (event.equalsIgnoreCase("31522-04.htm")) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("31535-03.htm")) {
            if (st.getInt("step") == 0) {
               st.set("step", "1");
               Npc triol = st.addSpawn(27218, 59712, -47568, -2712, 0, false, 300000, true);
               triol.broadcastPacket(new NpcSay(triol.getObjectId(), 0, triol.getId(), NpcStringId.THAT_BOX_WAS_SEALED_BY_MY_MASTER_S1_DONT_TOUCH_IT), 2000);
               triol.setRunning();
               ((Attackable)triol).addDamageHate(player, 0, 999);
               triol.getAI().setIntention(CtrlIntention.ATTACK, player);
               st.setCond(7, true);
            } else if (st.getInt("step") == 2) {
               htmltext = "31535-04.htm";
            }
         } else if (event.equalsIgnoreCase("31535-05.htm")) {
            st.giveItems(7066, 1L);
            st.takeItems(7157, -1L);
            st.setCond(9, true);
         } else if (event.equalsIgnoreCase("31532-02.htm")) {
            st.takeItems(7066, -1L);
         } else if (event.equalsIgnoreCase("31532-06.htm")) {
            st.setCond(11, true);
         } else if (event.equalsIgnoreCase("31531-02.htm")) {
            st.setCond(12, true);
            st.addSpawn(31536, 60104, -35820, -664, 0, false, 20000, true);
         } else if (event.equalsIgnoreCase("31532-18.htm")) {
            st.setCond(15, true);
         } else if (event.equalsIgnoreCase("31522-12.htm")) {
            st.setCond(16, true);
         } else if (event.equalsIgnoreCase("31348-10.htm")) {
            st.takeItems(7158, -1L);
         } else if (event.equalsIgnoreCase("31348-15.htm")) {
            st.setCond(17, true);
         } else if (event.equalsIgnoreCase("31348-16.htm")) {
            st.setCond(18, true);
         } else if (event.equalsIgnoreCase("31532-20.htm")) {
            st.takeItems(7063, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId(), 1);
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("31522-15.htm")) {
            st.takeItems(7063, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId(), 2);
            st.exitQuest(false, true);
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
         byte id = st.getState();
         if (id == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0) {
            if (npcId == 31349) {
               QuestState st2 = player.getQuestState("_024_InhabitantsOfTheForrestOfTheDead");
               if (st2 != null) {
                  if (st2.getState() == 2 && player.getLevel() >= 66) {
                     htmltext = "31349-01.htm";
                  } else {
                     htmltext = "31349-00.htm";
                  }
               } else {
                  htmltext = "31349-00.htm";
               }
            }
         } else if (id == 1) {
            if (npcId == 31349) {
               if (cond == 1) {
                  htmltext = "31349-02.htm";
               } else if (cond == 2 || cond == 3) {
                  htmltext = "31349-04.htm";
               } else if (cond == 4) {
                  htmltext = "31349-10.htm";
               }
            } else if (npcId == 31522) {
               if (cond == 2) {
                  htmltext = "31522-01.htm";
                  st.setCond(3, true);
                  st.giveItems(7156, 1L);
               } else if (cond == 3) {
                  htmltext = "31522-02.htm";
               } else if (cond == 5) {
                  htmltext = "31522-03.htm";
               } else if (cond == 6) {
                  htmltext = "31522-04.htm";
               } else if (cond == 9) {
                  htmltext = "31522-05.htm";
                  st.setCond(10, true);
               } else if (cond == 10) {
                  htmltext = "31522-05.htm";
               } else if (cond == 15) {
                  htmltext = "31522-06.htm";
               } else if (cond == 16) {
                  htmltext = "31522-13.htm";
               } else if (cond == 17) {
                  htmltext = "31522-16.htm";
               } else if (cond == 18) {
                  htmltext = "31522-14.htm";
               }
            } else if (npcId == 31348) {
               if (cond == 4) {
                  htmltext = "31348-01.htm";
               } else if (cond == 5) {
                  htmltext = "31348-08.htm";
               } else if (cond == 16) {
                  htmltext = "31348-09.htm";
               } else if (cond == 17) {
                  htmltext = "31348-17.htm";
               } else if (cond == 18) {
                  htmltext = "31348-18.htm";
               }
            } else if (npcId == 31533) {
               if (cond == 6) {
                  htmltext = "31533-01.htm";
               } else if (npcId == 31534) {
                  if (cond == 6) {
                     htmltext = "31534-01.htm";
                  } else if (npcId == 31535) {
                     if (cond >= 6 && cond <= 8) {
                        htmltext = "31535-01.htm";
                     } else if (cond == 9) {
                        htmltext = "31535-06.htm";
                     }
                  } else if (npcId == 31532) {
                     if (cond == 10) {
                        htmltext = "31532-01.htm";
                     } else if (cond == 11 || cond == 12) {
                        htmltext = "31532-06.htm";
                     } else if (cond == 13) {
                        htmltext = "31532-07.htm";
                        st.setCond(14, false);
                        st.takeItems(7155, -1L);
                     } else if (cond == 14) {
                        htmltext = "31532-08.htm";
                     } else if (cond == 15) {
                        htmltext = "31532-18.htm";
                     } else if (cond == 17) {
                        htmltext = "31532-19.htm";
                     } else if (cond == 18) {
                        htmltext = "31532-21.htm";
                     }
                  } else if (npcId == 31531) {
                     if (cond == 11 || cond == 12) {
                        htmltext = "31531-01.htm";
                     } else if (cond == 13) {
                        htmltext = "31531-03.htm";
                     } else if (npcId == 31536 && cond == 12) {
                        htmltext = "31536-01.htm";
                        st.giveItems(7155, 1L);
                        st.setCond(13, true);
                        npc.deleteMe();
                     }
                  }
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
         if (st.getCond() == 7) {
            st.playSound("ItemSound.quest_itemget");
            st.setCond(8, false);
            npc.broadcastPacket(
               new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUVE_ENDED_MY_IMMORTAL_LIFE_YOURE_PROTECTED_BY_THE_FEUDAL_LORD_ARENT_YOU), 2000
            );
            st.giveItems(7158, 1L);
            st.set("step", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _025_HidingBehindTheTruth(25, _025_HidingBehindTheTruth.class.getSimpleName(), "");
   }
}
