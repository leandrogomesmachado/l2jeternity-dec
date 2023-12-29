package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.listener.events.SiegeEvent;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public final class Benom extends AbstractNpcAI {
   private static final int CASTLE = 8;
   private static final int VENOM = 29054;
   private static final int TELEPORT_CUBE = 29055;
   private static final int DUNGEON_KEEPER = 35506;
   private static final byte ALIVE = 0;
   private static final byte DEAD = 1;
   private static final int HOURS_BEFORE = 24;
   private static final Location[] TARGET_TELEPORTS = new Location[]{
      new Location(12860, -49158, 976),
      new Location(14878, -51339, 1024),
      new Location(15674, -49970, 864),
      new Location(15696, -48326, 864),
      new Location(14873, -46956, 1024),
      new Location(12157, -49135, -1088),
      new Location(12875, -46392, -288),
      new Location(14087, -46706, -288),
      new Location(14086, -51593, -288),
      new Location(12864, -51898, -288),
      new Location(15538, -49153, -1056),
      new Location(17001, -49149, -1064)
   };
   private static final Location TRHONE = new Location(11025, -49152, -537);
   private static final Location DUNGEON = new Location(11882, -49216, -3008);
   private static final Location TELEPORT = new Location(12589, -49044, -3008);
   private static final Location CUBE = new Location(12047, -49211, -3009);
   private static final SkillHolder VENOM_STRIKE = new SkillHolder(4993, 1);
   private static final SkillHolder SONIC_STORM = new SkillHolder(4994, 1);
   private static final SkillHolder VENOM_TELEPORT = new SkillHolder(4995, 1);
   private static final SkillHolder RANGE_TELEPORT = new SkillHolder(4996, 1);
   private Npc _venom;
   private Npc _massymore;
   private int _venomX;
   private int _venomY;
   private int _venomZ;
   private boolean _aggroMode = false;
   private boolean _prisonIsOpen = false;
   private static final int[] TARGET_TELEPORTS_OFFSET = new int[]{650, 100, 100, 100, 100, 650, 200, 200, 200, 200, 200, 650};
   private static List<Player> _targets = new ArrayList<>();

   private Benom(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{35506, 29055});
      this.addTalkId(new int[]{35506, 29055});
      this.addSpawnId(new int[]{29054});
      this.addSpellFinishedId(new int[]{29054});
      this.addAttackId(29054);
      this.addKillId(29054);
      this.addAggroRangeEnterId(new int[]{29054});
      this.addSiegeNotify();

      for(Spawner spawns : SpawnParser.getInstance().getSpawnData()) {
         if (spawns != null && spawns.getId() == 35506) {
            this._massymore = spawns.getLastSpawn();
         }
      }

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         switch(spawn.getId()) {
            case 29054:
               this._venom = spawn.getLastSpawn();
               this._venomX = this._venom.getX();
               this._venomY = this._venom.getY();
               this._venomZ = this._venom.getZ();
               this._venom.disableSkill(VENOM_TELEPORT.getSkill(), 0L);
               this._venom.disableSkill(RANGE_TELEPORT.getSkill(), 0L);
               this._venom.doRevive();
               ((Attackable)this._venom).setCanReturnToSpawnPoint(false);
               if (this.checkStatus() == 1) {
                  this._venom.deleteMe();
               }
         }
      }

      long currentTime = System.currentTimeMillis();
      long startSiegeDate = CastleManager.getInstance().getCastleById(8).getSiegeDate().getTimeInMillis();
      long openingDungeonDate = startSiegeDate - 8640000L;
      if (currentTime > openingDungeonDate && currentTime < startSiegeDate) {
         this._prisonIsOpen = true;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      switch(npc.getId()) {
         case 29055:
            talker.teleToLocation(TeleportWhereType.TOWN, true);
            break;
         case 35506:
            if (!this._prisonIsOpen) {
               return "<html><body>" + ServerStorage.getInstance().getString(talker.getLang(), "Benom.CLOSED") + "</body></html>";
            }

            talker.teleToLocation(TELEPORT, 0, true);
      }

      return super.onTalk(npc, talker);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "tower_check":
            if (CastleManager.getInstance().getCastleById(8).getSiege().getControlTowerCount() <= 1) {
               this.changeLocation(Benom.MoveTo.THRONE);
               this.broadcastNpcSay(
                  this._massymore, 23, NpcStringId.OH_NO_THE_DEFENSES_HAVE_FAILED_IT_IS_TOO_DANGEROUS_TO_REMAIN_INSIDE_THE_CASTLE_FLEE_EVERY_MAN_FOR_HIMSELF
               );
               this.cancelQuestTimer("tower_check", npc, null);
               this.startQuestTimer("raid_check", 10000L, npc, null, true);
            }
            break;
         case "raid_check":
            if ((
                  npc.getX() != this._venomX && npc.getY() != this._venomY && npc.getAI().getIntention() == CtrlIntention.ACTIVE
                     || !npc.isInsideRadius(this._venomX, this._venomY, this._venomZ, 2500, true, true)
               )
               && !npc.isTeleporting()) {
               ((Attackable)npc).clearAggroList();
               npc.broadcastPacket(new MagicSkillUse(npc, npc, 2036, 1, 500, 0));
               npc.teleToLocation(new Location(this._venomX, this._venomY, this._venomZ), false);
            }
            break;
         case "cube_despawn":
            if (npc != null) {
               npc.deleteMe();
            }
      }

      return event;
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (isSummon) {
         return super.onAggroRangeEnter(npc, player, isSummon);
      } else {
         if (this._aggroMode && _targets.size() < 10 && getRandom(3) < 1 && !player.isDead()) {
            _targets.add(player);
         }

         return super.onAggroRangeEnter(npc, player, isSummon);
      }
   }

   @Override
   public boolean onSiegeEvent(SiegeEvent event) {
      if (event.getSiege().getCastle().getId() == 8) {
         if (event.getSiege().getCastle().getIsTimeRegistrationOver() && !event.getSiege().getAttackerClans().isEmpty()) {
            this._prisonIsOpen = true;
            this.changeLocation(Benom.MoveTo.PRISON);
         }

         switch(event.getStage()) {
            case START:
               this._aggroMode = true;
               this._prisonIsOpen = false;
               if (this._venom != null && !this._venom.isDead()) {
                  this._venom.setCurrentHp(this._venom.getMaxHp());
                  this._venom.setCurrentMp(this._venom.getMaxMp());
                  this._venom.enableSkill(VENOM_TELEPORT.getSkill());
                  this._venom.enableSkill(RANGE_TELEPORT.getSkill());
                  this.startQuestTimer("tower_check", 30000L, this._venom, null, true);
               }
               break;
            case END:
               this._aggroMode = false;
               if (this._venom != null && !this._venom.isDead()) {
                  this.changeLocation(Benom.MoveTo.PRISON);
                  this._venom.disableSkill(VENOM_TELEPORT.getSkill(), 0L);
                  this._venom.disableSkill(RANGE_TELEPORT.getSkill(), 0L);
               }

               this.updateStatus(0);
               this.cancelQuestTimer("tower_check", this._venom, null);
               this.cancelQuestTimer("raid_check", this._venom, null);
         }
      }

      return true;
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      switch(skill.getId()) {
         case 4222:
            npc.teleToLocation(new Location(this._venomX, this._venomY, this._venomZ), false);
            break;
         case 4995:
            this.teleportTarget(player);
            ((Attackable)npc).stopHating(player);
            break;
         case 4996:
            this.teleportTarget(player);
            ((Attackable)npc).stopHating(player);
            if (_targets != null && _targets.size() > 0) {
               for(Player target : _targets) {
                  long x = (long)(player.getX() - target.getX());
                  long y = (long)(player.getY() - target.getY());
                  long z = (long)(player.getZ() - target.getZ());
                  long range = 250L;
                  if (x * x + y * y + z * z <= 62500L) {
                     this.teleportTarget(target);
                     ((Attackable)npc).stopHating(target);
                  }
               }

               _targets.clear();
            }
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (!npc.isTeleporting()) {
         if (this.checkStatus() == 1) {
            npc.deleteMe();
         } else {
            npc.doRevive();
            this.broadcastNpcSay(
               npc,
               23,
               NpcStringId.WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE_LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD
            );
         }
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (!npc.isInsideRadius(this._venomX, this._venomY, this._venomZ, 2500, true, true)) {
         ((Attackable)npc).clearAggroList();
         npc.broadcastPacket(new MagicSkillUse(npc, npc, 2036, 1, 500, 0));
         npc.teleToLocation(new Location(this._venomX, this._venomY, this._venomZ), false);
      }

      double distance = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
      if (this._aggroMode && npc.getCurrentHp() < npc.getMaxHp() / 2.0 && getRandom(100) < 5 && !npc.isCastingNow()) {
         npc.setTarget(attacker);
         npc.doCast(VENOM_TELEPORT.getSkill());
      } else if (this._aggroMode && npc.getCurrentHp() < npc.getMaxHp() / 3.0 && getRandom(100) < 2 && !npc.isCastingNow()) {
         npc.setTarget(attacker);
         npc.doCast(RANGE_TELEPORT.getSkill());
      } else if (distance > 300.0 && getRandom(100) < 10 && !npc.isCastingNow()) {
         npc.setTarget(attacker);
         npc.doCast(VENOM_STRIKE.getSkill());
      } else if (getRandom(100) < 10 && !npc.isCastingNow()) {
         npc.setTarget(attacker);
         npc.doCast(SONIC_STORM.getSkill());
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      this.updateStatus(1);
      this.broadcastNpcSay(npc, 23, NpcStringId.ITS_NOT_OVER_YET_IT_WONT_BE_OVER_LIKE_THIS_NEVER);
      if (!CastleManager.getInstance().getCastleById(8).getSiege().getIsInProgress()) {
         Npc cube = addSpawn(29055, CUBE, false, 0L);
         this.startQuestTimer("cube_despawn", 120000L, cube, null);
      }

      this.cancelQuestTimer("raid_check", npc, null);
      return super.onKill(npc, killer, isSummon);
   }

   private void changeLocation(Benom.MoveTo loc) {
      switch(loc) {
         case THRONE:
            this._venom.getTemplate().setAggroRange(1500);
            this._venom.getTemplate().setBaseMoveSpeed(MoveType.RUN, 350.0);
            this._venom.teleToLocation(TRHONE, false);
            break;
         case PRISON:
            if (this._venom != null && !this._venom.isDead() && !this._venom.isDecayed()) {
               this._venom.getTemplate().setAggroRange(0);
               this._venom.getTemplate().setBaseMoveSpeed(MoveType.RUN, 220.0);
               this._venom.teleToLocation(DUNGEON, false);
            } else {
               this._venom = addSpawn(29054, DUNGEON, false, 0L);
            }

            this.cancelQuestTimer("raid_check", this._venom, null);
            this.cancelQuestTimer("tower_check", this._venom, null);
      }

      this._venomX = this._venom.getX();
      this._venomY = this._venom.getY();
      this._venomZ = this._venom.getZ();
   }

   private void teleportTarget(Player player) {
      if (player != null && !player.isDead()) {
         int rnd = getRandom(11);
         player.teleToLocation(TARGET_TELEPORTS[rnd], TARGET_TELEPORTS_OFFSET[rnd], true);
         player.getAI().setIntention(CtrlIntention.IDLE);
      }
   }

   private int checkStatus() {
      int checkStatus = 0;
      if (GlobalVariablesManager.getInstance().isVariableStored("VenomStatus")) {
         checkStatus = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("VenomStatus"));
      } else {
         GlobalVariablesManager.getInstance().storeVariable("VenomStatus", "0");
      }

      return checkStatus;
   }

   private void updateStatus(int status) {
      GlobalVariablesManager.getInstance().storeVariable("VenomStatus", Integer.toString(status));
   }

   public static void main(String[] args) {
      new Benom(Benom.class.getSimpleName(), "ai");
   }

   private static enum MoveTo {
      THRONE,
      PRISON;
   }
}
