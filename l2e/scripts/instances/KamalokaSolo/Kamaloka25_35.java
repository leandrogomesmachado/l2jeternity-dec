package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka25_35 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 4, 13002, 4, 13002, 4, 13002, 4, 13002, 4};
   private static final int[] REW2 = new int[]{12825, 1, 10837, 1, 10838, 1, 10841, 1, 12827, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka25_35(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka25_35";
      this.param.instanceId = 47;
      this.param.rewPosition = new Location(16301, -219806, -8021);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22455, 22456, 22457});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      return this.onAdvEventTo(event, npc, player, this.param.qn, REW1, REW2);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      return npc.getId() == 32484 ? this.onEnterTo(npc, player, this.param) : this.onTalkTo(npc, player, this.param.qn);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      return this.onKillTo(npc, player, isPet, this.param.qn, 22455, new int[]{22456, 22457});
   }

   public static void main(String[] args) {
      new Kamaloka25_35(Kamaloka25_35.class.getSimpleName(), "Kamaloka25_35");
   }
}
