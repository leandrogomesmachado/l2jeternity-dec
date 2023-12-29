package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SetupGauge;

public class Unstuck implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{52};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      for(AbstractFightEvent e : activeChar.getFightEvents()) {
         if (e != null && !e.canUseEscape(activeChar)) {
            activeChar.sendActionFailed();
            return false;
         }
      }

      if (!AerialCleftEvent.getInstance().onEscapeUse(activeChar.getObjectId())) {
         activeChar.sendActionFailed();
         return false;
      } else if (activeChar.isJailed()) {
         activeChar.sendMessage("You cannot use this function while you are jailed.");
         return false;
      } else {
         int unstuckTimer = activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000;
         if (!activeChar.isCastingNow()
            && !activeChar.isMovementDisabled()
            && !activeChar.isMuted()
            && !activeChar.isAlikeDead()
            && !activeChar.isInOlympiadMode()
            && !activeChar.inObserverMode()
            && !activeChar.isCombatFlagEquipped()) {
            activeChar.forceIsCasting(System.currentTimeMillis() + (long)(unstuckTimer / 2));
            Skill escape = SkillsParser.getInstance().getInfo(2099, 1);
            Skill GM_escape = SkillsParser.getInstance().getInfo(2100, 1);
            if (activeChar.getAccessLevel().isGm()) {
               if (GM_escape != null) {
                  activeChar.doCast(GM_escape);
                  return true;
               }

               activeChar.sendMessage("You use Escape: 1 second.");
            } else {
               if (Config.UNSTUCK_INTERVAL == 300 && escape != null) {
                  activeChar.doCast(escape);
                  return true;
               }

               if (Config.UNSTUCK_INTERVAL > 100) {
                  activeChar.sendMessage("You use Escape: " + unstuckTimer / 60000 + " minutes.");
               } else {
                  activeChar.sendMessage("You use Escape: " + unstuckTimer / 1000 + " seconds.");
               }
            }

            activeChar.getAI().setIntention(CtrlIntention.IDLE);
            activeChar.setTarget(activeChar);
            activeChar.disableAllSkills();
            MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
            Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
            SetupGauge sg = new SetupGauge(activeChar, 0, unstuckTimer);
            activeChar.sendPacket(sg);
            activeChar.setSkillCast(ThreadPoolManager.getInstance().schedule(new Unstuck.EscapeFinalizer(activeChar), (long)unstuckTimer));
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }

   private static class EscapeFinalizer implements Runnable {
      private final Player _activeChar;

      protected EscapeFinalizer(Player activeChar) {
         this._activeChar = activeChar;
      }

      @Override
      public void run() {
         if (!this._activeChar.isDead()) {
            this._activeChar.setIsIn7sDungeon(false);
            this._activeChar.enableAllSkills();
            this._activeChar.setIsCastingNow(false);
            this._activeChar.setReflectionId(0);
            this._activeChar.setGeoIndex(0);
            this._activeChar.teleToLocation(TeleportWhereType.TOWN, true);
         }
      }
   }
}
