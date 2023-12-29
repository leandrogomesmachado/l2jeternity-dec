package l2e.scripts.ai.freya;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class FreyaThrone extends Fighter {
   private static final int ETERNAL_BLIZZARD = 6274;
   private static final int ICE_BALL = 6278;
   private static final int SUMMON_ELEMENTAL = 6277;
   private static final int SELF_NOVA = 6279;
   private static final int DEATH_SENTENCE = 6280;
   private static final int ANGER = 6285;
   private long _blizzardReuseTimer = 0L;
   private long _iceballReuseTimer = 0L;
   private long _summonReuseTimer = 0L;
   private long _selfnovaReuseTimer = 0L;
   private long _deathsentenceReuseTimer = 0L;
   private long _angerReuseTimer = 0L;
   private final int _blizzardReuseDelay = 60;
   private final int _iceballReuseDelay = 20;
   private final int _summonReuseDelay = 60;
   private final int _selfnovaReuseDelay = 70;
   private final int _deathsentenceReuseDelay = 50;
   private final int _angerReuseDelay = 50;
   private final int _summonChance = 70;
   private final int _iceballChance = 60;
   private final int _deathsentenceChance = 60;
   private final int _angerChance = 60;

   public FreyaThrone(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      Creature mostHated = actor.getMostHated();
      if (!actor.isCastingNow() && this._blizzardReuseTimer < System.currentTimeMillis()) {
         actor.doCast(SkillsParser.getInstance().getInfo(6274, 1));
         ReflectionWorld instance = ReflectionManager.getInstance().getWorld(actor.getReflectionId());
         if (instance != null && instance.getAllowed() != null) {
            GameServerPacket packet = new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, 2, 3000);

            for(int objectId : instance.getAllowed()) {
               Player activeChar = World.getInstance().getPlayer(objectId);
               if (activeChar != null) {
                  activeChar.sendPacket(packet);
               }
            }
         }

         this._blizzardReuseTimer = System.currentTimeMillis() + 60000L;
      }

      if (!actor.isCastingNow()
         && !actor.isMoving()
         && this._iceballReuseTimer < System.currentTimeMillis()
         && Rnd.chance(60)
         && mostHated != null
         && !mostHated.isDead()
         && mostHated.isInRange(actor, 1000L)) {
         actor.setTarget(mostHated);
         actor.doCast(SkillsParser.getInstance().getInfo(6278, 1));
         this._iceballReuseTimer = System.currentTimeMillis() + 20000L;
      }

      if (!actor.isCastingNow() && this._summonReuseTimer < System.currentTimeMillis() && Rnd.chance(70)) {
         actor.doCast(SkillsParser.getInstance().getInfo(6277, 1));

         for(Npc npc : World.getInstance().getAroundNpc(actor, 800, 200)) {
            if (npc != null && npc.isMonster() && !npc.isDead()) {
               npc.makeTriggerCast(SkillsParser.getInstance().getInfo(6277, 1), npc);
            }
         }

         this._summonReuseTimer = System.currentTimeMillis() + 60000L;
      }

      if (!actor.isCastingNow() && this._selfnovaReuseTimer < System.currentTimeMillis()) {
         actor.doCast(SkillsParser.getInstance().getInfo(6279, 1));
         this._selfnovaReuseTimer = System.currentTimeMillis() + 70000L;
      }

      if (!actor.isCastingNow()
         && !actor.isMoving()
         && this._deathsentenceReuseTimer < System.currentTimeMillis()
         && Rnd.chance(60)
         && mostHated != null
         && !mostHated.isDead()
         && mostHated.isInRange(actor, 1000L)) {
         actor.setTarget(mostHated);
         actor.doCast(SkillsParser.getInstance().getInfo(6280, 1));
         this._deathsentenceReuseTimer = System.currentTimeMillis() + 50000L;
      }

      if (!actor.isCastingNow() && !actor.isMoving() && this._angerReuseTimer < System.currentTimeMillis() && Rnd.chance(60)) {
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(6285, 1));
         this._angerReuseTimer = System.currentTimeMillis() + 50000L;
      }

      super.thinkAttack();
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      long generalReuse = System.currentTimeMillis() + 40000L;
      this._blizzardReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._iceballReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._summonReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._selfnovaReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._deathsentenceReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._angerReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null && !activeChar.isDead()) {
               this.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(2));
            }
         }
      }
   }

   @Override
   protected boolean thinkActive() {
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null && !activeChar.isDead()) {
               this.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(2));
            }
         }
      }

      super.thinkActive();
      return true;
   }
}
