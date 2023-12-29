package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Pathfinder extends Quest {
   private static final String qn = "Pathfinder";
   private static final int Pathfinder = 32484;
   private static final int Rewarder = 32485;

   public Pathfinder(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32484);
      this.addTalkId(32484);
      this.addFirstTalkId(32485);
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      String htmltext = "";
      if (npcId == 32484) {
         if (npc.isInsideRadius(-13948, 123819, -3112, 500, true, false)) {
            htmltext = "gludio-list.htm";
         } else if (npc.isInsideRadius(18228, 146030, -3088, 500, true, false)) {
            htmltext = "dion-list.htm";
         } else if (npc.isInsideRadius(108384, 221614, -3592, 500, true, false)) {
            htmltext = "heine-list.htm";
         } else if (npc.isInsideRadius(80960, 56455, -1552, 500, true, false)) {
            htmltext = "oren-list.htm";
         } else if (npc.isInsideRadius(85894, -142108, -1336, 500, true, false)) {
            htmltext = "schuttgart-list.htm";
         } else {
            if (!npc.isInsideRadius(42674, -47909, -797, 500, true, false)) {
               return null;
            }

            htmltext = "rune-list.htm";
         }
      }

      return htmltext;
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      String htmltext = "";
      if (npcId == 32485) {
         if (npc.isInsideRadius(9261, -219862, -8021, 1000, true, false)) {
            htmltext = "20-30.htm";
         } else if (npc.isInsideRadius(16301, -219806, -8021, 1000, true, false)) {
            htmltext = "25-35.htm";
         } else if (npc.isInsideRadius(23478, -220079, -7799, 1000, true, false)) {
            htmltext = "30-40.htm";
         } else if (npc.isInsideRadius(9290, -212993, -7799, 1000, true, false)) {
            htmltext = "35-45.htm";
         } else if (npc.isInsideRadius(16598, -212997, -7802, 1000, true, false)) {
            htmltext = "40-50.htm";
         } else if (npc.isInsideRadius(23650, -213051, -8007, 1000, true, false)) {
            htmltext = "45-55.htm";
         } else if (npc.isInsideRadius(9136, -205733, -8007, 1000, true, false)) {
            htmltext = "50-60.htm";
         } else if (npc.isInsideRadius(16508, -205737, -8007, 1000, true, false)) {
            htmltext = "55-65.htm";
         } else if (npc.isInsideRadius(23229, -206316, -7991, 1000, true, false)) {
            htmltext = "60-70.htm";
         } else if (npc.isInsideRadius(42638, -219781, -8759, 1000, true, false)) {
            htmltext = "65-75.htm";
         } else {
            if (!npc.isInsideRadius(49014, -219737, -8759, 1000, true, false)) {
               return null;
            }

            htmltext = "70-80.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new Pathfinder(-1, "Pathfinder", "custom");
   }
}
