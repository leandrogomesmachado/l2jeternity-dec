package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka35_45 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 5, 13002, 5, 13002, 5, 13002, 5, 13002, 5};
   private static final int[] REW2 = new int[]{12826, 1, 10842, 1, 10843, 1, 10846, 1, 12829, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka35_45(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka35_45";
      this.param.instanceId = 49;
      this.param.rewPosition = new Location(9290, -212993, -7799);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22461, 22462, 22463});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22461, new int[]{22462, 22463});
   }

   public static void main(String[] args) {
      new Kamaloka35_45(Kamaloka35_45.class.getSimpleName(), "Kamaloka35_45");
   }
}
