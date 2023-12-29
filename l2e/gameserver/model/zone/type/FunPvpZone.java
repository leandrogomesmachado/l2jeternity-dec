package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.actor.templates.player.HwidTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;

public class FunPvpZone extends ZoneType {
   private boolean _isNoRestartZone = false;
   private boolean _isNoSummonFriendZone = false;
   private boolean _isNoLogoutZone = false;
   private boolean _isNoRevive = false;
   private boolean _isPvpEnabled = false;
   private boolean _allowPvpKills = false;
   private boolean _reviveNoblesse = false;
   private boolean _reviveHeal = false;
   private boolean _removeBuffs = false;
   private boolean _removePets = false;
   private boolean _giveNoblesse = false;
   private boolean _isPvpZone = false;
   private boolean _canUseCommunityBuffs = false;
   private boolean _canUseCommunityTp = false;
   private boolean _allotHwidsLimit = false;
   private int _hwidsLimit = 0;
   private int _radius = 100;
   private int _enchant = 0;
   private int _reviveDelay = 10;
   private int _flagDelay = 20;
   private int[][] _spawnLocs;
   private int _minLvl = 1;
   private int _maxLvl = 85;
   private int[] _zoneRewardId;
   private int[] _zoneRewardAmount;
   private int[] _zoneRewardChance;
   private final List<String> _items = new ArrayList<>();
   private final List<Integer> _skills = new ArrayList<>();
   private final List<String> _grades = new ArrayList<>();
   private final List<String> _classes = new ArrayList<>();
   private final String[] _gradeNames = new String[]{"", "D", "C", "B", "A", "S", "S80", "S84"};
   private int[][] _fighterBuffs = (int[][])null;
   private int[][] _mageBuffs = (int[][])null;
   private final List<HwidTemplate> _hwids = new ArrayList<>();

   public FunPvpZone(int id) {
      super(id);
      this.addZoneId(ZoneId.FUN_PVP);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("enablePvPFlag")) {
         this._isPvpEnabled = Boolean.parseBoolean(value);
      } else if (name.equals("enablePvPKills")) {
         this._allowPvpKills = Boolean.parseBoolean(value);
      } else if (name.equals("isPvpZone")) {
         this._isPvpZone = Boolean.parseBoolean(value);
         if (this._isPvpZone) {
            this.addZoneId(ZoneId.PVP);
         }
      } else if (name.equals("canUseCommunityBuffs")) {
         this._canUseCommunityBuffs = Boolean.parseBoolean(value);
      } else if (name.equals("canUseCommunityTeleports")) {
         this._canUseCommunityTp = Boolean.parseBoolean(value);
      } else if (name.equals("spawnLocations")) {
         this._spawnLocs = parseItemsList(value);
      } else if (name.equals("reviveDelay")) {
         this._reviveDelay = Integer.parseInt(value);
         if (this._reviveDelay != 0) {
            this._isNoRevive = true;
         }
      } else if (name.equals("giveNoblesse")) {
         this._giveNoblesse = Boolean.parseBoolean(value);
      } else if (name.equals("bannedItems")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            for(String i : propertySplit) {
               this._items.add(i);
            }
         }
      } else if (name.equals("bannedSkills")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            for(String i : propertySplit) {
               this._skills.add(Integer.parseInt(i));
            }
         }
      } else if (name.equals("bannedGrades")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            for(String i : propertySplit) {
               if (i.equals("D") || i.equals("C") || i.equals("B") || i.equals("A") || i.equals("S") || i.equals("S80") || i.equals("S84")) {
                  this._grades.add(i);
               }
            }
         }
      } else if (name.equals("bannedClasses")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            for(String i : propertySplit) {
               this._classes.add(i);
            }
         }
      } else if (name.equals("respawnRadius")) {
         this._radius = Integer.parseInt(value);
      } else if (name.equals("enchantLimit")) {
         this._enchant = Integer.parseInt(value);
      } else if (name.equals("removeBuffs")) {
         this._removeBuffs = Boolean.parseBoolean(value);
      } else if (name.equals("removePets")) {
         this._removePets = Boolean.parseBoolean(value);
      } else if (name.equals("isNoRestartZone")) {
         this._isNoRestartZone = Boolean.parseBoolean(value);
      } else if (name.equals("isNoSummonFriendZone")) {
         this._isNoSummonFriendZone = Boolean.parseBoolean(value);
         if (this._isNoSummonFriendZone) {
            this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
         }
      } else if (name.equals("isNoLogoutZone")) {
         this._isNoLogoutZone = Boolean.parseBoolean(value);
      } else if (name.equals("reviveNoblesse")) {
         this._reviveNoblesse = Boolean.parseBoolean(value);
      } else if (name.equals("reviveHeal")) {
         this._reviveHeal = Boolean.parseBoolean(value);
      } else if (name.equals("rewardItems")) {
         String[] rewardId = value.trim().split(",");
         this._zoneRewardId = new int[rewardId.length];

         try {
            int i = 0;

            for(String id : rewardId) {
               this._zoneRewardId[i++] = Integer.parseInt(id);
            }
         } catch (NumberFormatException var11) {
            _log.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         }
      } else if (name.equals("rewardAmount")) {
         String[] rewardCount = value.trim().split(",");
         this._zoneRewardAmount = new int[rewardCount.length];

         try {
            int i = 0;

            for(String count : rewardCount) {
               this._zoneRewardAmount[i++] = Integer.parseInt(count);
            }
         } catch (NumberFormatException var10) {
            _log.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
         }
      } else if (name.equals("rewardChance")) {
         String[] rewardChance = value.trim().split(",");
         this._zoneRewardChance = new int[rewardChance.length];

         try {
            int i = 0;

            for(String chance : rewardChance) {
               this._zoneRewardChance[i++] = Integer.parseInt(chance);
            }
         } catch (NumberFormatException var9) {
            _log.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      } else if (name.equals("rewardLvls")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            this._minLvl = Integer.parseInt(propertySplit[0]);
            this._maxLvl = Integer.parseInt(propertySplit[1]);
         }
      } else if (name.equals("fighterBuffs")) {
         this._fighterBuffs = this.parseBuffs(value);
      } else if (name.equals("mageBuffs")) {
         this._mageBuffs = this.parseBuffs(value);
      } else if (name.equals("flagDelay")) {
         this._flagDelay = Integer.parseInt(value);
      } else if (name.equals("hwidsLimit")) {
         this._hwidsLimit = Integer.parseInt(value);
         this._allotHwidsLimit = this._hwidsLimit > 0;
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if ((character.getActingPlayer() == null || !character.getActingPlayer().isInFightEvent()) && character.getReflectionId() <= 0) {
         if (!this._allotHwidsLimit || !character.isPlayer() || !this.checkHWID(character.getActingPlayer())) {
            if (this.isPvpZone() && character.isPlayer() && !character.isInsideZone(ZoneId.PVP, this)) {
               character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
            }

            if (character.isPlayer()) {
               Player activeChar = character.getActingPlayer();
               if (this._classes != null && this._classes.contains("" + activeChar.getClassId().getId())) {
                  ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(activeChar), 2000L);
                  activeChar.sendMessage("Your class is not allowed in the MultiFunction zone.");
                  return;
               }

               for(ItemInstance o : activeChar.getInventory().getItems()) {
                  if (o != null && o.isEquipable() && o.isEquipped() && !this.checkItem(o)) {
                     int slot = activeChar.getInventory().getSlotFromItem(o);
                     activeChar.getInventory().unEquipItemInBodySlot(slot);
                     activeChar.sendMessage(o.getName() + " unequiped because is not allowed inside this zone.");
                  }
               }

               this.clear(activeChar);
               this.buffPlayer(activeChar);
               if (this._isPvpEnabled) {
                  activeChar.stopPvpRegTask();
                  activeChar.updatePvPFlag(1);
               }

               if (this.isPvpZone()) {
                  activeChar.broadcastUserInfo(true);
               }
            }
         }
      }
   }

   @Override
   public void onPlayerLogoutInside(Player player) {
      if (!player.isInFightEvent() && player.getReflectionId() <= 0) {
         if (this._allotHwidsLimit) {
            this.removeHWIDInfo(player);
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if ((character.getActingPlayer() == null || !character.getActingPlayer().isInFightEvent()) && character.getReflectionId() <= 0) {
         if (this.isPvpZone() && character.isPlayer() && !character.isInsideZone(ZoneId.PVP, this)) {
            character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
         }

         if (character.isPlayer()) {
            Player activeChar = character.getActingPlayer();
            if (this._isPvpEnabled && this._flagDelay > 0) {
               activeChar.setPvpFlagLasts(System.currentTimeMillis() + (long)this._flagDelay * 1000L);
               activeChar.startPvPFlag();
            }

            if (this.isPvpZone()) {
               activeChar.broadcastUserInfo(true);
            }
         }

         if (this._allotHwidsLimit && character.isPlayer()) {
            this.removeHWIDInfo(character.getActingPlayer());
         }
      }
   }

   @Override
   public void onDieInside(Creature character) {
      if ((character.getActingPlayer() == null || !character.getActingPlayer().isInFightEvent()) && character.getReflectionId() <= 0) {
         if (character.isPlayer()) {
            final Player activeChar = character.getActingPlayer();
            if (this._isNoRevive) {
               ThreadPoolManager.getInstance()
                  .schedule(
                     new Runnable() {
                        @Override
                        public void run() {
                           activeChar.doRevive();
                           FunPvpZone.this.heal(activeChar);
                           if (FunPvpZone.this._spawnLocs != null) {
                              int[] loc = FunPvpZone.this._spawnLocs[Rnd.get(FunPvpZone.this._spawnLocs.length)];
                              activeChar.teleToLocation(
                                 loc[0] + Rnd.get(-FunPvpZone.this._radius, FunPvpZone.this._radius),
                                 loc[1] + Rnd.get(-FunPvpZone.this._radius, FunPvpZone.this._radius),
                                 loc[2],
                                 true
                              );
                           }
                        }
                     },
                     (long)(this._reviveDelay * 1000)
                  );
            }
         }
      }
   }

   @Override
   public void onReviveInside(Creature character) {
      if ((character.getActingPlayer() == null || !character.getActingPlayer().isInFightEvent()) && character.getReflectionId() <= 0) {
         if (character.isPlayer()) {
            this.buffPlayer(character.getActingPlayer());
            if (this._reviveNoblesse) {
               SkillsParser.getInstance().getInfo(1323, 1).getEffects(character.getActingPlayer(), character.getActingPlayer(), false);
            }

            if (this._reviveHeal) {
               this.heal(character.getActingPlayer());
            }
         }
      }
   }

   public boolean checkHWID(Player player) {
      String ipHwid = Config.PROTECTION.equalsIgnoreCase("NONE") ? player.getIPAddress() : player.getHWID();
      if (!this._hwids.isEmpty()) {
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
      if (!this._hwids.isEmpty()) {
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

   private void clear(Player player) {
      if ((player == null || !player.isInFightEvent()) && player.getReflectionId() <= 0) {
         if (this._removeBuffs) {
            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            if (this._removePets) {
               Summon pet = player.getSummon();
               if (pet != null) {
                  pet.stopAllEffectsExceptThoseThatLastThroughDeath();
                  pet.unSummon(player);
               }
            }
         } else if (this._removePets) {
            Summon pet = player.getSummon();
            if (pet != null) {
               pet.unSummon(player);
            }
         }
      }
   }

   private void heal(Player activeChar) {
      activeChar.setCurrentHp(activeChar.getMaxHp());
      activeChar.setCurrentCp(activeChar.getMaxCp());
      activeChar.setCurrentMp(activeChar.getMaxMp());
   }

   public void givereward(Player killer, Player player) {
      if ((killer == null || !killer.isInFightEvent()) && killer.getReflectionId() <= 0) {
         if (killer.isInsideZone(ZoneId.FUN_PVP) && this._zoneRewardChance != null) {
            if (killer.isInParty()) {
               for(Player pm : killer.getParty().getMembers()) {
                  if (pm != null && pm.isInsideZone(ZoneId.FUN_PVP) && this.isValidRewardLvl(pm)) {
                     int[] chance = this._zoneRewardChance;

                     for(int i = 0; i < chance.length; ++i) {
                        if (Rnd.chance(this._zoneRewardChance[i])) {
                           pm.addItem("Zone Reward", this._zoneRewardId[i], (long)this._zoneRewardAmount[i], pm, true);
                        }
                     }
                  }
               }
            } else if (this.isValidRewardLvl(killer)) {
               int[] chance = this._zoneRewardChance;

               for(int i = 0; i < chance.length; ++i) {
                  if (Rnd.chance(this._zoneRewardChance[i])) {
                     killer.addItem("Zone Reward", this._zoneRewardId[i], (long)this._zoneRewardAmount[i], killer, true);
                  }
               }
            }
         }
      }
   }

   public boolean checkSkill(Skill skill) {
      return this._skills == null || !this._skills.contains(skill.getId());
   }

   public boolean checkItem(ItemInstance item) {
      int o = item.getItem().getCrystalType();
      int e = item.getEnchantLevel();
      if (this._enchant != 0 && e >= this._enchant) {
         return false;
      } else if (this._grades != null && this._grades.contains(this._gradeNames[o])) {
         return false;
      } else {
         return this._items == null || !this._items.contains("" + item.getId());
      }
   }

   private static int[][] parseItemsList(String line) {
      String[] propertySplit = line.split(";");
      if (propertySplit.length == 0) {
         return (int[][])null;
      } else {
         int i = 0;
         int[][] result = new int[propertySplit.length][];

         for(String value : propertySplit) {
            String[] valueSplit = value.split(",");
            if (valueSplit.length != 3) {
               return (int[][])null;
            }

            result[i] = new int[3];

            try {
               result[i][0] = Integer.parseInt(valueSplit[0]);
            } catch (NumberFormatException var12) {
               return (int[][])null;
            }

            try {
               result[i][1] = Integer.parseInt(valueSplit[1]);
            } catch (NumberFormatException var11) {
               return (int[][])null;
            }

            try {
               result[i][2] = Integer.parseInt(valueSplit[2]);
            } catch (NumberFormatException var10) {
               return (int[][])null;
            }

            ++i;
         }

         return result;
      }
   }

   private int[][] parseBuffs(String buffs) {
      if (buffs != null && !buffs.isEmpty()) {
         StringTokenizer st = new StringTokenizer(buffs, ";");
         int[][] realBuffs = new int[st.countTokens()][2];

         for(int index = 0; st.hasMoreTokens(); ++index) {
            String[] skillLevel = st.nextToken().split(",");
            int[] realHourMin = new int[]{Integer.parseInt(skillLevel[0]), Integer.parseInt(skillLevel[1])};
            realBuffs[index] = realHourMin;
         }

         return realBuffs;
      } else {
         return (int[][])null;
      }
   }

   private void buffPlayer(Player player) {
      if (this._giveNoblesse) {
         SkillsParser.getInstance().getInfo(1323, 1).getEffects(player, player, false);
      }

      if (this._fighterBuffs != null && this._mageBuffs != null) {
         int[][] buffs;
         if (player.isMageClass()) {
            buffs = this._mageBuffs;
         } else {
            buffs = this._fighterBuffs;
         }

         giveBuffs(player, buffs, false);
         if (player.getSummon() != null) {
            giveBuffs(player, this._fighterBuffs, true);
         }
      }
   }

   private boolean isValidRewardLvl(Player player) {
      return player.getLevel() >= this._minLvl && player.getLevel() <= this._maxLvl;
   }

   private static void giveBuffs(Player player, int[][] buffs, boolean petbuff) {
      for(int[] buff1 : buffs) {
         Skill buff = SkillsParser.getInstance().getInfo(buff1[0], buff1[1]);
         if (buff != null) {
            if (!petbuff) {
               buff.getEffects(player, player, false);
            } else {
               buff.getEffects(player, player.getSummon(), false);
            }
         }
      }
   }

   public boolean isNoRestartZone() {
      return this._isNoRestartZone;
   }

   public boolean isNoLogoutZone() {
      return this._isNoLogoutZone;
   }

   public boolean canRevive() {
      return this._isNoRevive;
   }

   public boolean isPvpZone() {
      return this._isPvpZone;
   }

   public boolean canUseCbBuffs() {
      return this._canUseCommunityBuffs;
   }

   public boolean canUseCbTeleports() {
      return this._canUseCommunityTp;
   }

   public boolean allowPvpKills() {
      return this._allowPvpKills;
   }
}
