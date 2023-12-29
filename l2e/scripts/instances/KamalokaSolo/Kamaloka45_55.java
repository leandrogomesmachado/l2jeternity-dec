package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka45_55 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 5, 13002, 5, 13002, 5, 13002, 5, 13002, 5};
   private static final int[] REW2 = new int[]{12828, 1, 10847, 1, 10848, 1, 10851, 1, 12831, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka45_55(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka45_55";
      this.param.instanceId = 51;
      this.param.rewPosition = new Location(23650, -213051, -8007);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22467, 22468, 22469});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22467, new int[]{22468, 22469});
   }

   public static void main(String[] args) {
      new Kamaloka45_55(Kamaloka45_55.class.getSimpleName(), "Kamaloka45_55");
   }
}
