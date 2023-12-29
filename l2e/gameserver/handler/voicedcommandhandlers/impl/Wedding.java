package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.listener.EngageAnswerListener;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Wedding implements IVoicedCommandHandler {
   static final Logger _log = Logger.getLogger(Wedding.class.getName());
   private static final String[] _voicedCommands = new String[]{"divorce", "engage", "gotolove"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (activeChar == null || !Config.ALLOW_WEDDING) {
         return false;
      } else if (command.startsWith("engage")) {
         return this.engage(activeChar);
      } else if (command.startsWith("divorce")) {
         return this.divorce(activeChar);
      } else {
         return command.startsWith("gotolove") ? this.goToLove(activeChar) : false;
      }
   }

   public boolean divorce(Player activeChar) {
      if (activeChar.getPartnerId() == 0) {
         return false;
      } else {
         int _partnerId = activeChar.getPartnerId();
         int _coupleId = activeChar.getCoupleId();
         long AdenaAmount = 0L;
         if (activeChar.isMarried()) {
            activeChar.sendMessage("You are now divorced.");
            AdenaAmount = activeChar.getAdena() / 100L * (long)Config.WEDDING_DIVORCE_COSTS;
            activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
         } else {
            activeChar.sendMessage("You have broken up as a couple.");
         }

         Player partner = World.getInstance().getPlayer(_partnerId);
         if (partner != null) {
            partner.setPartnerId(0);
            if (partner.isMarried()) {
               partner.sendMessage("Your spouse has decided to divorce you.");
            } else {
               partner.sendMessage("Your fiance has decided to break the engagement with you.");
            }

            if (AdenaAmount > 0L) {
               partner.addAdena("WEDDING", AdenaAmount, null, false);
            }
         }

         CoupleManager.getInstance().deleteCouple(_coupleId);
         return true;
      }
   }

   private boolean engage(Player activeChar) {
      if (activeChar.getTarget() == null) {
         activeChar.sendMessage("You have no one targeted.");
         return false;
      } else if (!activeChar.getTarget().isPlayer()) {
         activeChar.sendMessage("You can only ask another player to engage you.");
         return false;
      } else if (activeChar.getPartnerId() != 0) {
         activeChar.sendMessage("You are already engaged.");
         if (Config.WEDDING_PUNISH_INFIDELITY) {
            activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
            int skillLevel = 1;
            if (activeChar.getLevel() > 40) {
               skillLevel = 2;
            }

            int skillId;
            if (activeChar.isMageClass()) {
               skillId = 4362;
            } else {
               skillId = 4361;
            }

            Skill skill = SkillsParser.getInstance().getInfo(skillId, skillLevel);
            if (activeChar.getFirstEffect(skill) == null) {
               skill.getEffects(activeChar, activeChar, false);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
               sm.addSkillName(skill);
               activeChar.sendPacket(sm);
            }
         }

         return false;
      } else {
         Player ptarget = (Player)activeChar.getTarget();
         if (ptarget.getObjectId() == activeChar.getObjectId()) {
            activeChar.sendMessage("Is there something wrong with you, are you trying to go out with youself?");
            return false;
         } else if (ptarget.isMarried()) {
            activeChar.sendMessage("Player already married.");
            return false;
         } else if (ptarget.isEngageRequest()) {
            activeChar.sendMessage("Player already asked by someone else.");
            return false;
         } else if (ptarget.getPartnerId() != 0) {
            activeChar.sendMessage("Player already engaged with someone else.");
            return false;
         } else if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex()) {
            activeChar.sendMessage("Gay marriage is not allowed on this server!");
            return false;
         } else {
            boolean FoundOnFriendList = false;

            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=?");
               statement.setInt(1, ptarget.getObjectId());
               ResultSet rset = statement.executeQuery();

               while(rset.next()) {
                  int objectId = rset.getInt("friendId");
                  if (objectId == activeChar.getObjectId()) {
                     FoundOnFriendList = true;
                  }
               }

               statement.close();
            } catch (Exception var19) {
               _log.warning("could not read friend data:" + var19);
            }

            if (!FoundOnFriendList) {
               activeChar.sendMessage(
                  "The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage."
               );
               return false;
            } else {
               ptarget.setEngageRequest(true, activeChar.getObjectId());
               ptarget.sendConfirmDlg(
                  new EngageAnswerListener(ptarget), 0, activeChar.getName() + " is asking to engage you. Do you want to start a new relationship?"
               );
               return true;
            }
         }
      }
   }

   public boolean goToLove(Player activeChar) {
      if (!activeChar.isMarried()) {
         activeChar.sendMessage("You're not married.");
         return false;
      } else if (activeChar.getPartnerId() == 0) {
         activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
         _log.severe("Married but couldn't find parter for " + activeChar.getName());
         return false;
      } else if (EpicBossManager.getInstance().getZone(activeChar) != null) {
         activeChar.sendMessage("You are inside a Boss Zone.");
         return false;
      } else if (activeChar.isCombatFlagEquipped()) {
         activeChar.sendMessage("While you are holding a Combat Flag or Territory Ward you can't go to your love!");
         return false;
      } else if (activeChar.isCursedWeaponEquipped()) {
         activeChar.sendMessage("While you are holding a Cursed Weapon you can't go to your love!");
         return false;
      } else if (EpicBossManager.getInstance().getZone(activeChar) != null) {
         activeChar.sendMessage("You are inside a Boss Zone.");
         return false;
      } else if (activeChar.isJailed()) {
         activeChar.sendMessage("You are in Jail!");
         return false;
      } else if (activeChar.isInOlympiadMode()) {
         activeChar.sendMessage("You are in the Olympiad now.");
         return false;
      } else if (activeChar.isInDuel()) {
         activeChar.sendMessage("You are in a duel!");
         return false;
      } else if (activeChar.inObserverMode()) {
         activeChar.sendMessage("You are in the observation.");
         return false;
      } else if (SiegeManager.getInstance().getSiege(activeChar) != null && SiegeManager.getInstance().getSiege(activeChar).getIsInProgress()) {
         activeChar.sendMessage("You are in a siege, you cannot go to your partner.");
         return false;
      } else if (activeChar.isFestivalParticipant()) {
         activeChar.sendMessage("You are in a festival.");
         return false;
      } else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift()) {
         activeChar.sendMessage("You are in the dimensional rift.");
         return false;
      } else {
         for(AbstractFightEvent e : activeChar.getFightEvents()) {
            if (e != null && !e.canUseEscape(activeChar)) {
               activeChar.sendActionFailed();
               return false;
            }
         }

         if (!AerialCleftEvent.getInstance().onEscapeUse(activeChar.getObjectId())) {
            activeChar.sendActionFailed();
            return false;
         } else if (activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            activeChar.sendMessage("You are in area which blocks summoning.");
            return false;
         } else {
            Player partner = World.getInstance().getPlayer(activeChar.getPartnerId());
            if (partner == null || !partner.isOnline()) {
               activeChar.sendMessage("Your partner is not online.");
               return false;
            } else if (activeChar.getReflectionId() != partner.getReflectionId()) {
               activeChar.sendMessage("Your partner is in another World!");
               return false;
            } else if (partner.isJailed()) {
               activeChar.sendMessage("Your partner is in Jail.");
               return false;
            } else if (partner.isCursedWeaponEquipped()) {
               activeChar.sendMessage("Your partner is holding a Cursed Weapon and you can't go to your love!");
               return false;
            } else if (EpicBossManager.getInstance().getZone(partner) != null) {
               activeChar.sendMessage("Your partner is inside a Boss Zone.");
               return false;
            } else if (partner.isInOlympiadMode()) {
               activeChar.sendMessage("Your partner is in the Olympiad now.");
               return false;
            } else if (partner.isInDuel()) {
               activeChar.sendMessage("Your partner is in a duel.");
               return false;
            } else if (partner.isFestivalParticipant()) {
               activeChar.sendMessage("Your partner is in a festival.");
               return false;
            } else if (partner.isInParty() && partner.getParty().isInDimensionalRift()) {
               activeChar.sendMessage("Your partner is in dimensional rift.");
               return false;
            } else if (partner.inObserverMode()) {
               activeChar.sendMessage("Your partner is in the observation.");
               return false;
            } else if (SiegeManager.getInstance().getSiege(partner) != null && SiegeManager.getInstance().getSiege(partner).getIsInProgress()) {
               activeChar.sendMessage("Your partner is in a siege, you cannot go to your partner.");
               return false;
            } else {
               if (partner.isIn7sDungeon() && !activeChar.isIn7sDungeon()) {
                  int playerCabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
                  boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
                  int compWinner = SevenSigns.getInstance().getCabalHighestScore();
                  if (isSealValidationPeriod) {
                     if (playerCabal != compWinner) {
                        activeChar.sendMessage("Your Partner is in a Seven Signs Dungeon and you are not in the winner Cabal!");
                        return false;
                     }
                  } else if (playerCabal == 0) {
                     activeChar.sendMessage("Your Partner is in a Seven Signs Dungeon and you are not registered!");
                     return false;
                  }
               }

               for(AbstractFightEvent e : partner.getFightEvents()) {
                  if (e != null && !e.canUseEscape(partner)) {
                     activeChar.sendActionFailed();
                     return false;
                  }
               }

               if (!AerialCleftEvent.getInstance().onEscapeUse(partner.getObjectId())) {
                  activeChar.sendActionFailed();
                  return false;
               } else if (partner.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
                  activeChar.sendMessage("Your partner is in area which blocks summoning.");
                  return false;
               } else {
                  int teleportTimer = Config.WEDDING_TELEPORT_DURATION * 1000;
                  activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your partner.");
                  activeChar.getInventory().reduceAdena("Wedding", (long)Config.WEDDING_TELEPORT_PRICE, activeChar, null);
                  activeChar.getAI().setIntention(CtrlIntention.IDLE);
                  activeChar.setTarget(activeChar);
                  activeChar.disableAllSkills();
                  MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
                  Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
                  SetupGauge sg = new SetupGauge(activeChar, 0, teleportTimer);
                  activeChar.sendPacket(sg);
                  Wedding.EscapeFinalizer ef = new Wedding.EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
                  activeChar.setSkillCast(ThreadPoolManager.getInstance().schedule(ef, (long)teleportTimer));
                  activeChar.forceIsCasting(System.currentTimeMillis() + (long)(teleportTimer / 2));
                  return true;
               }
            }
         }
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }

   static class EscapeFinalizer implements Runnable {
      private final Player _activeChar;
      private final int _partnerx;
      private final int _partnery;
      private final int _partnerz;
      private final boolean _to7sDungeon;

      EscapeFinalizer(Player activeChar, int x, int y, int z, boolean to7sDungeon) {
         this._activeChar = activeChar;
         this._partnerx = x;
         this._partnery = y;
         this._partnerz = z;
         this._to7sDungeon = to7sDungeon;
      }

      @Override
      public void run() {
         if (!this._activeChar.isDead()) {
            if (SiegeManager.getInstance().getSiege(this._partnerx, this._partnery, this._partnerz) != null
               && SiegeManager.getInstance().getSiege(this._partnerx, this._partnery, this._partnerz).getIsInProgress()) {
               this._activeChar.sendMessage("Your partner is in siege, you can't go to your partner.");
            } else {
               this._activeChar.setIsIn7sDungeon(this._to7sDungeon);
               this._activeChar.enableAllSkills();
               this._activeChar.setIsCastingNow(false);

               try {
                  this._activeChar.teleToLocation(this._partnerx, this._partnery, this._partnerz, true);
               } catch (Exception var2) {
                  Wedding._log.log(Level.SEVERE, "", (Throwable)var2);
               }
            }
         }
      }
   }
}
