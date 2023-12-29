package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.LakfiManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcSay;

public class LafiLakfi extends DefaultAI {
   private static final int MAX_RADIUS = 500;
   private static final Skill s_display_bug_of_fortune1 = SkillsParser.getInstance().getInfo(6045, 1);
   private static final Skill s_display_jackpot_firework = SkillsParser.getInstance().getInfo(5778, 1);
   private long _nextEat = 0L;
   private int i_ai2;
   private int actor_lvl;
   private int prev_st;
   private boolean _firstSaid;

   public LafiLakfi(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      Attackable actor = this.getActiveChar();
      this.addTimer(7778, 1000L);
      if (this.getFirstSpawned(actor)) {
         this.i_ai2 = 0;
         this.prev_st = 0;
      } else {
         this.i_ai2 = 3;
         this.prev_st = 3;
      }

      this._firstSaid = false;
      this.actor_lvl = actor.getLevel();
   }

   @Override
   protected void onEvtArrived() {
      super.onEvtArrived();
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (this.i_ai2 > 9) {
            if (!this._firstSaid) {
               actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE), 2000);
               this._firstSaid = true;
            }
         } else {
            ItemInstance closestItem = null;
            if (this._nextEat < System.currentTimeMillis()) {
               for(GameObject object : World.getInstance().getAroundObjects(actor, 20, 200)) {
                  if (object instanceof ItemInstance && ((ItemInstance)object).getId() == 57) {
                     closestItem = (ItemInstance)object;
                  }
               }

               if (closestItem != null && closestItem.getCount() >= Config.MIN_ADENA_TO_EAT) {
                  closestItem.decayMe();
                  actor.setTarget(actor);
                  actor.doCast(s_display_bug_of_fortune1);
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.YUM_YUM_YUM_YUM), 2000);
                  this._firstSaid = false;
                  if (this.i_ai2 == 2 && this.getFirstSpawned(actor)) {
                     MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(this.getCurrActor()));
                     Location loc = actor.getLocation();
                     npc.getTemplate().setLevel((byte)actor.getLevel());
                     npc.setReflectionId(actor.getReflectionId());
                     npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
                     npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
                     actor.doDie(actor);
                     actor.deleteMe();
                  }

                  ++this.i_ai2;
                  this._nextEat = System.currentTimeMillis() + (long)(Config.INTERVAL_EATING * 1000);
               }
            }
         }
      }
   }

   private boolean getFirstSpawned(Attackable actor) {
      return actor.getId() != 2503 && actor.getId() != 2502;
   }

   private int getCurrActor() {
      return Rnd.chance(20) ? 2503 : 2502;
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         if (!actor.isMoving() && this._nextEat < System.currentTimeMillis()) {
            ItemInstance closestItem = null;

            for(GameObject object : World.getInstance().getAroundObjects(actor, 500, 200)) {
               if (object instanceof ItemInstance && ((ItemInstance)object).getId() == 57) {
                  closestItem = (ItemInstance)object;
               }
            }

            if (closestItem != null) {
               actor.setWalking();
               actor.getAI().setIntention(CtrlIntention.MOVING, closestItem.getLocation());
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int getChance(int stage) {
      switch(stage) {
         case 4:
            return 10;
         case 5:
            return 20;
         case 6:
            return 40;
         case 7:
            return 60;
         case 8:
            return 70;
         case 9:
            return 80;
         case 10:
            return 100;
         default:
            return 0;
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      super.onEvtDead(killer);
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (killer != null && killer.isPlayer()) {
            if (this.i_ai2 >= 0 && this.i_ai2 < 3) {
               actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.I_HAVENT_EATEN_ANYTHING_IM_SO_WEAK), 2000);
               return;
            }

            actor.broadcastPacket(new MagicSkillUse(actor, s_display_jackpot_firework.getId(), 1, s_display_jackpot_firework.getHitTime(), 0));
            if (Rnd.chance(this.getChance(this.i_ai2))) {
               LakfiManager.getInstance().getLakfiRewards(this.actor_lvl, actor, killer.getActingPlayer(), (double)Rnd.get(0, 100));
            }
         }
      }
   }

   @Override
   protected void onEvtTimer(int timerId, Object arg1) {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (timerId == 7778) {
            switch(this.i_ai2) {
               case 0:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IF_YOU_HAVE_ITEMS_PLEASE_GIVE_THEM_TO_ME), 2000);
                  break;
               case 1:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.MY_STOMACH_IS_EMPTY), 2000);
                  break;
               case 2:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_HUNGRY_IM_HUNGRY), 2000);
                  break;
               case 3:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.I_FEEL_A_LITTLE_WOOZY), 2000);
                  break;
               case 4:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_STILL_NOT_FULL), 2000);
                  break;
               case 5:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_STILL_HUNGRY), 2000);
                  break;
               case 6:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.NOW_ITS_TIME_TO_EAT), 2000);
                  break;
               case 7:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.GIVE_ME_SOMETHING_TO_EAT), 2000);
                  break;
               case 8:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_STILL_HUNGRY_), 2000);
                  break;
               case 9:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.I_ALSO_NEED_A_DESSERT), 2000);
                  break;
               case 10:
                  actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE), 2000);
            }

            this.addTimer(7778, (long)(10000 + Rnd.get(10) * 1000));
         }

         if (timerId == 1500) {
            if (this.prev_st == this.i_ai2 && this.prev_st != 0 && this.i_ai2 != 10) {
               actor.doDie(actor);
            } else {
               this.prev_st = this.i_ai2;
               this.addTimer(1500, (long)(Config.TIME_IF_NOT_FEED * 60000));
            }
         } else {
            super.onEvtTimer(timerId, arg1);
         }
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }
}
