package l2e.scripts.ai.kamaloka;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class KnightMontagnar extends Fighter {
   private long _spawnTimer = 0L;
   private int _spawnCounter = 0;
   private long _orderTimer = 0L;
   private static final long _spawnInterval = 60000L;
   private static final int _spawnLimit = 6;
   private static final long _orderInterval = 24000L;

   public KnightMontagnar(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._spawnTimer == 0L) {
         this._spawnTimer = System.currentTimeMillis();
      }

      if (this._spawnCounter < 6 && this._spawnTimer + 60000L < System.currentTimeMillis()) {
         MonsterInstance follower = NpcUtils.spawnSingle(
            18569, Location.findPointToStay(actor.getLocation(), 200, actor.getGeoIndex(), true), actor.getReflectionId(), 0L
         );
         follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1000000));
         this._spawnTimer = System.currentTimeMillis();
         ++this._spawnCounter;
      }

      if (this._spawnCounter > 0 && this._orderTimer + 24000L < System.currentTimeMillis()) {
         List<Player> aggressionList = new ArrayList<>();

         for(Player p : World.getInstance().getAroundPlayers(actor, 1500, 200)) {
            if (!p.isDead()) {
               aggressionList.add(p.getActingPlayer());
            }
         }

         if (!aggressionList.isEmpty()) {
            Player aggressionTarget = aggressionList.get(Rnd.get(aggressionList.size()));
            if (aggressionTarget != null) {
               NpcSay packet = new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.YOU_S1_ATTACK_THEM);
               packet.addStringParameter(aggressionTarget.getName().toString());
               actor.broadcastPacket(packet, 2000);
               this._orderTimer = System.currentTimeMillis();

               for(Npc minion : World.getInstance().getAroundNpc(actor)) {
                  if (minion.getId() == 18569) {
                     ((Attackable)minion).clearAggroList();
                     minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, aggressionTarget, Integer.valueOf(1000000));
                  }
               }
            }
         }
      }

      super.thinkAttack();
   }
}
