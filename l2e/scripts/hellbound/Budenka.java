package l2e.scripts.hellbound;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Budenka extends Quest {
   private static final int BUDENKA = 32294;
   private static final int STANDART_CERT = 9851;
   private static final int PREMIUM_CERT = 9852;

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (player.getInventory().getInventoryItemCount(9852, -1, false) > 0L) {
         return "32294-premium.htm";
      } else if (player.getInventory().getInventoryItemCount(9851, -1, false) > 0L) {
         return "32294-standart.htm";
      } else {
         npc.showChatWindow(player);
         return null;
      }
   }

   public Budenka(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32294);
   }

   public static void main(String[] args) {
      new Budenka(-1, "Budenka", "hellbound");
   }
}
