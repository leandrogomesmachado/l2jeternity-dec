package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExNpcQuestHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _463_IMustBeaGenius extends Quest {
   private static final String qn = "_463_IMustBeaGenius";
   private static final int _gutenhagen = 32069;
   private static final int _corpse_log = 15510;
   private static final int _collection = 15511;
   private static final int[] _mobs = new int[]{22801, 22802, 22804, 22805, 22807, 22808, 22809, 22810, 22811, 22812};

   public _463_IMustBeaGenius(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32069);
      this.addTalkId(32069);

      for(int _mob : _mobs) {
         this.addKillId(_mob);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_463_IMustBeaGenius");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32069) {
            if (event.equalsIgnoreCase("32069-03")) {
               st.playSound("ItemSound.quest_accept");
               st.setState((byte)1);
               st.set("cond", "1");
               int _number = getRandom(500, 600);
               st.set("number", String.valueOf(_number));

               for(int _mob : _mobs) {
                  int _rand = getRandom(-2, 4);
                  if (_rand == 0) {
                     _rand = 5;
                  }

                  st.set(String.valueOf(_mob), String.valueOf(_rand));
               }

               st.set(String.valueOf(_mobs[getRandom(0, _mobs.length - 1)]), String.valueOf(getRandom(1, 100)));
               ExNpcQuestHtmlMessage html = new ExNpcQuestHtmlMessage(npc.getObjectId(), this.getId());
               html.setFile(player, player.getLang(), "data/scripts/quests/_463_IMustBeaGenius/" + player.getLang() + "/32069-03.htm");
               event.replace("%num%", String.valueOf(_number));
               player.sendPacket(html);
            } else if (event.equalsIgnoreCase("32069-05")) {
               ExNpcQuestHtmlMessage html = new ExNpcQuestHtmlMessage(npc.getObjectId(), this.getId());
               html.setFile(player, player.getLang(), "data/scripts/quests/_463_IMustBeaGenius/" + player.getLang() + "/32069-05.htm");
               html.replace("%num%", String.valueOf(st.get("number")));
               player.sendPacket(html);
            } else if (event.equalsIgnoreCase("32069-07.htm")) {
               st.addExpAndSp(317961, 25427);
               st.unset("cond");
               st.unset("number");

               for(int _mob : _mobs) {
                  st.unset(String.valueOf(_mob));
               }

               st.takeItems(15511, -1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(QuestState.QuestType.DAILY);
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_463_IMustBeaGenius");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32069) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 70) {
                     htmltext = "32069-01.htm";
                  } else {
                     htmltext = "32069-00.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32069-04.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "32069-06.htm";
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 70) {
                        htmltext = "32069-01.htm";
                     } else {
                        htmltext = "32069-00.htm";
                     }
                  } else {
                     htmltext = "32069-08.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_463_IMustBeaGenius");
      if (st == null) {
         return null;
      } else {
         if (st.getState() == 1 && st.getInt("cond") == 1 && Util.contains(_mobs, npc.getId())) {
            int _day_number = st.getInt("number");
            int _number = st.getInt(String.valueOf(npc.getId()));
            if (_number > 0) {
               st.giveItems(15510, (long)_number);
               st.playSound("ItemSound.quest_itemget");
               NpcSay ns = new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.ATT_ATTACK_S1_RO_ROGUE_S2);
               ns.addStringParameter(player.getName());
               ns.addStringParameter(String.valueOf(_number));
               npc.broadcastPacket(ns, 2000);
            } else if (_number < 0 && st.getQuestItemsCount(15510) + (long)_number > 0L) {
               st.takeItems(15510, (long)Math.abs(_number));
               st.playSound("ItemSound.quest_itemget");
               NpcSay ns = new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.ATT_ATTACK_S1_RO_ROGUE_S2);
               ns.addStringParameter(player.getName());
               ns.addStringParameter(String.valueOf(_number));
               npc.broadcastPacket(ns, 2000);
            }

            if (st.getQuestItemsCount(15510) == (long)_day_number) {
               st.takeItems(15510, -1L);
               st.giveItems(15511, 1L);
               st.set("cond", "2");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _463_IMustBeaGenius(463, "_463_IMustBeaGenius", "");
   }
}
