package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.CastleUpdater;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ArtefactInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.actor.templates.SeedTemplate;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.type.CastleZone;
import l2e.gameserver.model.zone.type.ResidenceTeleportZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Castle implements IIdentifiable {
   protected static final Logger _log = Logger.getLogger(Castle.class.getName());
   private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
   private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
   private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
   private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
   private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
   private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
   private int _castleId = 0;
   private final List<DoorInstance> _doors = new ArrayList<>();
   private String _name = "";
   private int _ownerId = 0;
   private Siege _siege = null;
   private Calendar _siegeDate;
   private boolean _isTimeRegistrationOver = true;
   private Calendar _siegeTimeRegistrationEndDate;
   private int _taxPercent = 0;
   private double _taxRate = 0.0;
   private long _treasury = 0L;
   private SiegeZone _zone = null;
   private CastleZone _castleZone = null;
   private ResidenceTeleportZone _teleZone;
   private Clan _formerOwner = null;
   private final List<ArtefactInstance> _artefacts = new ArrayList<>(1);
   private final Map<Integer, Castle.CastleFunction> _function;
   private final List<Skill> _residentialSkills = new ArrayList<>();
   private int _ticketBuyCount = 0;
   private List<CropProcureTemplate> _procure = new ArrayList<>();
   private List<SeedTemplate> _production = new ArrayList<>();
   private List<CropProcureTemplate> _procureNext = new ArrayList<>();
   private List<SeedTemplate> _productionNext = new ArrayList<>();
   private boolean _isNextPeriodApproved = false;
   public static final int FUNC_TELEPORT = 1;
   public static final int FUNC_RESTORE_HP = 2;
   public static final int FUNC_RESTORE_MP = 3;
   public static final int FUNC_RESTORE_EXP = 4;
   public static final int FUNC_SUPPORT = 5;

   public Castle(int castleId) {
      this._castleId = castleId;
      this.load();
      this._function = new ConcurrentHashMap<>();

      for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(castleId)) {
         Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
         if (sk != null) {
            this._residentialSkills.add(sk);
         } else {
            _log.warning("Castle Id: " + castleId + " has a null residential skill Id: " + s.getId() + " level: " + s.getLvl() + "!");
         }
      }

      if (this.getOwnerId() != 0) {
         this.loadFunctions();
      }
   }

   public Castle.CastleFunction getFunction(int type) {
      return this._function.containsKey(type) ? this._function.get(type) : null;
   }

   public synchronized void engrave(Clan clan, GameObject target) {
      if (this._artefacts.contains(target)) {
         this.setOwner(clan);
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_ENGRAVED_RULER);
         msg.addString(clan.getName());
         this.getSiege().announceToPlayer(msg, true);
      }
   }

   public void addToTreasury(long amount) {
      if (this.getOwnerId() > 0) {
         if (this._name.equalsIgnoreCase("Schuttgart") || this._name.equalsIgnoreCase("Goddard")) {
            Castle rune = CastleManager.getInstance().getCastle("rune");
            if (rune != null) {
               long runeTax = (long)((double)amount * rune.getTaxRate());
               if (rune.getOwnerId() > 0) {
                  rune.addToTreasury(runeTax);
               }

               amount -= runeTax;
            }
         }

         if (!this._name.equalsIgnoreCase("aden")
            && !this._name.equalsIgnoreCase("Rune")
            && !this._name.equalsIgnoreCase("Schuttgart")
            && !this._name.equalsIgnoreCase("Goddard")) {
            Castle aden = CastleManager.getInstance().getCastle("aden");
            if (aden != null) {
               long adenTax = (long)((double)amount * aden.getTaxRate());
               if (aden.getOwnerId() > 0) {
                  aden.addToTreasury(adenTax);
               }

               amount -= adenTax;
            }
         }

         this.addToTreasuryNoTax(amount);
      }
   }

   public boolean addToTreasuryNoTax(long amount) {
      if (this.getOwnerId() <= 0) {
         return false;
      } else {
         if (amount < 0L) {
            amount *= -1L;
            if (this._treasury < amount) {
               return false;
            }

            this._treasury -= amount;
         } else if (this._treasury + amount > PcInventory.MAX_ADENA) {
            this._treasury = PcInventory.MAX_ADENA;
         } else {
            this._treasury += amount;
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE castle SET treasury = ? WHERE id = ?");
         ) {
            statement.setLong(1, this.getTreasury());
            statement.setInt(2, this.getId());
            statement.execute();
         } catch (Exception var35) {
            _log.log(Level.WARNING, var35.getMessage(), (Throwable)var35);
         }

         return true;
      }
   }

   public void banishForeigners() {
      this.getCastleZone().banishForeigners(this.getOwnerId());
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this.getZone().isInsideZone(x, y, z);
   }

   public SiegeZone getZone() {
      if (this._zone == null) {
         for(SiegeZone zone : ZoneManager.getInstance().getAllZones(SiegeZone.class)) {
            if (zone.getSiegeObjectId() == this.getId()) {
               this._zone = zone;
               break;
            }
         }
      }

      return this._zone;
   }

   public CastleZone getCastleZone() {
      if (this._castleZone == null) {
         for(CastleZone zone : ZoneManager.getInstance().getAllZones(CastleZone.class)) {
            if (zone.getCastleId() == this.getId()) {
               this._castleZone = zone;
               break;
            }
         }
      }

      return this._castleZone;
   }

   public ResidenceTeleportZone getTeleZone() {
      if (this._teleZone == null) {
         for(ResidenceTeleportZone zone : ZoneManager.getInstance().getAllZones(ResidenceTeleportZone.class)) {
            if (zone.getResidenceId() == this.getId()) {
               this._teleZone = zone;
               break;
            }
         }
      }

      return this._teleZone;
   }

   public void oustAllPlayers() {
      this.getTeleZone().oustAllPlayers();
   }

   public double getDistance(GameObject obj) {
      return this.getZone().getDistanceToZone(obj);
   }

   public void closeDoor(Player activeChar, int doorId) {
      this.openCloseDoor(activeChar, doorId, false);
   }

   public void openDoor(Player activeChar, int doorId) {
      this.openCloseDoor(activeChar, doorId, true);
   }

   public void openCloseDoor(Player activeChar, int doorId, boolean open) {
      if (activeChar.getClanId() == this.getOwnerId()) {
         DoorInstance door = this.getDoor(doorId);
         if (door != null) {
            if (open) {
               door.openMe();
            } else {
               door.closeMe();
            }
         }
      }
   }

   public void removeUpgrade() {
      this.removeDoorUpgrade();

      for(Integer fc : this._function.keySet()) {
         this.removeFunction(fc);
      }

      this._function.clear();
   }

   public void setOwner(Clan clan) {
      Clan oldOwner = null;
      if (this.getOwnerId() > 0 && (clan == null || clan.getId() != this.getOwnerId())) {
         oldOwner = ClanHolder.getInstance().getClan(this.getOwnerId());
         if (oldOwner != null) {
            if (this._formerOwner == null) {
               this._formerOwner = oldOwner;
               if (Config.REMOVE_CASTLE_CIRCLETS) {
                  CastleManager.getInstance().removeCirclet(this._formerOwner, this.getId());
               }
            }

            try {
               Player oldleader = oldOwner.getLeader().getPlayerInstance();
               if (oldleader != null && oldleader.getMountType() == MountType.WYVERN) {
                  oldleader.dismount();
               }
            } catch (Exception var9) {
               _log.log(Level.WARNING, "Exception in setOwner: " + var9.getMessage(), (Throwable)var9);
            }

            oldOwner.setCastleId(0);
            this.getTerritory().changeOwner(null);

            for(Player member : oldOwner.getOnlineMembers(0)) {
               this.removeResidentialSkills(member);
               member.sendSkillList(false);
               if (Config.ALLOW_DAILY_TASKS && member.getActiveDailyTasks() != null) {
                  for(PlayerTaskTemplate taskTemplate : member.getActiveDailyTasks()) {
                     if (taskTemplate.getType().equalsIgnoreCase("Siege") && !taskTemplate.isComplete()) {
                        DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                        if (task.getSiegeCastle()) {
                           taskTemplate.setIsComplete(true);
                           member.updateDailyStatus(taskTemplate);
                           IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                           if (vch != null) {
                              vch.useVoicedCommand("missions", member, null);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.updateOwnerInDB(clan);
      if (clan != null && clan.getFortId() > 0) {
         FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
      }

      this.getTerritory().setOwnerClan(clan);
      if (clan != null) {
         if (Config.SHOW_CREST_WITHOUT_QUEST) {
            for(Npc npc : World.getInstance().getNpcs()) {
               if (npc != null && npc.getTerritory() == this.getTerritory()) {
                  npc.broadcastInfo();
               }
            }
         }

         for(Player member : clan.getOnlineMembers(0)) {
            this.giveResidentialSkills(member);
            member.sendSkillList(false);
         }
      }

      this.updateClansReputation();

      for(Player member : clan.getOnlineMembers(0)) {
         this.giveResidentialSkills(member);
         member.sendSkillList(false);
      }

      if (this.getSiege().getIsInProgress()) {
         this.getSiege().midVictory();
      }
   }

   public void removeOwner(Clan clan) {
      if (clan != null) {
         this._formerOwner = clan;
         if (Config.REMOVE_CASTLE_CIRCLETS) {
            CastleManager.getInstance().removeCirclet(this._formerOwner, this.getId());
         }

         for(Player member : clan.getOnlineMembers(0)) {
            this.removeResidentialSkills(member);
            member.sendSkillList(false);
         }

         clan.setCastleId(0);
         clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
      }

      this.updateOwnerInDB(null);
      if (this.getSiege().getIsInProgress()) {
         this.getSiege().midVictory();
      }

      for(Integer fc : this._function.keySet()) {
         this.removeFunction(fc);
      }

      this._function.clear();
   }

   public void setTaxPercent(Player activeChar, int taxPercent) {
      int maxTax;
      switch(SevenSigns.getInstance().getSealOwner(3)) {
         case 1:
            maxTax = 5;
            break;
         case 2:
            maxTax = 25;
            break;
         default:
            maxTax = 15;
      }

      if (taxPercent >= 0 && taxPercent <= maxTax) {
         this.setTaxPercent(taxPercent);
         activeChar.sendMessage(this.getName() + " castle tax changed to " + taxPercent + "%.");
      } else {
         activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
      }
   }

   public void setTaxPercent(int taxPercent) {
      this._taxPercent = taxPercent;
      this._taxRate = (double)this._taxPercent / 100.0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE castle SET taxPercent = ? WHERE id = ?");
      ) {
         statement.setInt(1, taxPercent);
         statement.setInt(2, this.getId());
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, var34.getMessage(), (Throwable)var34);
      }
   }

   public void spawnDoor() {
      this.spawnDoor(false);
   }

   public void spawnDoor(boolean isDoorWeak) {
      for(DoorInstance door : this._doors) {
         if (door.isDead()) {
            door.doRevive();
            if (isDoorWeak) {
               door.setCurrentHp(door.getMaxHp() / 2.0);
            } else {
               door.setCurrentHp(door.getMaxHp());
            }
         }

         if (door.getOpen()) {
            door.closeMe();
         }
      }

      this.loadDoorUpgrade();
   }

   public void upgradeDoor(int doorId, int hp, int pDef, int mDef) {
      DoorInstance door = this.getDoor(doorId);
      if (door != null) {
         door.setCurrentHp(door.getMaxHp() + (double)hp);
         this.saveDoorUpgrade(doorId, hp, pDef, mDef);
      }
   }

   private void load() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("SELECT * FROM castle WHERE id = ?");
         PreparedStatement ps2 = con.prepareStatement("SELECT clan_id FROM clan_data WHERE hasCastle = ?");
      ) {
         ps1.setInt(1, this.getId());

         try (ResultSet rs = ps1.executeQuery()) {
            while(rs.next()) {
               this._name = rs.getString("name");
               this._siegeDate = Calendar.getInstance();
               this._siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
               this._siegeTimeRegistrationEndDate = Calendar.getInstance();
               this._siegeTimeRegistrationEndDate.setTimeInMillis(rs.getLong("regTimeEnd"));
               this._isTimeRegistrationOver = rs.getBoolean("regTimeOver");
               this._taxPercent = rs.getInt("taxPercent");
               this._treasury = rs.getLong("treasury");
               this._ticketBuyCount = rs.getInt("ticketBuyCount");
            }
         }

         this._taxRate = (double)this._taxPercent / 100.0;
         ps2.setInt(1, this.getId());

         try (ResultSet rs = ps2.executeQuery()) {
            while(rs.next()) {
               this._ownerId = rs.getInt("clan_id");
            }
         }

         if (this.getOwnerId() > 0) {
            Clan clan = ClanHolder.getInstance().getClan(this.getOwnerId());
            ThreadPoolManager.getInstance().schedule(new CastleUpdater(clan, 1), 3600000L);
         }
      } catch (Exception var130) {
         _log.log(Level.WARNING, "Exception: loadCastleData(): " + var130.getMessage(), (Throwable)var130);
      }
   }

   private void loadFunctions() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_functions WHERE castle_id = ?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
               this._function
                  .put(
                     rs.getInt("type"),
                     new Castle.CastleFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true)
                  );
            }
         }
      } catch (Exception var59) {
         _log.log(Level.SEVERE, "Exception: Castle.loadFunctions(): " + var59.getMessage(), (Throwable)var59);
      }
   }

   public void removeFunction(int functionType) {
      this._function.remove(functionType);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM castle_functions WHERE castle_id=? AND type=?");
      ) {
         statement.setInt(1, this.getId());
         statement.setInt(2, functionType);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Exception: Castle.removeFunctions(int functionType): " + var34.getMessage(), (Throwable)var34);
      }
   }

   public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate, boolean addNew) {
      if (player == null) {
         return false;
      } else if (lease > 0 && !player.destroyItemByItemId("Consume", 57, (long)lease, null, true)) {
         return false;
      } else {
         if (addNew) {
            this._function.put(type, new Castle.CastleFunction(type, lvl, lease, 0, rate, 0L, false));
         } else if (lvl == 0 && lease == 0) {
            this.removeFunction(type);
         } else {
            int diffLease = lease - this._function.get(type).getLease();
            if (diffLease > 0) {
               this._function.remove(type);
               this._function.put(type, new Castle.CastleFunction(type, lvl, lease, 0, rate, -1L, false));
            } else {
               this._function.get(type).setLease(lease);
               this._function.get(type).setLvl(lvl);
               this._function.get(type).dbSave();
            }
         }

         return true;
      }
   }

   public void activateInstance() {
      this.loadDoor();
   }

   private void loadDoor() {
      for(DoorInstance door : DoorParser.getInstance().getDoors()) {
         if (door.getCastle() != null && door.getCastle().getId() == this.getId()) {
            this._doors.add(door);
         }
      }
   }

   private void loadDoorUpgrade() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         StringBuilder doorIds = new StringBuilder(100);

         for(DoorInstance door : this.getDoors()) {
            doorIds.append(door.getDoorId()).append(',');
         }

         doorIds.deleteCharAt(doorIds.length() - 1);
         PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (" + doorIds.toString() + ")");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
         }

         rs.close();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Exception: loadCastleDoorUpgrade(): " + var16.getMessage(), (Throwable)var16);
      }
   }

   private void removeDoorUpgrade() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         StringBuilder doorIds = new StringBuilder(100);

         for(DoorInstance door : this.getDoors()) {
            doorIds.append(door.getDoorId()).append(',');
         }

         doorIds.deleteCharAt(doorIds.length() - 1);
         PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (" + doorIds.toString() + ")");
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Exception: removeDoorUpgrade(): " + var16.getMessage(), (Throwable)var16);
      }
   }

   private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
         statement.setInt(1, doorId);
         statement.setInt(2, hp);
         statement.setInt(3, pDef);
         statement.setInt(4, mDef);
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.WARNING, "Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + var18.getMessage(), (Throwable)var18);
      }
   }

   private void updateOwnerInDB(Clan clan) {
      if (clan != null) {
         this._ownerId = clan.getId();
      } else {
         this._ownerId = 0;
         this.resetManor();
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
         statement.setInt(1, this.getId());
         statement.execute();
         statement.close();
         statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
         statement.setInt(1, this.getId());
         statement.setInt(2, this.getOwnerId());
         statement.execute();
         statement.close();
         if (clan != null) {
            clan.setCastleId(this.getId());
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
            clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
            ThreadPoolManager.getInstance().schedule(new CastleUpdater(clan, 1), 3600000L);
         }
      } catch (Exception var15) {
         _log.log(Level.WARNING, "Exception: updateOwnerInDB(Clan clan): " + var15.getMessage(), (Throwable)var15);
      }
   }

   @Override
   public final int getId() {
      return this._castleId;
   }

   public final DoorInstance getDoor(int doorId) {
      if (doorId <= 0) {
         return null;
      } else {
         for(DoorInstance door : this.getDoors()) {
            if (door.getDoorId() == doorId) {
               return door;
            }
         }

         return null;
      }
   }

   public final List<DoorInstance> getDoors() {
      return this._doors;
   }

   public final String getName() {
      return this._name;
   }

   public final int getOwnerId() {
      return this._ownerId;
   }

   public final Clan getOwner() {
      return this._ownerId != 0 ? ClanHolder.getInstance().getClan(this._ownerId) : null;
   }

   public final Siege getSiege() {
      if (this._siege == null) {
         this._siege = new Siege(new Castle[]{this});
      }

      return this._siege;
   }

   public final Calendar getSiegeDate() {
      return this._siegeDate;
   }

   public boolean getIsTimeRegistrationOver() {
      return this._isTimeRegistrationOver;
   }

   public void setIsTimeRegistrationOver(boolean val) {
      this._isTimeRegistrationOver = val;
   }

   public Calendar getTimeRegistrationOverDate() {
      if (this._siegeTimeRegistrationEndDate == null) {
         this._siegeTimeRegistrationEndDate = Calendar.getInstance();
      }

      return this._siegeTimeRegistrationEndDate;
   }

   public final int getTaxPercent() {
      return this._taxPercent;
   }

   public final double getTaxRate() {
      return this._taxRate;
   }

   public final long getTreasury() {
      return this._treasury;
   }

   public List<SeedTemplate> getSeedProduction(int period) {
      return period == 0 ? this._production : this._productionNext;
   }

   public List<CropProcureTemplate> getCropProcure(int period) {
      return period == 0 ? this._procure : this._procureNext;
   }

   public void setSeedProduction(List<SeedTemplate> seed, int period) {
      if (period == 0) {
         this._production = seed;
      } else {
         this._productionNext = seed;
      }
   }

   public void setCropProcure(List<CropProcureTemplate> crop, int period) {
      if (period == 0) {
         this._procure = crop;
      } else {
         this._procureNext = crop;
      }
   }

   public SeedTemplate getSeed(int seedId, int period) {
      for(SeedTemplate seed : this.getSeedProduction(period)) {
         if (seed.getId() == seedId) {
            return seed;
         }
      }

      return null;
   }

   public CropProcureTemplate getCrop(int cropId, int period) {
      for(CropProcureTemplate crop : this.getCropProcure(period)) {
         if (crop.getId() == cropId) {
            return crop;
         }
      }

      return null;
   }

   public long getManorCost(int period) {
      List<CropProcureTemplate> procure;
      List<SeedTemplate> production;
      if (period == 0) {
         procure = this._procure;
         production = this._production;
      } else {
         procure = this._procureNext;
         production = this._productionNext;
      }

      long total = 0L;
      if (production != null) {
         for(SeedTemplate seed : production) {
            total += ManorParser.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
         }
      }

      if (procure != null) {
         for(CropProcureTemplate crop : procure) {
            total += crop.getPrice() * crop.getStartAmount();
         }
      }

      return total;
   }

   public void saveSeedData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
      ) {
         ps1.setInt(1, this.getId());
         ps1.execute();
         if (this._production != null) {
            int count = 0;
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO castle_manor_production VALUES ");
            String[] values = new String[this._production.size()];

            for(SeedTemplate s : this._production) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + s.getId()
                  + ","
                  + s.getCanProduce()
                  + ","
                  + s.getStartProduce()
                  + ","
                  + s.getPrice()
                  + ","
                  + 0
                  + ")";
            }

            if (values.length > 0) {
               query.append(values[0]);

               for(int i = 1; i < values.length; ++i) {
                  query.append(',');
                  query.append(values[i]);
               }

               try (PreparedStatement ps2 = con.prepareStatement(query.toString())) {
                  ps2.execute();
               }
            }
         }

         if (this._productionNext != null) {
            int count = 0;
            String query = "INSERT INTO castle_manor_production VALUES ";
            String[] values = new String[this._productionNext.size()];

            for(SeedTemplate s : this._productionNext) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + s.getId()
                  + ","
                  + s.getCanProduce()
                  + ","
                  + s.getStartProduce()
                  + ","
                  + s.getPrice()
                  + ","
                  + 1
                  + ")";
            }

            if (values.length > 0) {
               query = query + values[0];

               for(int i = 1; i < values.length; ++i) {
                  query = query + "," + values[i];
               }

               try (PreparedStatement ps3 = con.prepareStatement(query)) {
                  ps3.execute();
               }
            }
         }
      } catch (Exception var89) {
         _log.info("Error adding seed production data for castle " + this.getName() + ": " + var89.getMessage());
      }
   }

   public void saveSeedData(int period) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
         statement.setInt(1, this.getId());
         statement.setInt(2, period);
         statement.execute();
         statement.close();
         List<SeedTemplate> prod = null;
         prod = this.getSeedProduction(period);
         if (prod != null) {
            int count = 0;
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO castle_manor_production VALUES ");
            String[] values = new String[prod.size()];

            for(SeedTemplate s : prod) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + s.getId()
                  + ","
                  + s.getCanProduce()
                  + ","
                  + s.getStartProduce()
                  + ","
                  + s.getPrice()
                  + ","
                  + period
                  + ")";
            }

            if (values.length > 0) {
               query.append(values[0]);

               for(int i = 1; i < values.length; ++i) {
                  query.append(',').append(values[i]);
               }

               statement = con.prepareStatement(query.toString());
               statement.execute();
               statement.close();
            }
         }
      } catch (Exception var21) {
         _log.info("Error adding seed production data for castle " + this.getName() + ": " + var21.getMessage());
      }
   }

   public void saveCropData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
      ) {
         ps1.setInt(1, this.getId());
         ps1.execute();
         if (!this._procure.isEmpty()) {
            int count = 0;
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO castle_manor_procure VALUES ");
            String[] values = new String[this._procure.size()];

            for(CropProcureTemplate cp : this._procure) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + cp.getId()
                  + ","
                  + cp.getAmount()
                  + ","
                  + cp.getStartAmount()
                  + ","
                  + cp.getPrice()
                  + ","
                  + cp.getReward()
                  + ","
                  + 0
                  + ")";
            }

            if (values.length > 0) {
               query.append(values[0]);

               for(int i = 1; i < values.length; ++i) {
                  query.append(',');
                  query.append(values[i]);
               }

               try (PreparedStatement ps2 = con.prepareStatement(query.toString())) {
                  ps2.execute();
               }
            }
         }

         if (!this._procureNext.isEmpty()) {
            int count = 0;
            String query = "INSERT INTO castle_manor_procure VALUES ";
            String[] values = new String[this._procureNext.size()];

            for(CropProcureTemplate cp : this._procureNext) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + cp.getId()
                  + ","
                  + cp.getAmount()
                  + ","
                  + cp.getStartAmount()
                  + ","
                  + cp.getPrice()
                  + ","
                  + cp.getReward()
                  + ","
                  + 1
                  + ")";
            }

            if (values.length > 0) {
               query = query + values[0];

               for(int i = 1; i < values.length; ++i) {
                  query = query + "," + values[i];
               }

               try (PreparedStatement ps3 = con.prepareStatement(query)) {
                  ps3.execute();
               }
            }
         }
      } catch (Exception var89) {
         _log.info("Error adding crop data for castle " + this.getName() + ": " + var89.getMessage());
      }
   }

   public void saveCropData(int period) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
         statement.setInt(1, this.getId());
         statement.setInt(2, period);
         statement.execute();
         statement.close();
         List<CropProcureTemplate> proc = null;
         proc = this.getCropProcure(period);
         if (proc != null && proc.size() > 0) {
            int count = 0;
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO castle_manor_procure VALUES ");
            String[] values = new String[proc.size()];

            for(CropProcureTemplate cp : proc) {
               values[count++] = "("
                  + this.getId()
                  + ","
                  + cp.getId()
                  + ","
                  + cp.getAmount()
                  + ","
                  + cp.getStartAmount()
                  + ","
                  + cp.getPrice()
                  + ","
                  + cp.getReward()
                  + ","
                  + period
                  + ")";
            }

            if (values.length > 0) {
               query.append(values[0]);

               for(int i = 1; i < values.length; ++i) {
                  query.append(',');
                  query.append(values[i]);
               }

               statement = con.prepareStatement(query.toString());
               statement.execute();
               statement.close();
            }
         }
      } catch (Exception var21) {
         _log.info("Error adding crop data for castle " + this.getName() + ": " + var21.getMessage());
      }
   }

   public void updateCrop(int cropId, long amount, int period) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
         statement.setLong(1, amount);
         statement.setInt(2, cropId);
         statement.setInt(3, this.getId());
         statement.setInt(4, period);
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.info("Error adding crop data for castle " + this.getName() + ": " + var18.getMessage());
      }
   }

   public void updateSeed(int seedId, long amount, int period) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
         statement.setLong(1, amount);
         statement.setInt(2, seedId);
         statement.setInt(3, this.getId());
         statement.setInt(4, period);
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.info("Error adding seed production data for castle " + this.getName() + ": " + var18.getMessage());
      }
   }

   public boolean isNextPeriodApproved() {
      return this._isNextPeriodApproved;
   }

   public void setNextPeriodApproved(boolean val) {
      this._isNextPeriodApproved = val;
   }

   public void updateClansReputation() {
      if (this._formerOwner != null) {
         if (this._formerOwner != ClanHolder.getInstance().getClan(this.getOwnerId())) {
            int maxreward = Math.max(0, this._formerOwner.getReputationScore());
            this._formerOwner.takeReputationScore(Config.LOOSE_CASTLE_POINTS, true);
            Clan owner = ClanHolder.getInstance().getClan(this.getOwnerId());
            if (owner != null) {
               owner.addReputationScore(Math.min(Config.TAKE_CASTLE_POINTS, maxreward), true);
            }
         } else {
            this._formerOwner.addReputationScore(Config.CASTLE_DEFENDED_POINTS, true);
         }
      } else {
         Clan owner = ClanHolder.getInstance().getClan(this.getOwnerId());
         if (owner != null) {
            owner.addReputationScore(Config.TAKE_CASTLE_POINTS, true);
         }
      }
   }

   public List<Skill> getResidentialSkills() {
      return this._residentialSkills;
   }

   public void giveResidentialSkills(Player player) {
      for(Skill sk : this._residentialSkills) {
         player.addSkill(sk, false);
      }

      if (this.getTerritory() != null && this.getTerritory().getOwnedWardIds().contains(this.getId() + 80)) {
         for(int wardId : this.getTerritory().getOwnedWardIds()) {
            for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(wardId)) {
               Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
               if (sk != null) {
                  player.addSkill(sk, false);
               } else {
                  _log.warning("Trying to add a null skill for Territory Ward Id: " + wardId + ", skill Id: " + s.getId() + " level: " + s.getLvl() + "!");
               }
            }
         }
      }
   }

   public void removeResidentialSkills(Player player) {
      for(Skill sk : this._residentialSkills) {
         player.removeSkill(sk, false, true);
      }

      if (this.getTerritory() != null) {
         for(int wardId : this.getTerritory().getOwnedWardIds()) {
            for(SkillLearn s : SkillTreesParser.getInstance().getAvailableResidentialSkills(wardId)) {
               Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
               if (sk != null) {
                  player.removeSkill(sk, false, true);
               } else {
                  _log.warning("Trying to remove a null skill for Territory Ward Id: " + wardId + ", skill Id: " + s.getId() + " level: " + s.getLvl() + "!");
               }
            }
         }
      }
   }

   public void registerArtefact(ArtefactInstance artefact) {
      this._artefacts.add(artefact);
   }

   public List<ArtefactInstance> getArtefacts() {
      return this._artefacts;
   }

   public void resetManor() {
      this.setCropProcure(new ArrayList<>(), 0);
      this.setCropProcure(new ArrayList<>(), 1);
      this.setSeedProduction(new ArrayList<>(), 0);
      this.setSeedProduction(new ArrayList<>(), 1);
      if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
         this.saveCropData();
         this.saveSeedData();
      }
   }

   public int getTicketBuyCount() {
      return this._ticketBuyCount;
   }

   public void setTicketBuyCount(int count) {
      this._ticketBuyCount = count;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE castle SET ticketBuyCount = ? WHERE id = ?");
      ) {
         statement.setInt(1, this._ticketBuyCount);
         statement.setInt(2, this._castleId);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, var34.getMessage(), (Throwable)var34);
      }
   }

   public TerritoryWarManager.Territory getTerritory() {
      return TerritoryWarManager.getInstance().getTerritory(this.getId());
   }

   @Override
   public String toString() {
      return this._name + "(" + this._castleId + ")";
   }

   public class CastleFunction {
      private final int _type;
      private int _lvl;
      protected int _fee;
      protected int _tempFee;
      private final long _rate;
      private long _endDate;
      protected boolean _inDebt;
      public boolean _cwh;

      public CastleFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh) {
         this._type = type;
         this._lvl = lvl;
         this._fee = lease;
         this._tempFee = tempLease;
         this._rate = rate;
         this._endDate = time;
         this.initializeTask(cwh);
      }

      public int getType() {
         return this._type;
      }

      public int getLvl() {
         return this._lvl;
      }

      public int getLease() {
         return this._fee;
      }

      public long getRate() {
         return this._rate;
      }

      public long getEndTime() {
         return this._endDate;
      }

      public void setLvl(int lvl) {
         this._lvl = lvl;
      }

      public void setLease(int lease) {
         this._fee = lease;
      }

      public void setEndTime(long time) {
         this._endDate = time;
      }

      private void initializeTask(boolean cwh) {
         if (Castle.this.getOwnerId() > 0) {
            long currentTime = System.currentTimeMillis();
            if (this._endDate > currentTime) {
               ThreadPoolManager.getInstance().schedule(new Castle.CastleFunction.FunctionTask(cwh), this._endDate - currentTime);
            } else {
               ThreadPoolManager.getInstance().schedule(new Castle.CastleFunction.FunctionTask(cwh), 0L);
            }
         }
      }

      public void dbSave() {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
               "REPLACE INTO castle_functions (castle_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"
            );
         ) {
            statement.setInt(1, Castle.this.getId());
            statement.setInt(2, this.getType());
            statement.setInt(3, this.getLvl());
            statement.setInt(4, this.getLease());
            statement.setLong(5, this.getRate());
            statement.setLong(6, this.getEndTime());
            statement.execute();
         } catch (Exception var33) {
            Castle._log
               .log(
                  Level.SEVERE,
                  "Exception: Castle.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + var33.getMessage(),
                  (Throwable)var33
               );
         }
      }

      private class FunctionTask implements Runnable {
         public FunctionTask(boolean cwh) {
            CastleFunction.this._cwh = cwh;
         }

         @Override
         public void run() {
            try {
               if (Castle.this.getOwnerId() <= 0) {
                  return;
               }

               if (ClanHolder.getInstance().getClan(Castle.this.getOwnerId()).getWarehouse().getAdena() < (long)CastleFunction.this._fee
                  && CastleFunction.this._cwh) {
                  Castle.this.removeFunction(CastleFunction.this.getType());
               } else {
                  int fee = CastleFunction.this._fee;
                  if (CastleFunction.this.getEndTime() == -1L) {
                     fee = CastleFunction.this._tempFee;
                  }

                  CastleFunction.this.setEndTime(System.currentTimeMillis() + CastleFunction.this.getRate());
                  CastleFunction.this.dbSave();
                  if (CastleFunction.this._cwh) {
                     ClanHolder.getInstance()
                        .getClan(Castle.this.getOwnerId())
                        .getWarehouse()
                        .destroyItemByItemId("CS_function_fee", 57, (long)fee, null, null);
                  }

                  ThreadPoolManager.getInstance().schedule(CastleFunction.this.new FunctionTask(true), CastleFunction.this.getRate());
               }
            } catch (Exception var2) {
               Castle._log.log(Level.SEVERE, "", (Throwable)var2);
            }
         }
      }
   }
}
