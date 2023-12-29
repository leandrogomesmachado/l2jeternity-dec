package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka70_80 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 8, 13002, 8, 13002, 8, 13002, 8, 13002, 10};
   private static final int[] REW2 = new int[]{10860, 1, 10861, 1, 10862, 1, 10863, 1, 10864, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka70_80(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka70_80";
      this.param.instanceId = 56;
      this.param.rewPosition = new Location(49014, -219737, -8759);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22482, 22483, 22484});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22482, new int[]{22483, 22484});
   }

   public static void main(String[] args) {
      new Kamaloka70_80(Kamaloka70_80.class.getSimpleName(), "Kamaloka70_80");
   }
}
