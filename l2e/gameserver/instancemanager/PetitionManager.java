package l2e.gameserver.instancemanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.Petition;
import l2e.gameserver.model.petition.PetitionState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class PetitionManager {
   protected static final Logger _log = Logger.getLogger(PetitionManager.class.getName());
   private final Map<Integer, Petition> _pendingPetitions = new HashMap<>();
   private final Map<Integer, Petition> _completedPetitions = new HashMap<>();

   protected PetitionManager() {
   }

   public void clearCompletedPetitions() {
      int numPetitions = this.getPendingPetitionCount();
      this.getCompletedPetitions().clear();
      _log.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
   }

   public void clearPendingPetitions() {
      int numPetitions = this.getPendingPetitionCount();
      this.getPendingPetitions().clear();
      _log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
   }

   public boolean acceptPetition(Player respondingAdmin, int petitionId) {
      if (!this.isValidPetition(petitionId)) {
         return false;
      } else {
         Petition currPetition = this.getPendingPetitions().get(petitionId);
         if (currPetition.getResponder() != null) {
            return false;
         } else {
            currPetition.setResponder(respondingAdmin);
            currPetition.setState(PetitionState.IN_PROCESS);
            currPetition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
            sm.addNumber(currPetition.getId());
            currPetition.sendResponderPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.STARTING_PETITION_WITH_C1);
            sm.addString(currPetition.getPetitioner().getName());
            currPetition.sendResponderPacket(sm);
            currPetition.getPetitioner().setLastPetitionGmName(currPetition.getResponder().getName());
            return true;
         }
      }
   }

   public boolean cancelActivePetition(Player player) {
      for(Petition currPetition : this.getPendingPetitions().values()) {
         if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
            return currPetition.endPetitionConsultation(PetitionState.PETITIONER_CANCEL);
         }

         if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
            return currPetition.endPetitionConsultation(PetitionState.RESPONDER_CANCEL);
         }
      }

      return false;
   }

   public void checkPetitionMessages(Player petitioner) {
      if (petitioner != null) {
         for(Petition currPetition : this.getPendingPetitions().values()) {
            if (currPetition != null && currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()) {
               for(CreatureSay logMessage : currPetition.getLogMessages()) {
                  petitioner.sendPacket(logMessage);
               }

               return;
            }
         }
      }
   }

   public boolean endActivePetition(Player player) {
      if (!player.isGM()) {
         return false;
      } else {
         for(Petition currPetition : this.getPendingPetitions().values()) {
            if (currPetition != null && currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
               return currPetition.endPetitionConsultation(PetitionState.COMPLETED);
            }
         }

         return false;
      }
   }

   public Map<Integer, Petition> getCompletedPetitions() {
      return this._completedPetitions;
   }

   public Map<Integer, Petition> getPendingPetitions() {
      return this._pendingPetitions;
   }

   public int getPendingPetitionCount() {
      return this.getPendingPetitions().size();
   }

   public int getPlayerTotalPetitionCount(Player player) {
      if (player == null) {
         return 0;
      } else {
         int petitionCount = 0;

         for(Petition currPetition : this.getPendingPetitions().values()) {
            if (currPetition != null && currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
               ++petitionCount;
            }
         }

         for(Petition currPetition : this.getCompletedPetitions().values()) {
            if (currPetition != null && currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
               ++petitionCount;
            }
         }

         return petitionCount;
      }
   }

   public boolean isPetitionInProcess() {
      for(Petition currPetition : this.getPendingPetitions().values()) {
         if (currPetition != null && currPetition.getState() == PetitionState.IN_PROCESS) {
            return true;
         }
      }

      return false;
   }

   public boolean isPetitionInProcess(int petitionId) {
      if (!this.isValidPetition(petitionId)) {
         return false;
      } else {
         Petition currPetition = this.getPendingPetitions().get(petitionId);
         return currPetition.getState() == PetitionState.IN_PROCESS;
      }
   }

   public boolean isPlayerInConsultation(Player player) {
      if (player != null) {
         for(Petition currPetition : this.getPendingPetitions().values()) {
            if (currPetition != null
               && currPetition.getState() == PetitionState.IN_PROCESS
               && (
                  currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()
                     || currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()
               )) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isPetitioningAllowed() {
      return Config.PETITIONING_ALLOWED;
   }

   public boolean isPlayerPetitionPending(Player petitioner) {
      if (petitioner != null) {
         for(Petition currPetition : this.getPendingPetitions().values()) {
            if (currPetition != null && currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isValidPetition(int petitionId) {
      return this.getPendingPetitions().containsKey(petitionId);
   }

   public boolean rejectPetition(Player respondingAdmin, int petitionId) {
      if (!this.isValidPetition(petitionId)) {
         return false;
      } else {
         Petition currPetition = this.getPendingPetitions().get(petitionId);
         if (currPetition.getResponder() != null) {
            return false;
         } else {
            currPetition.setResponder(respondingAdmin);
            return currPetition.endPetitionConsultation(PetitionState.RESPONDER_REJECT);
         }
      }
   }

   public boolean sendActivePetitionMessage(Player player, String messageText) {
      for(Petition currPetition : this.getPendingPetitions().values()) {
         if (currPetition != null) {
            if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
               CreatureSay cs = new CreatureSay(player.getObjectId(), 6, player.getName(), messageText);
               currPetition.addLogMessage(cs);
               currPetition.sendResponderPacket(cs);
               currPetition.sendPetitionerPacket(cs);
               return true;
            }

            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
               CreatureSay cs = new CreatureSay(player.getObjectId(), 7, player.getName(), messageText);
               currPetition.addLogMessage(cs);
               currPetition.sendResponderPacket(cs);
               currPetition.sendPetitionerPacket(cs);
               return true;
            }
         }
      }

      return false;
   }

   public void sendPendingPetitionList(Player activeChar) {
      StringBuilder htmlContent = StringUtil.startAppend(
         600 + this.getPendingPetitionCount() * 300,
         "<html><body><center><table width=270><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>Petition Menu</center></td><td width=45><button value=\"Back\" action=\"bypass -h admin_admin7\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"270\"><tr><td><table width=\"270\"><tr><td><button value=\"Reset\" action=\"bypass -h admin_reset_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td align=right><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br></td></tr>"
      );
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      if (this.getPendingPetitionCount() == 0) {
         htmlContent.append("<tr><td>There are no currently pending petitions.</td></tr>");
      } else {
         htmlContent.append("<tr><td><font color=\"LEVEL\">Current Petitions:</font><br></td></tr>");
      }

      boolean color = true;
      int petcount = 0;

      for(Petition currPetition : this.getPendingPetitions().values()) {
         if (currPetition != null) {
            StringUtil.append(
               htmlContent,
               "<tr><td width=\"270\"><table width=\"270\" cellpadding=\"2\" bgcolor=",
               color ? "131210" : "444444",
               "><tr><td width=\"130\">",
               dateFormat.format(new Date(currPetition.getSubmitTime()))
            );
            StringUtil.append(
               htmlContent,
               "</td><td width=\"140\" align=right><font color=\"",
               currPetition.getPetitioner().isOnline() ? "00FF00" : "999999",
               "\">",
               currPetition.getPetitioner().getName(),
               "</font></td></tr>"
            );
            StringUtil.append(htmlContent, "<tr><td width=\"130\">");
            if (currPetition.getState() != PetitionState.IN_PROCESS) {
               StringUtil.append(
                  htmlContent,
                  "<table width=\"130\" cellpadding=\"2\"><tr><td><button value=\"View\" action=\"bypass -h admin_view_petition ",
                  String.valueOf(currPetition.getId()),
                  "\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Reject\" action=\"bypass -h admin_reject_petition ",
                  String.valueOf(currPetition.getId()),
                  "\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>"
               );
            } else {
               htmlContent.append(
                  "<font color=\""
                     + (currPetition.getResponder().isOnline() ? "00FF00" : "999999")
                     + "\">"
                     + currPetition.getResponder().getName()
                     + "</font>"
               );
            }

            StringUtil.append(
               htmlContent,
               "</td>",
               currPetition.getTypeAsString(),
               "<td width=\"140\" align=right>",
               currPetition.getTypeAsString(),
               "</td></tr></table></td></tr>"
            );
            color = !color;
            if (++petcount > 10) {
               htmlContent.append("<tr><td><font color=\"LEVEL\">There is more pending petition...</font><br></td></tr>");
               break;
            }
         }
      }

      htmlContent.append("</table></center></body></html>");
      NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
      htmlMsg.setHtml(activeChar, htmlContent.toString());
      activeChar.sendPacket(htmlMsg);
   }

   public int submitPetition(Player petitioner, String petitionText, int petitionType) {
      Petition newPetition = new Petition(petitioner, petitionText, petitionType);
      int newPetitionId = newPetition.getId();
      this.getPendingPetitions().put(newPetitionId, newPetition);
      String msgContent = petitioner.getName() + " has submitted a new petition.";
      AdminParser.getInstance().broadcastToGMs(new CreatureSay(petitioner.getObjectId(), 17, "Petition System", msgContent));
      return newPetitionId;
   }

   public void viewPetition(Player activeChar, int petitionId) {
      if (activeChar.isGM()) {
         if (this.isValidPetition(petitionId)) {
            Petition currPetition = this.getPendingPetitions().get(petitionId);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/admin/petition.htm");
            html.replace("%petition%", String.valueOf(currPetition.getId()));
            html.replace("%time%", dateFormat.format(new Date(currPetition.getSubmitTime())));
            html.replace("%type%", currPetition.getTypeAsString());
            html.replace("%petitioner%", currPetition.getPetitioner().getName());
            html.replace("%online%", currPetition.getPetitioner().isOnline() ? "00FF00" : "999999");
            html.replace("%text%", currPetition.getContent());
            activeChar.sendPacket(html);
         }
      }
   }

   public static final PetitionManager getInstance() {
      return PetitionManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final PetitionManager _instance = new PetitionManager();
   }
}
