package l2e.gameserver.model.zone.type;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.GameServer;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.ZoneType;

public class BossZone extends ZoneType {
   private int _timeInvade;
   private boolean _enabled = true;
   private boolean _canTeleport = false;
   private int[] _oustLoc = new int[]{0, 0, 0};

   public BossZone(int id) {
      super(id);
      this._oustLoc = new int[3];
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new BossZone.Settings();
      }

      this.setSettings(settings);
      EpicBossManager.getInstance().addZone(this);
   }

   public BossZone.Settings getSettings() {
      return (BossZone.Settings)super.getSettings();
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("InvadeTime")) {
         this._timeInvade = Integer.parseInt(value);
      } else if (name.equals("default_enabled")) {
         this._enabled = Boolean.parseBoolean(value);
      } else if (name.equals("canTeleport")) {
         this._canTeleport = Boolean.parseBoolean(value);
      } else if (name.equals("oustX")) {
         this._oustLoc[0] = Integer.parseInt(value);
      } else if (name.equals("oustY")) {
         this._oustLoc[1] = Integer.parseInt(value);
      } else if (name.equals("oustZ")) {
         this._oustLoc[2] = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this._enabled) {
         if (character.isPlayer()) {
            Player player = character.getActingPlayer();
            if (player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) || player.isInFightEvent()) {
               return;
            }

            if (this.getSettings().getPlayersAllowed().contains(player.getObjectId())) {
               Long expirationTime = this.getSettings().getPlayerAllowedReEntryTimes().get(player.getObjectId());
               if (expirationTime == null) {
                  long serverStartTime = GameServer.dateTimeServerStarted.getTimeInMillis();
                  if (serverStartTime > System.currentTimeMillis() - (long)this._timeInvade) {
                     return;
                  }
               } else {
                  this.getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
                  if (expirationTime > System.currentTimeMillis()) {
                     return;
                  }
               }

               this.getSettings().getPlayersAllowed().remove(this.getSettings().getPlayersAllowed().indexOf(player.getObjectId()));
            }

            if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
               player.teleToLocation(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], true);
            } else {
               ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
            }
         } else if (character.isSummon()) {
            Player player = character.getActingPlayer();
            if (player != null) {
               if (this.getSettings().getPlayersAllowed().contains(player.getObjectId())
                  || player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)
                  || player.isInFightEvent()) {
                  return;
               }

               if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
                  player.teleToLocation(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], true);
               } else {
                  ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
               }
            }

            ((Summon)character).unSummon(player);
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (this._enabled) {
         if (character.isPlayer()) {
            Player player = character.getActingPlayer();
            if (player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) || player.isInFightEvent()) {
               return;
            }

            if (!player.isOnline() && this.getSettings().getPlayersAllowed().contains(player.getObjectId())) {
               this.getSettings().getPlayerAllowedReEntryTimes().put(player.getObjectId(), System.currentTimeMillis() + (long)this._timeInvade);
            } else {
               if (this.getSettings().getPlayersAllowed().contains(player.getObjectId())) {
                  this.getSettings().getPlayersAllowed().remove(this.getSettings().getPlayersAllowed().indexOf(player.getObjectId()));
               }

               this.getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
            }
         }

         if (character.isPlayable() && this.getCharactersInside() != null && !this.getCharactersInside().isEmpty()) {
            this.getSettings().getRaidList().clear();
            int count = 0;

            for(Creature obj : this.getCharactersInside()) {
               if (obj != null) {
                  if (obj.isPlayable()) {
                     ++count;
                  } else if (obj.isAttackable() && obj.isRaid()) {
                     this.getSettings().getRaidList().add(obj);
                  }
               }
            }

            if (count == 0 && !this.getSettings().getRaidList().isEmpty()) {
               for(int i = 0; i < this.getSettings().getRaidList().size(); ++i) {
                  Attackable raid = (Attackable)this.getSettings().getRaidList().get(i);
                  if (raid != null
                     && raid.getSpawn() != null
                     && !raid.isDead()
                     && !raid.isInsideRadius(raid.getSpawn().getX(), raid.getSpawn().getY(), 150, false)) {
                     raid.returnHome();
                  }
               }
            }
         }
      }

      if (character.isAttackable() && character.isRaid() && !character.isDead()) {
         ((Attackable)character).returnHome();
      }
   }

   public void setZoneEnabled(boolean flag) {
      if (this._enabled != flag) {
         this.oustAllPlayers();
      }

      this._enabled = flag;
   }

   public int getTimeInvade() {
      return this._timeInvade;
   }

   public void setAllowedPlayers(List<Integer> players) {
      if (players != null) {
         this.getSettings().getPlayersAllowed().clear();
         this.getSettings().getPlayersAllowed().addAll(players);
      }
   }

   public List<Integer> getAllowedPlayers() {
      return this.getSettings().getPlayersAllowed();
   }

   public boolean isPlayerAllowed(Player player) {
      if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && !player.isInFightEvent()) {
         if (this.getSettings().getPlayersAllowed().contains(player.getObjectId())) {
            return true;
         } else {
            if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
               player.teleToLocation(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], true);
            } else {
               ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public void movePlayersTo(int x, int y, int z) {
      if (!this._characterList.isEmpty()) {
         for(Creature character : this.getCharactersInside()) {
            if (character != null && character.isPlayer()) {
               Player player = character.getActingPlayer();
               if (player.isOnline() && !player.isInFightEvent()) {
                  player.teleToLocation(x, y, z, true);
               }
            }
         }
      }
   }

   public void oustAllPlayers() {
      if (!this._characterList.isEmpty()) {
         for(Creature character : this.getCharactersInside()) {
            if (character != null && character.isPlayer()) {
               Player player = character.getActingPlayer();
               if (player.isOnline() && !player.isInFightEvent()) {
                  if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
                     player.teleToLocation(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], true);
                  } else {
                     ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
                  }
               }
            }
         }

         this.getSettings().getPlayerAllowedReEntryTimes().clear();
         this.getSettings().getPlayersAllowed().clear();
      }
   }

   public void allowPlayerEntry(Player player, int durationInSec) {
      if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)) {
         if (!this.getSettings().getPlayersAllowed().contains(player.getObjectId())) {
            this.getSettings().getPlayersAllowed().add(player.getObjectId());
         }

         this.getSettings().getPlayerAllowedReEntryTimes().put(player.getObjectId(), System.currentTimeMillis() + (long)(durationInSec * 1000));
      }
   }

   public void removePlayer(Player player) {
      if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)) {
         this.getSettings().getPlayersAllowed().remove(Integer.valueOf(player.getObjectId()));
         this.getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
      }
   }

   public void setCanTeleport(boolean canTele) {
      this._canTeleport = canTele;
   }

   public boolean isCanTeleport() {
      return this._canTeleport;
   }

   private final class Settings extends AbstractZoneSettings {
      private final Map<Integer, Long> _playerAllowedReEntryTimes = new ConcurrentHashMap<>();
      private final List<Integer> _playersAllowed = new CopyOnWriteArrayList<>();
      private final List<Creature> _raidList = new CopyOnWriteArrayList<>();

      public Settings() {
      }

      public Map<Integer, Long> getPlayerAllowedReEntryTimes() {
         return this._playerAllowedReEntryTimes;
      }

      public List<Integer> getPlayersAllowed() {
         return this._playersAllowed;
      }

      public List<Creature> getRaidList() {
         return this._raidList;
      }

      @Override
      public void clear() {
         this._playerAllowedReEntryTimes.clear();
         this._playersAllowed.clear();
         this._raidList.clear();
      }
   }
}
