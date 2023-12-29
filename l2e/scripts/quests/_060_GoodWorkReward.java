package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _060_GoodWorkReward extends Quest {
   private static final Map<Integer, String> _profs = new HashMap<>();
   private static final Map<String, int[]> _classes = new HashMap<>();
   public Npc _pursuer;

   public _060_GoodWorkReward(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31435);
      this.addTalkId(new int[]{31435, 30081, 31092, 32487});
      this.addKillId(27340);
      this.questItemIds = new int[]{10867, 10868};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("31435-03.htm")) {
            if (st.getState() == 0) {
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("32487-02.htm")) {
            if (cond == 1) {
               this.despawnPursuer(st);
               this.spawnPursuer(st);
               this._pursuer.setRunning();
               ((Attackable)this._pursuer).addDamageHate(player, 0, 999);
               this._pursuer.getAI().setIntention(CtrlIntention.ATTACK, player);
            }
         } else if (event.equalsIgnoreCase("31435-05.htm")) {
            if (cond == 3) {
               st.setCond(4, true);
            }
         } else if (event.equalsIgnoreCase("30081-03.htm")) {
            if (cond == 4) {
               if (st.getQuestItemsCount(10867) < 1L) {
                  return "30081-03a.htm";
               }

               st.takeItems(10867, -1L);
               st.setCond(5, true);
            }
         } else if (event.equalsIgnoreCase("30081-05.htm")) {
            if (cond == 5) {
               st.setCond(6, true);
            }
         } else if (event.equalsIgnoreCase("30081-08.htm")) {
            if (cond == 5 || cond == 6) {
               if (st.getQuestItemsCount(57) < 3000000L) {
                  st.setCond(6, true);
                  return "30081-07.htm";
               }

               st.takeItems(57, 3000000L);
               st.giveItems(10868, 1L);
               st.setCond(7, true);
            }
         } else if (event.equalsIgnoreCase("32487-06.htm")) {
            if (cond == 7) {
               if (st.getQuestItemsCount(10868) < 1L) {
                  return "32487-06a.htm";
               }

               st.takeItems(10868, -1L);
               st.setCond(8, true);
            }
         } else if (event.equalsIgnoreCase("31435-08.htm")) {
            if (cond == 8) {
               st.setCond(9, true);
            }
         } else if (event.equalsIgnoreCase("31092-05.htm")) {
            if (cond == 10 && _profs.containsKey(player.getClassId().getId())) {
               return "" + (String)_profs.get(player.getClassId().getId()) + ".htm";
            }
         } else if (event.startsWith("classes-") && cond == 10) {
            String occupation = event.replaceAll("classes-", "");
            int[] classes = (int[])_classes.get(occupation);
            if (classes == null) {
               return "Error id: " + occupation;
            }

            int adena = 0;

            for(int mark : classes) {
               if (st.getQuestItemsCount(mark) > 0L) {
                  adena = 1;
               } else {
                  st.giveItems(mark, 1L);
               }
            }

            if (adena > 0) {
               st.calcReward(this.getId());
            }

            st.exitQuest(false, true);
            return "31092-06.htm";
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
      } else {
         int cond = st.getCond();
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 31435) {
                  if (player.getLevel() >= 39 && player.getRace() != Race.Kamael && player.getClassId().level() == 1) {
                     htmltext = "31435-01.htm";
                  } else {
                     st.exitQuest(true);
                     htmltext = "31435-00.htm";
                  }
               }
               break;
            case 1:
               switch(npcId) {
                  case 30081:
                     switch(cond) {
                        case 4:
                           return "30081-01.htm";
                        case 5:
                           return "30081-04.htm";
                        case 6:
                           return "30081-06.htm";
                        default:
                           if (cond > 6) {
                              htmltext = "30081-09.htm";
                           }

                           return htmltext;
                     }
                  case 31092:
                     switch(cond) {
                        case 10:
                           htmltext = "31092-01.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31435:
                     switch(cond) {
                        case 1:
                        case 2:
                           return "31435-03.htm";
                        case 3:
                           return "31435-04.htm";
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                           return "31435-06.htm";
                        case 8:
                           return "31435-07.htm";
                        case 9:
                           st.setCond(10, true);
                           return "31435-09.htm";
                        default:
                           if (cond > 9) {
                              htmltext = "31435-10.htm";
                           }

                           return htmltext;
                     }
                  case 32487:
                     switch(cond) {
                        case 1:
                           return "32487-01.htm";
                        case 2:
                           st.setCond(3, true);
                           return "32487-03.htm";
                        case 3:
                           return "32487-04.htm";
                        case 4:
                        case 5:
                        case 6:
                        default:
                           if (cond > 7) {
                              htmltext = "32487-06.htm";
                           }

                           return htmltext;
                        case 7:
                           htmltext = "32487-05.htm";
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
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1)) {
         if (st.calcDropItems(this.getId(), 10867, npc.getId(), 1)) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOU_HAVE_GOOD_LUCK_I_SHALL_RETURN), 2000);
            st.setCond(2);
         } else {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOU_ARE_STRONG_THIS_WAS_A_MISTAKE), 2000);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   private void spawnPursuer(QuestState st) {
      this._pursuer = st.addSpawn(27340, 72590, 148100, -3312, getRandom(0, 20), true, 1800000);
      this._pursuer
         .broadcastPacket(new NpcSay(this._pursuer.getObjectId(), 0, this._pursuer.getId(), NpcStringId.S1_I_MUST_KILL_YOU_BLAME_YOUR_OWN_CURIOSITY), 2000);
      this._pursuer.getAI().setIntention(CtrlIntention.ATTACK, st.getPlayer());
   }

   private void despawnPursuer(QuestState st) {
      if (this._pursuer != null) {
         this._pursuer.deleteMe();
      }

      this._pursuer = null;
   }

   public static void main(String[] args) {
      new _060_GoodWorkReward(60, _060_GoodWorkReward.class.getSimpleName(), "");
   }

   static {
      _profs.put(1, "classId-1");
      _profs.put(4, "classId-4");
      _profs.put(7, "classId-7");
      _profs.put(11, "classId-11");
      _profs.put(15, "classId-15");
      _profs.put(19, "classId-19");
      _profs.put(22, "classId-22");
      _profs.put(26, "classId-26");
      _profs.put(29, "classId-29");
      _profs.put(32, "classId-32");
      _profs.put(35, "classId-35");
      _profs.put(39, "classId-39");
      _profs.put(42, "classId-42");
      _profs.put(45, "classId-45");
      _profs.put(47, "classId-47");
      _profs.put(50, "classId-50");
      _profs.put(54, "classId-54");
      _profs.put(56, "classId-56");
      _classes.put("AW", new int[]{2673, 3172, 2809});
      _classes.put("BD", new int[]{2627, 3172, 2762});
      _classes.put("BH", new int[]{2809, 3119, 3238});
      _classes.put("BS", new int[]{2721, 2734, 2820});
      _classes.put("DA", new int[]{2633, 2734, 3307});
      _classes.put("DT", new int[]{2627, 3203, 3276});
      _classes.put("EE", new int[]{2721, 3140, 2820});
      _classes.put("ES", new int[]{2674, 3140, 3336});
      _classes.put("GL", new int[]{2627, 2734, 2762});
      _classes.put("HK", new int[]{2673, 2734, 3293});
      _classes.put("NM", new int[]{2674, 2734, 3307});
      _classes.put("OL", new int[]{2721, 3203, 3390});
      _classes.put("PA", new int[]{2633, 2734, 2820});
      _classes.put("PP", new int[]{2721, 2734, 2821});
      _classes.put("PR", new int[]{2673, 3172, 3293});
      _classes.put("PS", new int[]{2674, 3172, 3336});
      _classes.put("PW", new int[]{2673, 3140, 2809});
      _classes.put("SC", new int[]{2674, 2734, 2840});
      _classes.put("SE", new int[]{2721, 3172, 2821});
      _classes.put("SH", new int[]{2674, 3172, 2840});
      _classes.put("SK", new int[]{2633, 3172, 3307});
      _classes.put("SP", new int[]{2674, 3140, 2840});
      _classes.put("SR", new int[]{2673, 3140, 3293});
      _classes.put("SS", new int[]{2627, 3140, 2762});
      _classes.put("TH", new int[]{2673, 2734, 2809});
      _classes.put("TK", new int[]{2633, 3140, 2820});
      _classes.put("TR", new int[]{2627, 3203, 2762});
      _classes.put("WA", new int[]{2674, 2734, 3336});
      _classes.put("WC", new int[]{2721, 3203, 2879});
      _classes.put("WL", new int[]{2627, 2734, 3276});
      _classes.put("WS", new int[]{2867, 3119, 3238});
   }
}
