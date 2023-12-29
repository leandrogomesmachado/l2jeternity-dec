package l2e.scripts.village_master;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class FirstClassTransferTalk extends Quest {
   private static final int BITZ = 30026;
   private static final int BIOTIN = 30031;
   private static final int ASTERIOS = 30154;
   private static final int THIFIELL = 30358;
   private static final int KAKAI = 30565;
   private static final int REED = 30520;
   private static final int BRONK = 30525;
   private static final int HOFFA = 32171;
   private static final int FISLER = 32158;
   private static final int MOKA = 32157;
   private static final int DEVON = 32160;
   private static final int RIVIAN = 32147;
   private static final int TOOK = 32150;
   private static final int PRANA = 32153;
   private static final int ALDENIA = 32154;

   @Override
   public String onEvent(String event, QuestState st) {
      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = npc.getId() + "_";
      switch(npc.getId()) {
         case 30026:
         case 32154:
            if (player.getRace() != Race.Human || player.getClassId().isMage()) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               htmltext = htmltext + "fighter.html";
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30031:
         case 32153:
            if (player.getRace() != Race.Human || !player.getClassId().isMage()) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               htmltext = htmltext + "mystic.html";
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30154:
         case 32147:
            if (player.getRace() != Race.Elf) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               if (player.getClassId().isMage()) {
                  htmltext = htmltext + "mystic.html";
               } else {
                  htmltext = htmltext + "fighter.html";
               }
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30358:
         case 32160:
            if (player.getRace() != Race.DarkElf) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               if (player.getClassId().isMage()) {
                  htmltext = htmltext + "mystic.html";
               } else {
                  htmltext = htmltext + "fighter.html";
               }
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30520:
         case 32158:
         case 32171:
            if (player.getRace() != Race.Dwarf) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               htmltext = htmltext + "fighter.html";
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30525:
         case 32157:
            if (player.getClassId().level() == 0) {
               htmltext = htmltext + "fighter.html";
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
            break;
         case 30565:
         case 32150:
            if (player.getRace() != Race.Orc) {
               htmltext = htmltext + "no.html";
            } else if (player.getClassId().level() == 0) {
               if (player.getClassId().isMage()) {
                  htmltext = htmltext + "mystic.html";
               } else {
                  htmltext = htmltext + "fighter.html";
               }
            } else if (player.getClassId().level() == 1) {
               htmltext = htmltext + "transfer_1.html";
            } else {
               htmltext = htmltext + "transfer_2.html";
            }
      }

      return htmltext;
   }

   public FirstClassTransferTalk(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int npc : new int[]{30026, 30031, 30154, 30358, 30565, 30520, 30525, 32171, 32158, 32157, 32160, 32147, 32150, 32153, 32154}) {
         this.addStartNpc(npc);
         this.addTalkId(npc);
      }
   }

   public static void main(String[] args) {
      new FirstClassTransferTalk(-2, "FirstClassTransferTalk", "village_master");
   }
}
