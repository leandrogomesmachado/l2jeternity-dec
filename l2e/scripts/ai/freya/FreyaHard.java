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

public class FreyaHard extends Fighter {
   private static final int ETERNAL_BLIZZARD = 6275;
   private static final int BLIZZARD_FORCE = 6697;
   private static final int ICE_BALL = 6278;
   private static final int SUMMON_ELEMENTAL = 6277;
   private static final int SELF_NOVA = 6279;
   private static final int DEATH_SENTENCE = 6280;
   private static final int REFLECT_MAGIC = 6282;
   private static final int ICE_STORM = 6283;
   private static final int ANGER = 6285;
   private long _blizzardReuseTimer = 0L;
   private long _iceballReuseTimer = 0L;
   private long _selfnovaReuseTimer = 0L;
   private long _summonReuseTimer = 0L;
   private long _deathsentenceReuseTimer = 0L;
   private long _reflectReuseTimer = 0L;
   private long _icestormReuseTimer = 0L;
   private long _angerReuseTimer = 0L;
   private final int _blizzardReuseDelay = 50;
   private final int _iceballReuseDelay = 7;
   private final int _summonReuseDelay = 40;
   private final int _selfnovaReuseDelay = 40;
   private final int _deathsentenceReuseDelay = 40;
   private final int _reflectReuseDelay = 30;
   private final int _icestormReuseDelay = 40;
   private final int _angerReuseDelay = 30;
   private int _manaBurnUse = 0;

   public FreyaHard(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      Creature mostHated = actor.getMostHated();
      if (!actor.isDead() && !actor.isCastingNow() && this._blizzardReuseTimer < System.currentTimeMillis()) {
         ReflectionWorld instance = ReflectionManager.getInstance().getWorld(actor.getReflectionId());
         if (instance != null && instance.getAllowed() != null) {
            if (this._manaBurnUse < 4 && actor.getCurrentHp() < actor.getMaxHp() * (0.8 - 0.2 * (double)this._manaBurnUse)) {
               ++this._manaBurnUse;
               actor.doCast(SkillsParser.getInstance().getInfo(6697, 1));
               GameServerPacket packet = new ExShowScreenMessage(
                  NpcStringId.MAGIC_POWER_SO_STRONG_THAT_IT_COULD_MAKE_YOU_LOSE_YOUR_MIND_CAN_BE_FELT_FROM_SOMEWHERE, 2, 3000
               );

               for(int objectId : instance.getAllowed()) {
                  Player activeChar = World.getInstance().getPlayer(objectId);
                  if (activeChar != null) {
                     activeChar.setCurrentMp(0.0);
                     activeChar.broadcastStatusUpdate();
                     activeChar.sendPacket(packet);
                  }
               }
            } else {
               actor.doCast(SkillsParser.getInstance().getInfo(6275, 1));
               GameServerPacket packet = new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, 2, 3000);

               for(int objectId : instance.getAllowed()) {
                  Player activeChar = World.getInstance().getPlayer(objectId);
                  if (activeChar != null) {
                     activeChar.sendPacket(packet);
                  }
               }
            }
         }

         this._blizzardReuseTimer = System.currentTimeMillis() + 50000L;
      }

      if (!actor.isCastingNow()
         && !actor.isMoving()
         && this._iceballReuseTimer < System.currentTimeMillis()
         && mostHated != null
         && !mostHated.isDead()
         && mostHated.isInRange(actor, 1000L)) {
         actor.setTarget(mostHated);
         actor.doCast(SkillsParser.getInstance().getInfo(6278, 1));
         this._iceballReuseTimer = System.currentTimeMillis() + 7000L;
      }

      if (!actor.isCastingNow() && this._summonReuseTimer < System.currentTimeMillis()) {
         actor.doCast(SkillsParser.getInstance().getInfo(6277, 1));

         for(Npc npc : World.getInstance().getAroundNpc(actor, 800, 200)) {
            if (npc != null && npc.isMonster() && !npc.isDead()) {
               npc.makeTriggerCast(SkillsParser.getInstance().getInfo(6277, 1), npc);
            }
         }

         this._summonReuseTimer = System.currentTimeMillis() + 40000L;
      }

      if (!actor.isCastingNow() && this._selfnovaReuseTimer < System.currentTimeMillis()) {
         actor.doCast(SkillsParser.getInstance().getInfo(6279, 1));
         this._selfnovaReuseTimer = System.currentTimeMillis() + 40000L;
      }

      if (!actor.isCastingNow() && this._reflectReuseTimer < System.currentTimeMillis()) {
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(6282, 1));
         this._reflectReuseTimer = System.currentTimeMillis() + 30000L;
      }

      if (!actor.isCastingNow() && this._icestormReuseTimer < System.currentTimeMillis()) {
         actor.setTarget(mostHated);
         actor.doCast(SkillsParser.getInstance().getInfo(6283, 1));
         this._icestormReuseTimer = System.currentTimeMillis() + 40000L;
      }

      if (!actor.isCastingNow()
         && !actor.isMoving()
         && this._deathsentenceReuseTimer < System.currentTimeMillis()
         && mostHated != null
         && !mostHated.isDead()
         && mostHated.isInRange(actor, 1000L)) {
         actor.setTarget(mostHated);
         actor.doCast(SkillsParser.getInstance().getInfo(6280, 1));
         this._deathsentenceReuseTimer = System.currentTimeMillis() + 40000L;
      }

      if (!actor.isCastingNow() && !actor.isMoving() && this._angerReuseTimer < System.currentTimeMillis()) {
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(6285, 1));
         this._angerReuseTimer = System.currentTimeMillis() + 30000L;
      }

      super.thinkAttack();
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      long generalReuse = System.currentTimeMillis() + 30000L;
      this._blizzardReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._iceballReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._summonReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._selfnovaReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._reflectReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
      this._icestormReuseTimer += generalReuse + (long)Rnd.get(1, 20) * 1000L;
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
