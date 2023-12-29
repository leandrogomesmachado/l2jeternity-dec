package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _198_SevenSignEmbryo extends Quest {
   private static final String qn = "_198_SevenSignEmbryo";
   private static final int WOOD = 32593;
   private static final int FRANZ = 32597;
   private static final int SHILENSEVIL1 = 27346;
   private static final int SHILENSEVIL2 = 27399;
   private static final int SHILENSEVIL3 = 27402;
   private static final int SCULPTURE = 14360;
   private static final int BRACELET = 15312;
   private static final int AA = 5575;
   private static final int AARATE = 1;
   private boolean ShilensevilOnSpawn = false;
   private final List<Npc> _minions = new ArrayList<>();

   public _198_SevenSignEmbryo(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32593);
      this.addTalkId(32593);
      this.addTalkId(32597);
      this.addKillId(27346);
      this.addKillId(27399);
      this.addKillId(27402);
      this.questItemIds = new int[]{14360};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_198_SevenSignEmbryo");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32593) {
            if (event.equalsIgnoreCase("32593-02.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (npc.getId() == 32597) {
            if (event.equalsIgnoreCase("32597-05.htm")) {
               if (!this.ShilensevilOnSpawn) {
                  NpcSay ns = new NpcSay(32597, 0, 32597, NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP);
                  ns.addStringParameter(player.getAppearance().getVisibleName());
                  player.sendPacket(ns);
                  MonsterInstance monster = (MonsterInstance)addSpawn(27346, -23656, -9236, -5392, 0, false, 600000L, true, npc.getReflectionId());
                  monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM), 2000);
                  monster.setRunning();
                  monster.addDamageHate(player, 0, 999);
                  monster.getAI().setIntention(CtrlIntention.ATTACK, player);
                  MonsterInstance monster1 = (MonsterInstance)addSpawn(27399, -23656, -9236, -5392, 0, false, 600000L, true, npc.getReflectionId());
                  this._minions.add(monster1);
                  monster1.setRunning();
                  monster1.addDamageHate(player, 0, 999);
                  monster1.getAI().setIntention(CtrlIntention.ATTACK, player);
                  MonsterInstance monster2 = (MonsterInstance)addSpawn(27402, -23656, -9236, -5392, 0, false, 600000L, true, npc.getReflectionId());
                  this._minions.add(monster2);
                  monster2.setRunning();
                  monster2.addDamageHate(player, 0, 999);
                  monster2.getAI().setIntention(CtrlIntention.ATTACK, player);
                  this.ShilensevilOnSpawn = true;
                  this.startQuestTimer("aiplayer", 30000L, npc, player);
               }
            } else {
               if (event.equalsIgnoreCase("aiplayer")) {
                  if (this.ShilensevilOnSpawn) {
                     npc.setTarget(player);
                     npc.doCast(SkillsParser.getInstance().getInfo(1011, 18));
                     this.startQuestTimer("aiplayer", 30000L, npc, player);
                  } else {
                     this.cancelQuestTimer("aiplayer", npc, player);
                  }

                  return "";
               }

               if (event.equalsIgnoreCase("32597-10.htm")) {
                  st.set("cond", "3");
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WE_WILL_BE_WITH_YOU_ALWAYS), 2000);
                  st.takeItems(14360, -1L);
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_198_SevenSignEmbryo");
      if (st == null) {
         return htmltext;
      } else {
         QuestState fifth = player.getQuestState("_197_SevenSignTheSacredBookOfSeal");
         if (npc.getId() == 32593) {
            switch(st.getState()) {
               case 0:
                  if (fifth != null && fifth.getState() == 2 && player.getLevel() >= 79) {
                     htmltext = "32593-01.htm";
                  } else {
                     htmltext = "32593-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1 || st.getInt("cond") == 2) {
                     htmltext = "32593-02.htm";
                  } else if (st.getInt("cond") == 3) {
                     htmltext = "32593-04.htm";
                     st.giveItems(15312, 1L);
                     st.giveItems(5575, 1500000L);
                     st.addExpAndSp(150000000, 15000000);
                     st.unset("cond");
                     st.setState((byte)2);
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
            }
         } else if (npc.getId() == 32597 && st.getState() == 1) {
            if (st.getInt("cond") == 1) {
               htmltext = "32597-01.htm";
            } else if (st.getInt("cond") == 2) {
               htmltext = "32597-06.htm";
            } else if (st.getInt("cond") == 3) {
               htmltext = "32597-11.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_198_SevenSignEmbryo");
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         if (npc.getId() == 27346 && st.getInt("cond") == 1) {
            npc.deleteMe();

            for(Npc minion : this._minions) {
               if (minion != null) {
                  minion.deleteMe();
               }
            }

            this._minions.clear();
            NpcSay ns = new NpcSay(27346, 0, 27346, NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU);
            ns.addStringParameter(player.getAppearance().getVisibleName());
            player.sendPacket(ns);
            NpcSay nss = new NpcSay(32597, 0, 32597, NpcStringId.WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED);
            nss.addStringParameter(player.getAppearance().getVisibleName());
            player.sendPacket(nss);
            st.giveItems(14360, 1L);
            st.set("cond", "2");
            player.showQuestMovie(14);
            this.ShilensevilOnSpawn = false;
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _198_SevenSignEmbryo(198, "_198_SevenSignEmbryo", "");
   }
}
