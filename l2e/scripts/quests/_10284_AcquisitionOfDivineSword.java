package l2e.scripts.quests;

import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _10284_AcquisitionOfDivineSword extends Quest {
   public _10284_AcquisitionOfDivineSword(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32020);
      this.addTalkId(new int[]{32020, 32760, 32654, 32653});
      this.registerQuestItems(new int[]{15514});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "32020-02.htm":
               qs.startQuest();
               qs.setMemoState(1);
               htmltext = event;
               break;
            case "32020-03.htm":
            case "32760-02a.htm":
            case "32760-02b.htm":
            case "32760-03a.htm":
            case "32760-03b.htm":
            case "32760-04a.htm":
            case "32760-04b.htm":
               if (qs.isMemoState(1)) {
                  htmltext = event;
               }
               break;
            case "32760-02c.htm":
               if (qs.isMemoState(1)) {
                  qs.set("ex1", 1);
                  htmltext = event;
               }
               break;
            case "another_story":
               if (qs.isMemoState(1)) {
                  if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 0) {
                     htmltext = "32760-05a.htm";
                  } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 0) {
                     htmltext = "32760-05b.htm";
                  } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 1) {
                     htmltext = "32760-05c.htm";
                  } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                     htmltext = "32760-05d.htm";
                  } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 1) {
                     htmltext = "32760-05e.htm";
                  } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 0) {
                     htmltext = "32760-05f.htm";
                  } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                     htmltext = "32760-05g.htm";
                  }
               }
               break;
            case "32760-03c.htm":
               if (qs.isMemoState(1)) {
                  qs.set("ex2", 1);
                  htmltext = event;
               }
               break;
            case "32760-04c.htm":
               if (qs.isMemoState(1)) {
                  qs.set("ex3", 1);
                  htmltext = event;
               }
               break;
            case "32760-06.htm":
               if (qs.isMemoState(1) && qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                  htmltext = event;
               }
               break;
            case "32760-07.htm":
               if (qs.isMemoState(1) && qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                  qs.unset("ex1");
                  qs.unset("ex2");
                  qs.unset("ex3");
                  qs.setCond(3, true);
                  qs.setMemoState(2);
                  ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
                  if (world != null) {
                     world.removeAllowed(player.getObjectId());
                  }

                  player.setReflectionId(0);
                  htmltext = event;
               }
               break;
            case "exit_instance":
               if (qs.isMemoState(2)) {
                  player.teleToLocation(new Location(113793, -109342, -845, 0), 0, true);
               }
               break;
            case "32654-02.htm":
            case "32654-03.htm":
            case "32653-02.htm":
            case "32653-03.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      switch(qs.getState()) {
         case 0:
            if (npc.getId() == 32020) {
               qs = player.getQuestState("_10283_RequestOfIceMerchant");
               htmltext = player.getLevel() >= 82 && qs != null && qs.isCompleted() ? "32020-01.htm" : "32020-04.htm";
            }
            break;
         case 1:
            switch(npc.getId()) {
               case 32020:
                  switch(qs.getMemoState()) {
                     case 1:
                        return player.getLevel() >= 82 ? "32020-06.htm" : "32020-08.htm";
                     case 2:
                        return "32020-07.htm";
                     default:
                        return htmltext;
                  }
               case 32653:
                  switch(qs.getMemoState()) {
                     case 2:
                        return player.getLevel() >= 82 ? "32653-01.htm" : "32653-05.htm";
                     case 3:
                        qs.calcExpAndSp(this.getId());
                        qs.calcReward(this.getId());
                        qs.exitQuest(false, true);
                        return "32653-04.htm";
                     default:
                        return htmltext;
                  }
               case 32654:
                  switch(qs.getMemoState()) {
                     case 2:
                        return player.getLevel() >= 82 ? "32654-01.htm" : "32654-05.htm";
                     case 3:
                        qs.calcExpAndSp(this.getId());
                        qs.calcReward(this.getId());
                        qs.exitQuest(false, true);
                        return "32654-04.htm";
                     default:
                        return htmltext;
                  }
               case 32760:
                  if (qs.isMemoState(1)) {
                     if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 0) {
                        return "32760-01.htm";
                     } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 0) {
                        return "32760-01a.htm";
                     } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 0) {
                        return "32760-01b.htm";
                     } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 1) {
                        return "32760-01c.htm";
                     } else if (qs.getInt("ex1") == 0 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                        return "32760-01d.htm";
                     } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 0 && qs.getInt("ex3") == 1) {
                        return "32760-01e.htm";
                     } else {
                        if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 0) {
                           htmltext = "32760-01f.htm";
                        } else if (qs.getInt("ex1") == 1 && qs.getInt("ex2") == 1 && qs.getInt("ex3") == 1) {
                           htmltext = "32760-01g.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  }

                  return htmltext;
               default:
                  return htmltext;
            }
         case 2:
            if (npc.getId() == 32020) {
               htmltext = "32020-05.htm";
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _10284_AcquisitionOfDivineSword(10284, _10284_AcquisitionOfDivineSword.class.getSimpleName(), "");
   }
}
