package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CastleChamberlainInstance;
import l2e.gameserver.model.actor.instance.ManorManagerInstance;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.BuyListSeed;
import l2e.gameserver.network.serverpackets.ExShowCropInfo;
import l2e.gameserver.network.serverpackets.ExShowCropSetting;
import l2e.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import l2e.gameserver.network.serverpackets.ExShowProcureCropDetail;
import l2e.gameserver.network.serverpackets.ExShowSeedInfo;
import l2e.gameserver.network.serverpackets.ExShowSeedSetting;
import l2e.gameserver.network.serverpackets.ExShowSellCropList;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ManorManager implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"manor_menu_select"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      Npc manager = activeChar.getLastFolkNPC();
      boolean isCastle = manager instanceof CastleChamberlainInstance;
      if (!(manager instanceof ManorManagerInstance) && !isCastle) {
         return false;
      } else if (!activeChar.isInsideRadius(manager, 150, true, false)) {
         return false;
      } else {
         try {
            Castle castle = manager.getCastle();
            if (isCastle) {
               if (activeChar.getClan() == null || castle.getOwnerId() != activeChar.getClanId() || (activeChar.getClanPrivileges() & 131072) != 131072) {
                  manager.showChatWindow(activeChar, "data/html/chamberlain/chamberlain-noprivs.htm");
                  return false;
               }

               if (castle.getSiege().getIsInProgress()) {
                  manager.showChatWindow(activeChar, "data/html/chamberlain/chamberlain-busy.htm");
                  return false;
               }
            }

            if (CastleManorManager.getInstance().isUnderMaintenance()) {
               activeChar.sendActionFailed();
               activeChar.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
               return true;
            } else {
               StringTokenizer st = new StringTokenizer(command, "&");
               int ask = Integer.parseInt(st.nextToken().split("=")[1]);
               int state = Integer.parseInt(st.nextToken().split("=")[1]);
               int time = Integer.parseInt(st.nextToken().split("=")[1]);
               int castleId;
               if (state < 0) {
                  castleId = castle.getId();
               } else {
                  castleId = state;
               }

               switch(ask) {
                  case 1:
                     if (!isCastle) {
                        if (castleId != castle.getId()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
                           sm.addString(manager.getCastle().getName());
                           activeChar.sendPacket(sm);
                        } else {
                           activeChar.sendPacket(new BuyListSeed(activeChar.getAdena(), castleId, castle.getSeedProduction(0)));
                        }
                     }
                     break;
                  case 2:
                     if (!isCastle) {
                        activeChar.sendPacket(new ExShowSellCropList(activeChar, castleId, castle.getCropProcure(0)));
                     }
                     break;
                  case 3:
                     if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()) {
                        activeChar.sendPacket(new ExShowSeedInfo(castleId, null));
                     } else {
                        activeChar.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
                     }
                     break;
                  case 4:
                     if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()) {
                        activeChar.sendPacket(new ExShowCropInfo(castleId, null));
                     } else {
                        activeChar.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
                     }
                     break;
                  case 5:
                     activeChar.sendPacket(new ExShowManorDefaultInfo());
                     break;
                  case 6:
                     if (!isCastle) {
                        ((MerchantInstance)manager).showBuyWindow(activeChar, 300000 + manager.getId());
                     }
                     break;
                  case 7:
                     if (isCastle) {
                        if (castle.isNextPeriodApproved()) {
                           activeChar.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        } else {
                           activeChar.sendPacket(new ExShowSeedSetting(castle.getId()));
                        }
                     }
                     break;
                  case 8:
                     if (isCastle) {
                        if (castle.isNextPeriodApproved()) {
                           activeChar.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        } else {
                           activeChar.sendPacket(new ExShowCropSetting(castle.getId()));
                        }
                     }
                     break;
                  case 9:
                     if (!isCastle) {
                        activeChar.sendPacket(new ExShowProcureCropDetail(state));
                     }
                     break;
                  default:
                     return false;
               }

               return true;
            }
         } catch (Exception var13) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var13);
            return false;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
