package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka55_65 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 6, 13002, 6, 13002, 6, 13002, 6, 13002, 6};
   private static final int[] REW2 = new int[]{12830, 1, 10852, 1, 10853, 1, 10856, 1, 12833, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka55_65(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka55_65";
      this.param.instanceId = 53;
      this.param.rewPosition = new Location(16508, -205737, -8008);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22473, 22474, 22475});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22473, new int[]{22474, 22475});
   }

   public static void main(String[] args) {
      new Kamaloka55_65(Kamaloka55_65.class.getSimpleName(), "Kamaloka55_65");
   }
}
