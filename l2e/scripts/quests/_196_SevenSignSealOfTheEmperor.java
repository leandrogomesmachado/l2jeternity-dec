package l2e.scripts.quests;

import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _196_SevenSignSealOfTheEmperor extends Quest {
   public _196_SevenSignSealOfTheEmperor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30969);
      this.addTalkId(new int[]{30969, 32593, 32584, 32598, 32586, 32587, 32657});
      this.questItemIds = new int[]{15310, 13808, 13846, 13809};
   }

   protected void exitInstance(Player player) {
      player.setReflectionId(0);
      player.teleToLocation(171782, -17612, -4901, true);
      ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
      if (world != null) {
         Reflection inst = world.getReflection();
         inst.setDuration(300000);
         inst.setEmptyDestroyTime(0L);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (event.equalsIgnoreCase("30969-05.htm")) {
         st.startQuest();
      } else if (event.equalsIgnoreCase("32598-02.htm")) {
         st.giveItems(13809, 1L);
         st.playSound("ItemSound.quest_middle");
      } else if (event.equalsIgnoreCase("30969-11.htm")) {
         st.setCond(6, true);
      } else if (event.equalsIgnoreCase("32584-05.htm")) {
         st.setCond(2, true);
         npc.deleteMe();
      } else if (event.equalsIgnoreCase("32586-06.htm")) {
         st.setCond(4, true);
         st.giveItems(15310, 1L);
         st.giveItems(13808, 1L);
      } else if (event.equalsIgnoreCase("32586-12.htm")) {
         st.setCond(5, true);
         st.takeItems(13846, 4L);
         st.takeItems(15310, 1L);
         st.takeItems(13808, 1L);
         st.takeItems(13809, 1L);
      } else if (event.equalsIgnoreCase("32593-02.htm")) {
         st.calcExpAndSp(this.getId());
         st.exitQuest(false, true);
      } else if (event.equalsIgnoreCase("30969-06.htm")) {
         if (World.getInstance().getNpcById(32584) != null) {
            return "30969-06a.htm";
         }

         Npc mammon = addSpawn(32584, 109742, 219978, -3520, 0, false, 120000L, true);
         mammon.broadcastPacket(new NpcSay(mammon.getObjectId(), 0, mammon.getId(), NpcStringId.WHO_DARES_SUMMON_THE_MERCHANT_OF_MAMMON), 2000);
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 30969:
               if (player.getLevel() < 79) {
                  st.exitQuest(true);
                  htmltext = "30969-00.htm";
               }

               QuestState qs = player.getQuestState(_195_SevenSignSecretRitualOfThePriests.class.getSimpleName());
               if (qs == null) {
                  return htmltext;
               }

               if (qs.isCompleted() && st.getState() == 0) {
                  htmltext = "30969-01.htm";
               } else {
                  switch(cond) {
                     case 0:
                        st.exitQuest(true);
                        return "30969-00.htm";
                     case 1:
                        return "30969-05.htm";
                     case 2:
                        st.set("cond", "3");
                        return "30969-08.htm";
                     case 3:
                     case 4:
                     default:
                        return htmltext;
                     case 5:
                        return "30969-09.htm";
                     case 6:
                        htmltext = "30969-11.htm";
                  }
               }
               break;
            case 32584:
               switch(cond) {
                  case 1:
                     htmltext = "32584-01.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 32586:
               switch(cond) {
                  case 3:
                     return "32586-01.htm";
                  case 4:
                     if (st.getQuestItemsCount(15310) == 0L) {
                        st.giveItems(15310, 1L);
                        htmltext = "32586-14.htm";
                     }

                     if (st.getQuestItemsCount(13808) == 0L) {
                        st.giveItems(13808, 1L);
                        htmltext = "32586-14.htm";
                     }

                     if (st.getQuestItemsCount(13846) <= 3L) {
                        htmltext = "32586-07.htm";
                     }

                     if (st.getQuestItemsCount(13846) >= 4L) {
                        htmltext = "32586-08.htm";
                     }

                     return htmltext;
                  case 5:
                     htmltext = "32586-13.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 32587:
               if (st.getCond() >= 3) {
                  this.exitInstance(player);
                  htmltext = "32587-02.htm";
               }
               break;
            case 32593:
               if (cond == 6) {
                  htmltext = "32593-01.htm";
               } else if (st.getState() == 2) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
               break;
            case 32598:
               switch(cond) {
                  case 4:
                     if (st.getQuestItemsCount(13809) == 0L) {
                        htmltext = "32598-01.htm";
                     }

                     if (st.getQuestItemsCount(13809) >= 1L) {
                        htmltext = "32598-03.htm";
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 32657:
               switch(cond) {
                  case 4:
                     htmltext = "32657-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _196_SevenSignSealOfTheEmperor(196, _196_SevenSignSealOfTheEmperor.class.getSimpleName(), "");
   }
}
