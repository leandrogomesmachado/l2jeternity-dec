package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class VarkaSilenosSupport extends Quest {
   private static final String qn = "VarkaSilenosSupport";
   private static final int ASHAS = 31377;
   private static final int NARAN = 31378;
   private static final int UDAN = 31379;
   private static final int DIYABU = 31380;
   private static final int HAGOS = 31381;
   private static final int SHIKON = 31382;
   private static final int TERANU = 31383;
   private static final int[] NPCS = new int[]{31377, 31378, 31379, 31380, 31381, 31382, 31383};
   private static final int[] VARKA_MARKS = new int[]{7221, 7222, 7223, 7224, 7225};
   private static final int SEED = 7187;
   private static final Map<Integer, VarkaSilenosSupport.BuffsData> BUFF = new HashMap<>();

   private int getAllianceLevel(Player player) {
      for(int i = 0; i < VARKA_MARKS.length; ++i) {
         if (hasQuestItems(player, VARKA_MARKS[i])) {
            return -(i + 1);
         }
      }

      return 0;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("VarkaSilenosSupport");
      if (st == null) {
         return event;
      } else {
         int Alevel = this.getAllianceLevel(player);
         if (Util.isDigit(event) && BUFF.containsKey(Integer.parseInt(event))) {
            VarkaSilenosSupport.BuffsData buff = BUFF.get(Integer.parseInt(event));
            if (st.getQuestItemsCount(7187) >= (long)buff.getCost()) {
               st.takeItems(7187, (long)buff.getCost());
               npc.setTarget(player);
               npc.doCast(buff.getSkill());
               npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
               htmltext = "31379-4.htm";
            }
         } else if (event.equals("Withdraw")) {
            if (player.getWarehouse().getSize() == 0) {
               htmltext = "31381-0.htm";
            } else {
               player.sendActionFailed();
               player.setActiveWarehouse(player.getWarehouse());
               player.sendPacket(new WareHouseWithdrawList(player, 1));
            }
         } else if (event.equals("Teleport")) {
            if (Alevel == -4) {
               htmltext = "31383-4.htm";
            } else if (Alevel == -5) {
               htmltext = "31383-5.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("VarkaSilenosSupport");
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      int Alevel = this.getAllianceLevel(player);
      if (npcId == 31377) {
         if (Alevel < 0) {
            htmltext = "31377-friend.htm";
         } else {
            htmltext = "31377-no.htm";
         }
      } else if (npcId == 31378) {
         if (Alevel < 0) {
            htmltext = "31378-friend.htm";
         } else {
            htmltext = "31378-no.htm";
         }
      } else if (npcId == 31379) {
         st.setState((byte)1);
         if (Alevel > 0) {
            htmltext = "31379-3.htm";
         } else if (Alevel > -3) {
            htmltext = "31379-1.htm";
         } else if (Alevel < -2) {
            if (st.hasQuestItems(7187)) {
               htmltext = "31379-4.htm";
            } else {
               htmltext = "31379-2.htm";
            }
         }
      } else if (npcId == 31380) {
         if (player.getKarma() >= 1) {
            htmltext = "31380-pk.htm";
         } else if (Alevel >= 0) {
            htmltext = "31380-no.htm";
         } else if (Alevel != -1 && Alevel != -2) {
            htmltext = "31380-2.htm";
         } else {
            htmltext = "31380-1.htm";
         }
      } else if (npcId == 31381) {
         if (Alevel >= 0) {
            htmltext = "31381-no.htm";
         } else if (Alevel == -1) {
            htmltext = "31381-1.htm";
         } else if (player.getWarehouse().getSize() == 0) {
            htmltext = "31381-3.htm";
         } else if (Alevel != -2 && Alevel != -3) {
            htmltext = "31381-4.htm";
         } else {
            htmltext = "31381-2.htm";
         }
      } else if (npcId == 31382) {
         if (Alevel == -2) {
            htmltext = "31382-1.htm";
         } else if (Alevel == -3 || Alevel == -4) {
            htmltext = "31382-2.htm";
         } else if (Alevel == -5) {
            htmltext = "31382-3.htm";
         } else {
            htmltext = "31382-no.htm";
         }
      } else if (npcId == 31383) {
         if (Alevel >= 0) {
            htmltext = "31383-no.htm";
         } else if (Alevel < 0 && Alevel > -4) {
            htmltext = "31383-1.htm";
         } else if (Alevel == -4) {
            htmltext = "31383-2.htm";
         } else {
            htmltext = "31383-3.htm";
         }
      }

      return htmltext;
   }

   public VarkaSilenosSupport(int id, String name, String descr) {
      super(id, name, descr);

      for(int i : NPCS) {
         this.addFirstTalkId(i);
      }

      this.addTalkId(31379);
      this.addTalkId(31381);
      this.addTalkId(31383);
      this.addStartNpc(31381);
      this.addStartNpc(31383);
      BUFF.put(1, new VarkaSilenosSupport.BuffsData(4359, 2));
      BUFF.put(2, new VarkaSilenosSupport.BuffsData(4360, 2));
      BUFF.put(3, new VarkaSilenosSupport.BuffsData(4345, 3));
      BUFF.put(4, new VarkaSilenosSupport.BuffsData(4355, 3));
      BUFF.put(5, new VarkaSilenosSupport.BuffsData(4352, 3));
      BUFF.put(6, new VarkaSilenosSupport.BuffsData(4354, 3));
      BUFF.put(7, new VarkaSilenosSupport.BuffsData(4356, 6));
      BUFF.put(8, new VarkaSilenosSupport.BuffsData(4357, 6));
   }

   public static void main(String[] args) {
      new VarkaSilenosSupport(-1, "VarkaSilenosSupport", "custom");
   }

   private class BuffsData {
      private final int _skill;
      private final int _cost;

      public BuffsData(int skill, int cost) {
         this._skill = skill;
         this._cost = cost;
      }

      public Skill getSkill() {
         return SkillsParser.getInstance().getInfo(this._skill, 1);
      }

      public int getCost() {
         return this._cost;
      }
   }
}
