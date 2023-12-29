package l2e.gameserver.model.skills.l2skills;

import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class SkillSiegeFlag extends Skill {
   private final boolean _isAdvanced;
   private final boolean _isOutpost;

   public SkillSiegeFlag(StatsSet set) {
      super(set);
      this._isAdvanced = set.getBool("isAdvanced", false);
      this._isOutpost = set.getBool("isOutpost", false);
   }

   @Override
   public void useSkill(Creature activeChar, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         Player player = activeChar.getActingPlayer();
         if (player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId()) {
            if (checkIfOkToPlaceFlag(player, true, this._isOutpost)) {
               if (TerritoryWarManager.getInstance().isTWInProgress()) {
                  try {
                     SiegeFlagInstance flag = new SiegeFlagInstance(
                        player,
                        IdFactory.getInstance().getNextId(),
                        NpcsParser.getInstance().getTemplate(this._isOutpost ? '軮' : '裶'),
                        this._isAdvanced,
                        this._isOutpost
                     );
                     flag.setTitle(player.getClan().getName());
                     flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
                     flag.setHeading(player.getHeading());
                     flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
                     if (this._isOutpost) {
                        TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
                     } else {
                        TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
                     }
                  } catch (Exception var8) {
                     player.sendMessage("Error placing flag: " + var8);
                     _log.log(Level.WARNING, "Error placing flag: " + var8.getMessage(), (Throwable)var8);
                  }
               } else {
                  try {
                     SiegeFlagInstance flag = new SiegeFlagInstance(
                        player, IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(35062), this._isAdvanced, false
                     );
                     flag.setTitle(player.getClan().getName());
                     flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
                     flag.setHeading(player.getHeading());
                     flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
                     Castle castle = CastleManager.getInstance().getCastle(activeChar);
                     Fort fort = FortManager.getInstance().getFort(activeChar);
                     SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
                     if (castle != null) {
                        castle.getSiege().getFlag(player.getClan()).add(flag);
                     } else if (fort != null) {
                        fort.getSiege().getFlag(player.getClan()).add(flag);
                     } else {
                        hall.getSiege().getFlag(player.getClan()).add(flag);
                     }
                  } catch (Exception var9) {
                     player.sendMessage("Error placing flag:" + var9);
                     _log.log(Level.WARNING, "Error placing flag: " + var9.getMessage(), (Throwable)var9);
                  }
               }
            }
         }
      }
   }

   public static boolean checkIfOkToPlaceFlag(Creature activeChar, boolean isCheckOnly, boolean isOutPost) {
      if (TerritoryWarManager.getInstance().isTWInProgress()) {
         return checkIfOkToPlaceHQ(activeChar, isCheckOnly, isOutPost);
      } else if (isOutPost) {
         return false;
      } else {
         Castle castle = CastleManager.getInstance().getCastle(activeChar);
         Fort fort = FortManager.getInstance().getFort(activeChar);
         SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
         if (castle == null && fort == null && hall == null) {
            return false;
         } else if (castle != null) {
            return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
         } else {
            return fort != null ? checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly) : checkIfOkToPlaceFlag(activeChar, hall, isCheckOnly);
         }
      }
   }

   public static boolean checkIfOkToPlaceFlag(Creature activeChar, Castle castle, boolean isCheckOnly) {
      if (!activeChar.isPlayer()) {
         return false;
      } else {
         String text = "";
         Player player = activeChar.getActingPlayer();
         if (castle == null || castle.getId() <= 0) {
            text = "You must be on castle ground to place a flag.";
         } else if (!castle.getSiege().getIsInProgress()) {
            text = "You can only place a flag during a siege.";
         } else if (castle.getSiege().getAttackerClan(player.getClan()) == null) {
            text = "You must be an attacker to place a flag.";
         } else if (!player.isClanLeader()) {
            text = "You must be a clan leader to place a flag.";
         } else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount()) {
            text = "You have already placed the maximum number of flags possible.";
         } else {
            if (player.isInsideZone(ZoneId.HQ)) {
               return true;
            }

            player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
         }

         if (!isCheckOnly) {
            player.sendMessage(text);
         }

         return false;
      }
   }

   public static boolean checkIfOkToPlaceFlag(Creature activeChar, Fort fort, boolean isCheckOnly) {
      if (!activeChar.isPlayer()) {
         return false;
      } else {
         String text = "";
         Player player = activeChar.getActingPlayer();
         if (fort == null || fort.getId() <= 0) {
            text = "You must be on fort ground to place a flag.";
         } else if (!fort.getSiege().getIsInProgress()) {
            text = "You can only place a flag during a siege.";
         } else if (fort.getSiege().getAttackerClan(player.getClan()) == null) {
            text = "You must be an attacker to place a flag.";
         } else if (!player.isClanLeader()) {
            text = "You must be a clan leader to place a flag.";
         } else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount()) {
            text = "You have already placed the maximum number of flags possible.";
         } else {
            if (player.isInsideZone(ZoneId.HQ)) {
               return true;
            }

            player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
         }

         if (!isCheckOnly) {
            player.sendMessage(text);
         }

         return false;
      }
   }

   public static boolean checkIfOkToPlaceFlag(Creature activeChar, SiegableHall hall, boolean isCheckOnly) {
      if (!activeChar.isPlayer()) {
         return false;
      } else {
         String text = "";
         Player player = activeChar.getActingPlayer();
         int hallId = hall.getId();
         if (hallId <= 0) {
            text = "You must be on Siegable clan hall ground to place a flag.";
         } else if (!hall.isInSiege()) {
            text = "You can only place a flag during a siege.";
         } else if (player.getClan() == null || !player.isClanLeader()) {
            text = "You must be a clan leader to place a flag.";
         } else if (!hall.isRegistered(player.getClan())) {
            text = "You must be an attacker to place a flag.";
         } else if (hall.getSiege().getAttackerClan(player.getClan()).getNumFlags() > Config.CHS_MAX_FLAGS_PER_CLAN) {
            text = "You have already placed the maximum number of flags possible.";
         } else if (!player.isInsideZone(ZoneId.HQ)) {
            player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
         } else {
            if (hall.getSiege().canPlantFlag()) {
               return true;
            }

            text = "You cannot place a flag on this siege.";
         }

         if (!isCheckOnly) {
            player.sendMessage(text);
         }

         return false;
      }
   }

   public static boolean checkIfOkToPlaceHQ(Creature activeChar, boolean isCheckOnly, boolean isOutPost) {
      Castle castle = CastleManager.getInstance().getCastle(activeChar);
      Fort fort = FortManager.getInstance().getFort(activeChar);
      if (castle == null && fort == null) {
         return false;
      } else {
         String text = "";
         Player player = activeChar.getActingPlayer();
         if ((fort == null || fort.getId() != 0) && (castle == null || castle.getId() != 0)) {
            if ((fort == null || fort.getZone().isActive()) && (castle == null || castle.getZone().isActive())) {
               if (!player.isClanLeader()) {
                  text = "You must be a clan leader to construct an outpost or flag.";
               } else if (TerritoryWarManager.getInstance().getHQForClan(player.getClan()) != null && isOutPost) {
                  player.sendPacket(SystemMessageId.NOT_ANOTHER_HEADQUARTERS);
               } else if (TerritoryWarManager.getInstance().getFlagForClan(player.getClan()) != null && !isOutPost) {
                  player.sendPacket(SystemMessageId.A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED);
               } else {
                  if (player.isInsideZone(ZoneId.HQ)) {
                     return true;
                  }

                  player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
               }
            } else {
               text = "You can only construct an outpost or flag on siege field.";
            }
         } else {
            text = "You must be on fort or castle ground to construct an outpost or flag.";
         }

         if (!isCheckOnly) {
            player.sendMessage(text);
         }

         return false;
      }
   }
}
