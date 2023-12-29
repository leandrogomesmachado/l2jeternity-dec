package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.network.ServerPacketOpcodes;

public abstract class NpcInfo extends GameServerPacket {
   protected int _x;
   protected int _y;
   protected int _z;
   protected int _heading;
   protected int _idTemplate;
   protected boolean _isAttackable;
   protected boolean _isSummoned;
   protected double _mAtkSpd;
   protected double _pAtkSpd;
   protected int _runSpd;
   protected int _walkSpd;
   protected final int _swimRunSpd;
   protected final int _swimWalkSpd;
   protected final int _flyRunSpd;
   protected final int _flyWalkSpd;
   protected double _moveMultiplier;
   protected int _rhand;
   protected int _lhand;
   protected int _chest;
   protected int _enchantEffect;
   protected double _collisionHeight;
   protected double _collisionRadius;
   protected String _name = "";
   protected String _title = "";

   @Override
   protected ServerPacketOpcodes getOpcodes() {
      return ServerPacketOpcodes.NpcInfo;
   }

   public NpcInfo(Creature cha) {
      this._isSummoned = cha.isShowSummonAnimation();
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ() + Config.CLIENT_SHIFTZ;
      this._heading = cha.getHeading();
      this._mAtkSpd = cha.getMAtkSpd();
      this._pAtkSpd = cha.getPAtkSpd();
      this._moveMultiplier = cha.getMovementSpeedMultiplier();
      this._runSpd = (int)Math.round(cha.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)Math.round(cha.getWalkSpeed() / this._moveMultiplier);
      this._swimRunSpd = (int)Math.round(cha.getSwimRunSpeed() / this._moveMultiplier);
      this._swimWalkSpd = (int)Math.round(cha.getSwimWalkSpeed() / this._moveMultiplier);
      this._flyRunSpd = cha.isFlying() ? this._runSpd : 0;
      this._flyWalkSpd = cha.isFlying() ? this._walkSpd : 0;
   }

   public static class Info extends NpcInfo {
      private final Npc _npc;
      private int _clanCrest = 0;
      private int _allyCrest = 0;
      private int _allyId = 0;
      private int _clanId = 0;
      private int _displayEffect = 0;

      public Info(Npc cha, Creature attacker) {
         super(cha);
         this._npc = cha;
         this._idTemplate = cha.getTemplate().getIdTemplate();
         this._rhand = cha.getRightHandItem();
         this._lhand = cha.getLeftHandItem();
         this._enchantEffect = cha.getChampionTemplate() != null ? cha.getChampionTemplate().weaponEnchant : cha.getEnchantEffect();
         this._collisionHeight = cha.getColHeight();
         this._collisionRadius = cha.getColRadius();
         this._isAttackable = cha.isAutoAttackable(attacker);
         if ((Config.SHOW_NPC_SERVER_NAME || cha.getTemplate().isCustom()) && attacker != null && attacker.isPlayer()) {
            this._name = attacker.getActingPlayer().getNpcName(cha.getTemplate());
         }

         if (this._npc.isInvisible()) {
            this._title = "Invisible";
         } else if (cha.getChampionTemplate() != null) {
            this._title = cha.getChampionTemplate().title;
         } else if ((Config.SHOW_NPC_SERVER_TITLE || cha.getTemplate().isCustom()) && attacker != null && attacker.isPlayer()) {
            this._title = attacker.getActingPlayer().getNpcTitle(cha.getTemplate());
         }

         if (Config.SHOW_NPC_LVL && this._npc instanceof MonsterInstance && ((Attackable)this._npc).canShowLevelInTitle()) {
            String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
            if (this._title != null) {
               t = t + " " + this._title;
            }

            this._title = t;
         }

         TerritoryWarManager.Territory territory = cha.getTerritory();
         if (territory != null && (Config.SHOW_CREST_WITHOUT_QUEST || territory.getLordObjectId() != 0 && territory.getOwnerClan() != null)) {
            Clan clan = territory.getOwnerClan();
            if (clan != null && (Config.SHOW_CREST_WITHOUT_QUEST || clan.getLeaderId() == territory.getLordObjectId())) {
               this._clanCrest = clan.getCrestId();
               this._clanId = clan.getId();
               this._allyCrest = clan.getAllyCrestId();
               this._allyId = clan.getAllyId();
            }
         }

         this._displayEffect = cha.getDisplayEffect();
      }

      @Override
      protected void writeImpl() {
         this.writeD(this._npc.getObjectId());
         this.writeD(this._idTemplate + 1000000);
         this.writeD(this._isAttackable ? 1 : 0);
         this.writeD(this._x);
         this.writeD(this._y);
         this.writeD(this._z + Config.CLIENT_SHIFTZ);
         this.writeD(this._heading);
         this.writeD(0);
         this.writeD((int)this._mAtkSpd);
         this.writeD((int)this._pAtkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._swimRunSpd);
         this.writeD(this._swimWalkSpd);
         this.writeD(this._flyRunSpd);
         this.writeD(this._flyWalkSpd);
         this.writeD(this._flyRunSpd);
         this.writeD(this._flyWalkSpd);
         this.writeF(this._moveMultiplier);
         this.writeF((double)this._npc.getAttackSpeedMultiplier());
         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(this._rhand);
         this.writeD(this._chest);
         this.writeD(this._lhand);
         this.writeC(1);
         this.writeC(this._npc.isRunning() ? 1 : 0);
         this.writeC(this._npc.isInCombat() ? 1 : 0);
         this.writeC(this._npc.isAlikeDead() ? 1 : 0);
         this.writeC(this._isSummoned ? 2 : 0);
         this.writeD(-1);
         this.writeS(this._name);
         this.writeD(-1);
         this.writeS(this._title);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(this._npc.isInvisible() ? this._npc.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask() : this._npc.getAbnormalEffectMask());
         this.writeD(this._clanId);
         this.writeD(this._clanCrest);
         this.writeD(this._allyId);
         this.writeD(this._allyCrest);
         this.writeC(this._npc.isInWater(this._npc) ? 1 : (this._npc.isFlying() ? 2 : 0));
         if (this._npc.getChampionTemplate() != null) {
            if (this._npc.getChampionTemplate().blueCircle) {
               this.writeC(1);
            } else if (this._npc.getChampionTemplate().redCircle) {
               this.writeC(2);
            } else {
               this.writeC(0);
            }
         } else {
            this.writeC(this._npc.getTeam());
         }

         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(this._enchantEffect);
         this.writeD(this._npc.isFlying() ? 1 : 0);
         this.writeD(0);
         this.writeD(this._npc.getColorEffect());
         this.writeC(this._npc.isTargetable() ? 1 : 0);
         this.writeC(this._npc.isShowName() ? 1 : 0);
         this.writeD(this._npc.getAbnormalEffectMask2());
         this.writeD(this._displayEffect);
      }
   }

   public static class SummonInfo extends NpcInfo {
      private final Summon _summon;
      private int _form = 0;
      private int _val = 0;

      public SummonInfo(Summon cha, Creature attacker, int val) {
         super(cha);
         this._summon = cha;
         this._val = val;
         if (this._summon.isShowSummonAnimation()) {
            this._val = 2;
         }

         int npcId = cha.getTemplate().getId();
         if (npcId == 16041 || npcId == 16042) {
            if (cha.getLevel() > 69) {
               this._form = 3;
            } else if (cha.getLevel() > 64) {
               this._form = 2;
            } else if (cha.getLevel() > 59) {
               this._form = 1;
            }
         } else if (npcId == 16025 || npcId == 16037) {
            if (cha.getLevel() > 69) {
               this._form = 3;
            } else if (cha.getLevel() > 64) {
               this._form = 2;
            } else if (cha.getLevel() > 59) {
               this._form = 1;
            }
         }

         this._isAttackable = cha.isAutoAttackable(attacker);
         this._rhand = cha.getWeapon();
         this._lhand = 0;
         this._chest = cha.getArmor();
         this._enchantEffect = cha.getTemplate().getEnchantEffect();
         this._name = cha.getName();
         this._title = cha.getOwner() != null ? (!cha.getOwner().isOnline() ? "" : cha.getOwner().getName()) : "";
         this._idTemplate = cha.getTemplate().getIdTemplate();
         this._collisionHeight = cha.getTemplate().getfCollisionHeight();
         this._collisionRadius = cha.getTemplate().getfCollisionRadius();
         this._invisible = cha.isInvisible();
      }

      @Override
      protected void writeImpl() {
         boolean gmSeeInvis = false;
         if (this._invisible) {
            Player activeChar = this.getClient().getActiveChar();
            if (activeChar != null && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)) {
               gmSeeInvis = true;
            }
         }

         this.writeD(this._summon.getObjectId());
         this.writeD(this._idTemplate + 1000000);
         this.writeD(this._isAttackable ? 1 : 0);
         this.writeD(this._x);
         this.writeD(this._y);
         this.writeD(this._z);
         this.writeD(this._heading);
         this.writeD(0);
         this.writeD((int)this._mAtkSpd);
         this.writeD((int)this._pAtkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._swimRunSpd);
         this.writeD(this._swimWalkSpd);
         this.writeD(this._flyRunSpd);
         this.writeD(this._flyWalkSpd);
         this.writeD(this._flyRunSpd);
         this.writeD(this._flyWalkSpd);
         this.writeF(this._moveMultiplier);
         this.writeF((double)this._summon.getAttackSpeedMultiplier());
         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(this._rhand);
         this.writeD(this._chest);
         this.writeD(this._lhand);
         this.writeC(1);
         this.writeC(1);
         this.writeC(this._summon.isInCombat() ? 1 : 0);
         this.writeC(this._summon.isAlikeDead() ? 1 : 0);
         this.writeC(this._val);
         this.writeD(-1);
         this.writeS(this._name);
         this.writeD(-1);
         this.writeS(this._title);
         this.writeD(1);
         this.writeD(this._summon.getPvpFlag());
         this.writeD(this._summon.getKarma());
         this.writeD(gmSeeInvis ? this._summon.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask() : this._summon.getAbnormalEffectMask());
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeC(0);
         this.writeC(this._summon.getTeam());
         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(this._enchantEffect);
         this.writeD(0);
         this.writeD(0);
         this.writeD(this._form);
         this.writeC(1);
         this.writeC(1);
         this.writeD(this._summon.getAbnormalEffectMask2());
      }
   }

   public static class TrapInfo extends NpcInfo {
      private final TrapInstance _trap;

      public TrapInfo(TrapInstance cha, Creature attacker) {
         super(cha);
         this._trap = cha;
         this._idTemplate = cha.getTemplate().getIdTemplate();
         this._isAttackable = cha.isAutoAttackable(attacker);
         this._rhand = 0;
         this._lhand = 0;
         this._collisionHeight = this._trap.getTemplate().getfCollisionHeight();
         this._collisionRadius = this._trap.getTemplate().getfCollisionRadius();
         this._name = cha.getName();
         this._title = cha.getOwner() != null ? cha.getOwner().getName() : "";
      }

      @Override
      protected void writeImpl() {
         this.writeD(this._trap.getObjectId());
         this.writeD(this._idTemplate + 1000000);
         this.writeD(this._isAttackable ? 1 : 0);
         this.writeD(this._x);
         this.writeD(this._y);
         this.writeD(this._z);
         this.writeD(this._heading);
         this.writeD(0);
         this.writeD((int)this._mAtkSpd);
         this.writeD((int)this._pAtkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeF(this._moveMultiplier);
         this.writeF((double)this._trap.getAttackSpeedMultiplier());
         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(this._rhand);
         this.writeD(this._chest);
         this.writeD(this._lhand);
         this.writeC(1);
         this.writeC(1);
         this.writeC(this._trap.isInCombat() ? 1 : 0);
         this.writeC(this._trap.isAlikeDead() ? 1 : 0);
         this.writeC(this._isSummoned ? 2 : 0);
         this.writeD(-1);
         this.writeS(this._name);
         this.writeD(-1);
         this.writeS(this._title);
         this.writeD(0);
         this.writeD(this._trap.getPvpFlag());
         this.writeD(this._trap.getKarma());
         this.writeD(this._trap.isInvisible() ? this._trap.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask() : this._trap.getAbnormalEffectMask());
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeC(0);
         this.writeC(this._trap.getTeam());
         this.writeF(this._collisionRadius);
         this.writeF(this._collisionHeight);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeC(1);
         this.writeC(1);
         this.writeD(0);
      }
   }
}
