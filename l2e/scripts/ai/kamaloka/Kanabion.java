package l2e.scripts.ai.kamaloka;

import java.util.Iterator;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.Quest;

public class Kanabion extends Fighter {
   public Kanabion(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      boolean isOverhit = false;
      if (actor instanceof MonsterInstance) {
         isOverhit = ((MonsterInstance)actor).getOverhitDamage() > 0.0;
      }

      int npcId = actor.getId();
      int nextId = 0;
      if (npcId != this.getNextDoppler(npcId) && npcId != this.getNextVoid(npcId)) {
         if (isOverhit) {
            if (Rnd.chance(70)) {
               nextId = this.getNextDoppler(npcId);
            } else if (Rnd.chance(80)) {
               nextId = this.getNextVoid(npcId);
            }
         } else if (Rnd.chance(65)) {
            nextId = this.getNextDoppler(npcId);
         }
      } else if (npcId == this.getNextDoppler(npcId)) {
         if (isOverhit) {
            if (Rnd.chance(60)) {
               nextId = this.getNextDoppler(npcId);
            } else if (Rnd.chance(90)) {
               nextId = this.getNextVoid(npcId);
            }
         } else if (Rnd.chance(40)) {
            nextId = this.getNextDoppler(npcId);
         } else if (Rnd.chance(50)) {
            nextId = this.getNextVoid(npcId);
         }
      } else if (npcId == this.getNextVoid(npcId)) {
         if (isOverhit) {
            if (Rnd.chance(80)) {
               nextId = this.getNextVoid(npcId);
            }
         } else if (Rnd.chance(50)) {
            nextId = this.getNextVoid(npcId);
         }
      }

      if (nextId > 0) {
         Creature player = null;
         if (!killer.isPlayer()) {
            ReflectionWorld instance = ReflectionManager.getInstance().getWorld(actor.getReflectionId());
            if (instance != null && instance.getAllowed() != null) {
               Iterator var8 = instance.getAllowed().iterator();
               if (var8.hasNext()) {
                  int objectId = var8.next();
                  Player activeChar = World.getInstance().getPlayer(objectId);
                  if (activeChar != null) {
                     player = activeChar;
                  }
               }
            }
         }

         if (player == null) {
            player = killer;
         }

         ThreadPoolManager.getInstance().schedule(new Kanabion.SpawnNext(actor, player, nextId), 5000L);
      }

      super.onEvtDead(killer);
   }

   private int getNextDoppler(int npcId) {
      switch(npcId) {
         case 22452:
         case 22453:
         case 22454:
            return 22453;
         case 22455:
         case 22456:
         case 22457:
            return 22456;
         case 22458:
         case 22459:
         case 22460:
            return 22459;
         case 22461:
         case 22462:
         case 22463:
            return 22462;
         case 22464:
         case 22465:
         case 22466:
            return 22465;
         case 22467:
         case 22468:
         case 22469:
            return 22468;
         case 22470:
         case 22471:
         case 22472:
            return 22471;
         case 22473:
         case 22474:
         case 22475:
            return 22474;
         case 22476:
         case 22477:
         case 22478:
            return 22477;
         case 22479:
         case 22480:
         case 22481:
            return 22480;
         case 22482:
         case 22483:
         case 22484:
            return 22483;
         default:
            return 0;
      }
   }

   private int getNextVoid(int npcId) {
      switch(npcId) {
         case 22452:
         case 22453:
         case 22454:
            return 22454;
         case 22455:
         case 22456:
         case 22457:
            return 22457;
         case 22458:
         case 22459:
         case 22460:
            return 22460;
         case 22461:
         case 22462:
         case 22463:
            return 22463;
         case 22464:
         case 22465:
         case 22466:
            return 22466;
         case 22467:
         case 22468:
         case 22469:
            return 22469;
         case 22470:
         case 22471:
         case 22472:
            return 22472;
         case 22473:
         case 22474:
         case 22475:
            return 22475;
         case 22476:
         case 22477:
         case 22478:
            return 22478;
         case 22479:
         case 22480:
         case 22481:
            return 22481;
         case 22482:
         case 22483:
         case 22484:
            return 22484;
         default:
            return 0;
      }
   }

   public static class SpawnNext extends RunnableImpl {
      private final Attackable _actor;
      private final Creature _player;
      private final int _nextId;

      public SpawnNext(Attackable actor, Creature player, int nextId) {
         this._actor = actor;
         this._player = player;
         this._nextId = nextId;
      }

      @Override
      public void runImpl() throws Exception {
         Attackable npc = (Attackable)Quest.addSpawn(this._nextId, this._actor);
         npc.setReflectionId(this._actor.getReflectionId());
         npc.setHeading(PositionUtils.calculateHeadingFrom(npc, this._player));
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this._player, Integer.valueOf(1000));
      }
   }
}
