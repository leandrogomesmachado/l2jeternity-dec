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

public class KetraOrcSupport extends Quest {
   private static final String qn = "KetraOrcSupport";
   private static final int KADUN = 31370;
   private static final int WAHKAN = 31371;
   private static final int ASEFA = 31372;
   private static final int ATAN = 31373;
   private static final int JAFF = 31374;
   private static final int JUMARA = 31375;
   private static final int KURFA = 31376;
   private static final int[] NPCS = new int[]{31370, 31371, 31372, 31373, 31374, 31375, 31376};
   private static final int[] KETRA_MARKS = new int[]{7211, 7212, 7213, 7214, 7215};
   private static final int HORN = 7186;
   private static final Map<Integer, KetraOrcSupport.BuffsData> BUFF = new HashMap<>();

   private int getAllianceLevel(Player player) {
      for(int i = 0; i < KETRA_MARKS.length; ++i) {
         if (hasQuestItems(player, KETRA_MARKS[i])) {
            return i + 1;
         }
      }

      return 0;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("KetraOrcSupport");
      if (st == null) {
         return event;
      } else {
         int Alevel = this.getAllianceLevel(player);
         if (Util.isDigit(event) && BUFF.containsKey(Integer.parseInt(event))) {
            KetraOrcSupport.BuffsData buff = BUFF.get(Integer.parseInt(event));
            if (st.getQuestItemsCount(7186) >= (long)buff.getCost()) {
               st.takeItems(7186, (long)buff.getCost());
               npc.setTarget(player);
               npc.doCast(buff.getSkill());
               npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
               htmltext = "31372-4.htm";
            }
         } else if (event.equals("Withdraw")) {
            if (player.getWarehouse().getSize() == 0) {
               htmltext = "31374-0.htm";
            } else {
               player.sendActionFailed();
               player.setActiveWarehouse(player.getWarehouse());
               player.sendPacket(new WareHouseWithdrawList(player, 1));
            }
         } else if (event.equals("Teleport")) {
            if (Alevel == 4) {
               htmltext = "31376-4.htm";
            } else if (Alevel == 5) {
               htmltext = "31376-5.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("KetraOrcSupport");
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      int Alevel = this.getAllianceLevel(player);
      if (npcId == 31370) {
         if (Alevel > 0) {
            htmltext = "31370-friend.htm";
         } else {
            htmltext = "31370-no.htm";
         }
      } else if (npcId == 31371) {
         if (Alevel > 0) {
            htmltext = "31371-friend.htm";
         } else {
            htmltext = "31371-no.htm";
         }
      } else if (npcId == 31372) {
         st.setState((byte)1);
         if (Alevel < 1) {
            htmltext = "31372-3.htm";
         } else if (Alevel < 3 && Alevel > 0) {
            htmltext = "31372-1.htm";
         } else if (Alevel > 2) {
            if (st.hasQuestItems(7186)) {
               htmltext = "31372-4.htm";
            } else {
               htmltext = "31372-2.htm";
            }
         }
      } else if (npcId == 31373) {
         if (player.getKarma() >= 1) {
            htmltext = "31373-pk.htm";
         } else if (Alevel <= 0) {
            htmltext = "31373-no.htm";
         } else if (Alevel != 1 && Alevel != 2) {
            htmltext = "31373-2.htm";
         } else {
            htmltext = "31373-1.htm";
         }
      } else if (npcId == 31374) {
         if (Alevel <= 0) {
            htmltext = "31374-no.htm";
         } else if (Alevel == 1) {
            htmltext = "31374-1.htm";
         } else if (player.getWarehouse().getSize() == 0) {
            htmltext = "31374-3.htm";
         } else if (Alevel != 2 && Alevel != 3) {
            htmltext = "31374-4.htm";
         } else {
            htmltext = "31374-2.htm";
         }
      } else if (npcId == 31375) {
         if (Alevel == 2) {
            htmltext = "31375-1.htm";
         } else if (Alevel == 3 || Alevel == 4) {
            htmltext = "31375-2.htm";
         } else if (Alevel == 5) {
            htmltext = "31375-3.htm";
         } else {
            htmltext = "31375-no.htm";
         }
      } else if (npcId == 31376) {
         if (Alevel <= 0) {
            htmltext = "31376-no.htm";
         } else if (Alevel > 0 && Alevel < 4) {
            htmltext = "31376-1.htm";
         } else if (Alevel == 4) {
            htmltext = "31376-2.htm";
         } else {
            htmltext = "31376-3.htm";
         }
      }

      return htmltext;
   }

   public KetraOrcSupport(int id, String name, String descr) {
      super(id, name, descr);

      for(int i : NPCS) {
         this.addFirstTalkId(i);
      }

      this.addTalkId(31372);
      this.addTalkId(31376);
      this.addTalkId(31374);
      this.addStartNpc(31376);
      this.addStartNpc(31374);
      BUFF.put(1, new KetraOrcSupport.BuffsData(4359, 2));
      BUFF.put(2, new KetraOrcSupport.BuffsData(4360, 2));
      BUFF.put(3, new KetraOrcSupport.BuffsData(4345, 3));
      BUFF.put(4, new KetraOrcSupport.BuffsData(4355, 3));
      BUFF.put(5, new KetraOrcSupport.BuffsData(4352, 3));
      BUFF.put(6, new KetraOrcSupport.BuffsData(4354, 3));
      BUFF.put(7, new KetraOrcSupport.BuffsData(4356, 6));
      BUFF.put(8, new KetraOrcSupport.BuffsData(4357, 6));
   }

   public static void main(String[] args) {
      new KetraOrcSupport(-1, "KetraOrcSupport", "custom");
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
