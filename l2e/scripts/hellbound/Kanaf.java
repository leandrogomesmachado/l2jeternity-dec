package l2e.scripts.hellbound;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Kanaf extends Quest {
   private static final int KANAF = 32346;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      return event.equalsIgnoreCase("info") ? "32346-0" + getRandom(1, 3) + ".htm" : null;
   }

   public Kanaf(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32346);
      this.addTalkId(32346);
   }

   public static void main(String[] args) {
      new Kanaf(-1, "Kanaf", "hellbound");
   }
}
