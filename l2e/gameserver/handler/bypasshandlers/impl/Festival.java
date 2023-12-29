package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.FestivalGuideInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Festival implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"festival", "festivaldesc"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof FestivalGuideInstance)) {
         return false;
      } else {
         FestivalGuideInstance npc = (FestivalGuideInstance)target;

         try {
            if (command.toLowerCase().startsWith(COMMANDS[1])) {
               int val = Integer.parseInt(command.substring(13));
               npc.showChatWindow(activeChar, val, null, true);
               return true;
            } else {
               int val = Integer.parseInt(command.substring(9, 10));
               switch(val) {
                  case 0:
                     if (!SevenSigns.getInstance().isSealValidationPeriod()) {
                        activeChar.sendMessage("Bonuses cannot be paid during the competition period.");
                        return true;
                     }

                     if (SevenSignsFestival.getInstance().distribAccumulatedBonus(activeChar) > 0) {
                        npc.showChatWindow(activeChar, 0, "a", false);
                     } else {
                        npc.showChatWindow(activeChar, 0, "b", false);
                     }
                     break;
                  case 1:
                     if (SevenSigns.getInstance().isSealValidationPeriod()) {
                        npc.showChatWindow(activeChar, 2, "a", false);
                        return true;
                     }

                     if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                        activeChar.sendMessage("You cannot sign up while a festival is in progress.");
                        return true;
                     }

                     if (!activeChar.isInParty()) {
                        npc.showChatWindow(activeChar, 2, "b", false);
                        return true;
                     }

                     Party party = activeChar.getParty();
                     if (!party.isLeader(activeChar)) {
                        npc.showChatWindow(activeChar, 2, "c", false);
                        return true;
                     }

                     if (party.getMemberCount() < Config.ALT_FESTIVAL_MIN_PLAYER) {
                        npc.showChatWindow(activeChar, 2, "b", false);
                        return true;
                     }

                     if (party.getLevel() > SevenSignsFestival.getMaxLevelForFestival(npc.getFestivalType())) {
                        npc.showChatWindow(activeChar, 2, "d", false);
                        return true;
                     }

                     if (activeChar.isFestivalParticipant()) {
                        SevenSignsFestival.getInstance().setParticipants(npc.getFestivalOracle(), npc.getFestivalType(), party);
                        npc.showChatWindow(activeChar, 2, "f", false);
                        return true;
                     }

                     npc.showChatWindow(activeChar, 1, null, false);
                     break;
                  case 2:
                     int stoneType = Integer.parseInt(command.substring(11));
                     int stoneCount = npc.getStoneCount(stoneType);
                     if (stoneCount <= 0) {
                        return false;
                     }

                     if (!activeChar.destroyItemByItemId("SevenSigns", stoneType, (long)stoneCount, npc, true)) {
                        return false;
                     }

                     SevenSignsFestival.getInstance().setParticipants(npc.getFestivalOracle(), npc.getFestivalType(), activeChar.getParty());
                     SevenSignsFestival.getInstance().addAccumulatedBonus(npc.getFestivalType(), stoneType, stoneCount);
                     npc.showChatWindow(activeChar, 2, "e", false);
                     break;
                  case 3:
                     if (SevenSigns.getInstance().isSealValidationPeriod()) {
                        npc.showChatWindow(activeChar, 3, "a", false);
                        return true;
                     }

                     if (SevenSignsFestival.getInstance().isFestivalInProgress()) {
                        activeChar.sendMessage("You cannot register a score while a festival is in progress.");
                        return true;
                     }

                     if (!activeChar.isInParty()) {
                        npc.showChatWindow(activeChar, 3, "b", false);
                        return true;
                     }

                     List<Integer> prevParticipants = SevenSignsFestival.getInstance().getPreviousParticipants(npc.getFestivalOracle(), npc.getFestivalType());
                     if (prevParticipants == null || prevParticipants.isEmpty() || !prevParticipants.contains(activeChar.getObjectId())) {
                        npc.showChatWindow(activeChar, 3, "b", false);
                        return true;
                     }

                     if (activeChar.getObjectId() != prevParticipants.get(0)) {
                        npc.showChatWindow(activeChar, 3, "b", false);
                        return true;
                     }

                     ItemInstance bloodOfferings = activeChar.getInventory().getItemByItemId(5901);
                     if (bloodOfferings == null) {
                        activeChar.sendMessage("You do not have any blood offerings to contribute.");
                        return true;
                     }

                     long offeringScore = bloodOfferings.getCount() * 5L;
                     if (!activeChar.destroyItem("SevenSigns", bloodOfferings, npc, false)) {
                        return true;
                     }

                     boolean isHighestScore = SevenSignsFestival.getInstance()
                        .setFinalScore(activeChar, npc.getFestivalOracle(), npc.getFestivalType(), offeringScore);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1);
                     sm.addItemNumber(offeringScore);
                     activeChar.sendPacket(sm);
                     if (isHighestScore) {
                        npc.showChatWindow(activeChar, 3, "c", false);
                     } else {
                        npc.showChatWindow(activeChar, 3, "d", false);
                     }
                     break;
                  case 4:
                     StringBuilder strBuffer = StringUtil.startAppend(500, "<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
                     StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(2, npc.getFestivalType());
                     StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(1, npc.getFestivalType());
                     StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(npc.getFestivalType());
                     int dawnScore = dawnData.getInteger("score");
                     int duskScore = duskData.getInteger("score");
                     int overallScore = 0;
                     if (overallData != null) {
                        overallScore = overallData.getInteger("score");
                     }

                     StringUtil.append(strBuffer, SevenSignsFestival.getFestivalName(npc.getFestivalType()), " festival.<br>");
                     if (dawnScore > 0) {
                        StringUtil.append(
                           strBuffer,
                           "Dawn: ",
                           this.calculateDate(dawnData.getString("date")),
                           ". Score ",
                           String.valueOf(dawnScore),
                           "<br>",
                           dawnData.getString("members"),
                           "<br>"
                        );
                     } else {
                        strBuffer.append("Dawn: No record exists. Score 0<br>");
                     }

                     if (duskScore > 0) {
                        StringUtil.append(
                           strBuffer,
                           "Dusk: ",
                           this.calculateDate(duskData.getString("date")),
                           ". Score ",
                           String.valueOf(duskScore),
                           "<br>",
                           duskData.getString("members"),
                           "<br>"
                        );
                     } else {
                        strBuffer.append("Dusk: No record exists. Score 0<br>");
                     }

                     if (overallScore > 0 && overallData != null) {
                        String cabalStr;
                        if (overallData.getString("cabal").equals("dawn")) {
                           cabalStr = "Children of Dawn";
                        } else {
                           cabalStr = "Children of Dusk";
                        }

                        StringUtil.append(
                           strBuffer,
                           "Consecutive top scores: ",
                           this.calculateDate(overallData.getString("date")),
                           ". Score ",
                           String.valueOf(overallScore),
                           "<br>Affilated side: ",
                           cabalStr,
                           "<br>",
                           overallData.getString("members"),
                           "<br>"
                        );
                     } else {
                        strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
                     }

                     StringUtil.append(strBuffer, "<a action=\"bypass -h npc_", String.valueOf(npc.getObjectId()), "_Chat 0\">Go back.</a></body></html>");
                     NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                     html.setHtml(activeChar, strBuffer.toString());
                     activeChar.sendPacket(html);
                     break;
                  case 5:
                  case 6:
                  case 7:
                  default:
                     npc.showChatWindow(activeChar, val, null, false);
                     break;
                  case 8:
                     if (!activeChar.isInParty()) {
                        return true;
                     }

                     if (!SevenSignsFestival.getInstance().isFestivalInProgress()) {
                        return true;
                     }

                     Party party = activeChar.getParty();
                     if (!party.isLeader(activeChar)) {
                        npc.showChatWindow(activeChar, 8, "a", false);
                        return true;
                     }

                     if (SevenSignsFestival.getInstance().increaseChallenge(npc.getFestivalOracle(), npc.getFestivalType())) {
                        npc.showChatWindow(activeChar, 8, "b", false);
                     } else {
                        npc.showChatWindow(activeChar, 8, "c", false);
                     }
                     break;
                  case 9:
                     if (!activeChar.isInParty()) {
                        return true;
                     }

                     Party party = activeChar.getParty();
                     if (party.isLeader(activeChar)) {
                        SevenSignsFestival.getInstance().updateParticipants(activeChar, null);
                     } else if (party.getMemberCount() > Config.ALT_FESTIVAL_MIN_PLAYER) {
                        party.removePartyMember(activeChar, Party.messageType.Expelled);
                     } else {
                        activeChar.sendMessage("Only the party leader can leave a festival when a party has minimum number of members.");
                     }
               }

               return true;
            }
         } catch (Exception var23) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var23);
            return false;
         }
      }
   }

   private final String calculateDate(String milliFromEpoch) {
      long numMillis = Long.valueOf(milliFromEpoch);
      Calendar calCalc = Calendar.getInstance();
      calCalc.setTimeInMillis(numMillis);
      return calCalc.get(1) + "/" + calCalc.get(2) + "/" + calCalc.get(5);
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
