package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.actor.templates.player.HwidTemplate;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.entity.Siegable;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;

public class SiegeZone extends ZoneType {
   private static final int DISMOUNT_DELAY = 5;
   private int _fortId = 0;
   private int _castleId = 0;
   private int _clanHallId = 0;
   private boolean _allotHwidsLimit = false;
   private int _hwidsLimit = 0;
   private final List<HwidTemplate> _hwids = new ArrayList<>();

   public SiegeZone(int id) {
      super(id);
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new SiegeZone.Settings();
      }

      this.setSettings(settings);
   }

   public SiegeZone.Settings getSettings() {
      return (SiegeZone.Settings)super.getSettings();
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("castleId")) {
         if (this.getSettings().getSiegeableId() != -1) {
            throw new IllegalArgumentException("Siege object already defined!");
         }

         this.getSettings().setSiegeableId(Integer.parseInt(value));
         this._castleId = Integer.parseInt(value);
      } else if (name.equals("fortId")) {
         if (this.getSettings().getSiegeableId() != -1) {
            throw new IllegalArgumentException("Siege object already defined!");
         }

         this.getSettings().setSiegeableId(Integer.parseInt(value));
         this._fortId = Integer.parseInt(value);
      } else if (name.equals("clanHallId")) {
         if (this.getSettings().getSiegeableId() != -1) {
            throw new IllegalArgumentException("Siege object already defined!");
         }

         this.getSettings().setSiegeableId(Integer.parseInt(value));
         this._clanHallId = Integer.parseInt(value);
         SiegableHall hall = CHSiegeManager.getInstance().getConquerableHalls().get(this.getSettings().getSiegeableId());
         if (hall == null) {
            _log.warning("SiegeZone: Siegable clan hall with id " + value + " does not exist!");
         } else {
            hall.setSiegeZone(this);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this.getSettings().isActiveSiege() && character.isPlayer()) {
         Player plyer = character.getActingPlayer();
         if (plyer.getFarmSystem().isAutofarming()) {
            plyer.getFarmSystem().stopFarmTask(false);
         }

         if (this._allotHwidsLimit && this.checkHWID(character.getActingPlayer())) {
            return;
         }

         if (plyer.isRegisteredOnThisSiegeField(this.getSettings().getSiegeableId())) {
            plyer.setIsInSiege(true);
            if (this.getSettings().getSiege().giveFame() && this.getSettings().getSiege().getFameFrequency() > 0) {
               plyer.startFameTask((long)(this.getSettings().getSiege().getFameFrequency() * 1000), this.getSettings().getSiege().getFameAmount());
            }
         }

         character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
         if (!Config.ALLOW_WYVERN_DURING_SIEGE && plyer.getMountType() == MountType.WYVERN) {
            plyer.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
            plyer.enteredNoLanding(5);
         }

         if (Config.ALLOW_BLOCK_TRANSFORMS_AT_SIEGE
            && plyer.getTransformationId() > 0
            && Config.LIST_BLOCK_TRANSFORMS_AT_SIEGE.contains(plyer.getTransformationId())) {
            plyer.untransform();
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (this.getSettings().isActiveSiege() && character.isPlayer()) {
         Player player = character.getActingPlayer();
         if (player != null) {
            player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
            if (player.getMountType() == MountType.WYVERN) {
               player.exitedNoLanding();
            }

            if (player.getPvpFlag() == 0) {
               player.startPvPFlag();
            }

            if (this._allotHwidsLimit) {
               this.removeHWIDInfo(player);
            }
         }
      }

      if (character.isPlayer()) {
         Player activeChar = character.getActingPlayer();
         activeChar.stopFameTask();
         activeChar.setIsInSiege(false);
         if (this.getSettings().getSiege() instanceof FortSiege && activeChar.getInventory().getItemByItemId(9819) != null) {
            Fort fort = FortManager.getInstance().getFortById(this.getSettings().getSiegeableId());
            if (fort != null) {
               FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getId());
            } else {
               int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
               activeChar.getInventory().unEquipItemInBodySlot(slot);
               activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
            }
         }
      }

      if (character instanceof SiegeSummonInstance) {
         ((SiegeSummonInstance)character).unSummon(((SiegeSummonInstance)character).getOwner());
      }
   }

   @Override
   public void onPlayerLogoutInside(Player player) {
      if (this.getSettings().isActiveSiege() && this._allotHwidsLimit) {
         this.removeHWIDInfo(player);
      }
   }

   @Override
   public void onDieInside(Creature character) {
      if (this.getSettings().isActiveSiege()
         && character.isPlayer()
         && character.getActingPlayer().isRegisteredOnThisSiegeField(this.getSettings().getSiegeableId())) {
         int lvl = 1;
         Effect e = character.getFirstEffect(5660);
         if (e != null) {
            lvl = Math.min(lvl + e.getSkill().getLevel(), 5);
         }

         Skill skill = SkillsParser.getInstance().getInfo(5660, lvl);
         if (skill != null) {
            skill.getEffects(character, character, false);
         }
      }
   }

   public boolean checkHWID(Player player) {
      String ipHwid = Config.PROTECTION.equalsIgnoreCase("NONE") ? player.getIPAddress() : player.getHWID();
      if (this._hwids != null && !this._hwids.isEmpty()) {
         for(HwidTemplate tpl : this._hwids) {
            if (tpl != null && tpl.getPlayers().get(ipHwid) != null) {
               if (tpl.getAmount() >= this._hwidsLimit) {
                  ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
                  return true;
               }

               tpl.setAmount(true, player, ipHwid);
               return false;
            }
         }
      }

      this._hwids.add(new HwidTemplate(1, player, ipHwid));
      return false;
   }

   private void removeHWIDInfo(Player player) {
      if (this._hwids != null && !this._hwids.isEmpty()) {
         HwidTemplate toRemove = null;

         for(HwidTemplate tpl : this._hwids) {
            if (tpl != null) {
               tpl.setAmount(false, player, null);
               if (tpl.getAmount() <= 0) {
                  toRemove = tpl;
               }
            }
         }

         if (toRemove != null) {
            this._hwids.remove(toRemove);
         }
      }
   }

   public void updateZoneStatusForCharactersInside() {
      if (this.getSettings().isActiveSiege()) {
         this._hwids.clear();

         for(Creature character : this.getCharactersInside()) {
            if (character != null) {
               this.onEnter(character);
            }
         }
      } else {
         for(Creature character : this.getCharactersInside()) {
            if (character != null) {
               if (character.isPlayer()) {
                  Player player = character.getActingPlayer();
                  if (player != null) {
                     player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
                     player.stopFameTask();
                     if (player.getMountType() == MountType.WYVERN) {
                        player.exitedNoLanding();
                     }
                  }
               }

               if (character instanceof SiegeSummonInstance) {
                  ((SiegeSummonInstance)character).unSummon(((SiegeSummonInstance)character).getOwner());
               }

               this._hwids.clear();
            }
         }
      }
   }

   public void announceToPlayers(String message) {
      for(Player player : this.getPlayersInside()) {
         if (player != null) {
            player.sendMessage(message);
         }
      }
   }

   public int getSiegeObjectId() {
      return this.getSettings().getSiegeableId();
   }

   public boolean isActive() {
      return this.getSettings().isActiveSiege();
   }

   public void setIsActive(boolean val) {
      this.getSettings().setActiveSiege(val);
      if (val) {
         if (this.getCastleId() > 0) {
            this._hwidsLimit = SiegeManager.getInstance().getCastleHwidLimit();
            this._allotHwidsLimit = this._hwidsLimit > 0;
         } else if (this.getFortId() > 0) {
            this._hwidsLimit = FortSiegeManager.getInstance().getFortHwidLimit();
            this._allotHwidsLimit = this._hwidsLimit > 0;
         } else if (this.getClanHallId() > 0) {
            this._hwidsLimit = Config.CLAN_HALL_HWID_LIMIT;
            this._allotHwidsLimit = this._hwidsLimit > 0;
         }

         this.addZoneId(ZoneId.PVP);
         this.addZoneId(ZoneId.SIEGE);
         this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
      } else {
         this.getZoneId().clear();
      }
   }

   public void setSiegeInstance(Siegable siege) {
      this.getSettings().setSiege(siege);
   }

   public void banishForeigners(int owningClanId) {
      TeleportWhereType type = TeleportWhereType.TOWN;

      for(Player temp : this.getPlayersInside()) {
         if (temp.getClanId() != owningClanId) {
            temp.teleToLocation(type, true);
         }
      }
   }

   public int getCastleId() {
      return this._castleId;
   }

   public int getFortId() {
      return this._fortId;
   }

   public int getClanHallId() {
      return this._clanHallId;
   }

   private final class Settings extends AbstractZoneSettings {
      private int _siegableId = -1;
      private Siegable _siege = null;
      private boolean _isActiveSiege = false;

      public Settings() {
      }

      public int getSiegeableId() {
         return this._siegableId;
      }

      protected void setSiegeableId(int id) {
         this._siegableId = id;
      }

      public Siegable getSiege() {
         return this._siege;
      }

      public void setSiege(Siegable s) {
         this._siege = s;
      }

      public boolean isActiveSiege() {
         return this._isActiveSiege;
      }

      public void setActiveSiege(boolean val) {
         this._isActiveSiege = val;
      }

      @Override
      public void clear() {
         this._siegableId = -1;
         this._siege = null;
         this._isActiveSiege = false;
      }
   }
}
