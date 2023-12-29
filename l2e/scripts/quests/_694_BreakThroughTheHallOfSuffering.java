package l2e.scripts.quests;

import java.util.Calendar;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class _694_BreakThroughTheHallOfSuffering extends Quest {
   public _694_BreakThroughTheHallOfSuffering(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32603);
      this.addTalkId(32603);
      this.addTalkId(32530);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32603-02.htm")) {
            st.startQuest();
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
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId()) && player.getLevel() <= getMaxLvl(this.getId())) {
                  htmltext = "32603-01.htm";
               } else {
                  htmltext = "32603-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 32530:
                     ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
                     if (world != null && world.getTemplateId() == 115) {
                        if (world.getTag() == -1) {
                           htmltext = "32530-11.htm";
                        } else {
                           if (player.getParty() == null || player.getParty().getLeaderObjectId() != player.getObjectId()) {
                              return "32530-10.htm";
                           }

                           for(Player member : player.getParty().getMembers()) {
                              QuestState st1 = member.getQuestState(this.getName());
                              if (st1 != null) {
                                 if (world.getTag() == 13777) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 1);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-00.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13778) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 2);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-01.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13779) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 3);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-02.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13780) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 4);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-03.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13781) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 5);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-04.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13782) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 6);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-05.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13783) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 7);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-06.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13784) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 8);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-07.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13785) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 9);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-08.htm";
                                    finishInstance(player);
                                 } else if (world.getTag() == 13786) {
                                    if (st1.getQuestItemsCount(13691) == 0L) {
                                       st1.giveItems(13691, 1L);
                                    }

                                    st1.calcReward(this.getId(), 10);
                                    st1.exitQuest(true, true);
                                    htmltext = "32530-09.htm";
                                    finishInstance(player);
                                 } else {
                                    htmltext = "32530-11.htm";
                                 }
                              }
                           }
                        }
                        break;
                     }

                     htmltext = "32530-11.htm";
                     break;
                  case 32603:
                     htmltext = "32603-01a.htm";
               }
         }

         return htmltext;
      }
   }

   private static final void finishInstance(Player player) {
      ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
      Calendar reenter = Calendar.getInstance();
      reenter.set(12, 30);
      if (reenter.get(11) >= 6) {
         reenter.add(5, 1);
      }

      reenter.set(11, 6);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
      sm.addInstanceName(world.getTemplateId());

      for(int objectId : world.getAllowed()) {
         Player obj = World.getInstance().getPlayer(objectId);
         if (obj != null && obj.isOnline()) {
            ReflectionManager.getInstance().setReflectionTime(objectId, world.getTemplateId(), reenter.getTimeInMillis());
            obj.sendPacket(sm);
         }
      }

      Reflection inst = world.getReflection();
      inst.setDuration(300000);
      inst.setEmptyDestroyTime(0L);
   }

   public static void main(String[] args) {
      new _694_BreakThroughTheHallOfSuffering(694, _694_BreakThroughTheHallOfSuffering.class.getSimpleName(), "");
   }
}
