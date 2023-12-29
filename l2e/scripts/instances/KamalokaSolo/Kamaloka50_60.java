package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka50_60 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 6, 13002, 6, 13002, 6, 13002, 6, 13002, 6};
   private static final int[] REW2 = new int[]{10850, 1, 10851, 1, 10852, 1, 10853, 1, 10854, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka50_60(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka50_60";
      this.param.instanceId = 52;
      this.param.rewPosition = new Location(9136, -205733, -8007);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22470, 22471, 22472});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22470, new int[]{22471, 22472});
   }

   public static void main(String[] args) {
      new Kamaloka50_60(Kamaloka50_60.class.getSimpleName(), "Kamaloka50_60");
   }
}
