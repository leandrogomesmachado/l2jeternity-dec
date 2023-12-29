package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.SquadTrainer;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillDone;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortSupportCaptainInstance extends MerchantInstance implements SquadTrainer {
   private static final int[] TalismanIds = new int[]{
      9914,
      9915,
      9917,
      9918,
      9919,
      9920,
      9921,
      9922,
      9923,
      9924,
      9926,
      9927,
      9928,
      9930,
      9931,
      9932,
      9933,
      9934,
      9935,
      9936,
      9937,
      9938,
      9939,
      9940,
      9941,
      9942,
      9943,
      9944,
      9945,
      9946,
      9947,
      9948,
      9949,
      9950,
      9951,
      9952,
      9953,
      9954,
      9955,
      9956,
      9957,
      9958,
      9959,
      9960,
      9961,
      9962,
      9963,
      9964,
      9965,
      9966,
      10141,
      10142,
      10158
   };

   public FortSupportCaptainInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.FortSupportCaptainInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         String par = "";
         if (st.countTokens() >= 1) {
            par = st.nextToken();
         }

         if (actualCommand.equalsIgnoreCase("Chat")) {
            int val = 0;

            try {
               val = Integer.parseInt(par);
            } catch (IndexOutOfBoundsException var11) {
            } catch (NumberFormatException var12) {
            }

            this.showMessageWindow(player, val);
         } else if (actualCommand.equalsIgnoreCase("ExchangeKE")) {
            int itemId = TalismanIds[Rnd.get(TalismanIds.length)];
            if (player.exchangeItemsById("FortSupportUnitExchangeKE", this, 9912, 10L, itemId, 1L, true)) {
               this.showChatWindow(player, "data/html/fortress/supportunit-talisman.htm");
            } else {
               this.showChatWindow(player, "data/html/fortress/supportunit-noepau.htm");
            }
         } else if (command.equals("subskills")) {
            if (player.isClanLeader()) {
               List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableSubPledgeSkills(player.getClan());
               AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.SUBPLEDGE);
               int count = 0;

               for(SkillLearn s : skills) {
                  if (SkillsParser.getInstance().getInfo(s.getId(), s.getLvl()) != null) {
                     asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getLevelUpSp(), 0);
                     ++count;
                  }
               }

               if (count == 0) {
                  player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
                  player.sendPacket(AcquireSkillDone.STATIC);
               } else {
                  player.sendPacket(asl);
               }
            } else {
               this.showChatWindow(player, "data/html/fortress/supportunit-nosquad.htm");
            }
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }

   @Override
   public void showChatWindow(Player player) {
      if (player.getClan() != null && this.getFort().getOwnerClan() != null && player.getClan() == this.getFort().getOwnerClan()) {
         this.showMessageWindow(player, 0);
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), "data/html/fortress/supportunit-noclan.htm");
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         player.sendPacket(html);
      }
   }

   private void showMessageWindow(Player player, int val) {
      String filename;
      if (val == 0) {
         filename = "data/html/fortress/supportunit.htm";
      } else {
         filename = "data/html/fortress/supportunit-" + val + ".htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      if (this.getFort().getOwnerClan() != null) {
         html.replace("%clanname%", this.getFort().getOwnerClan().getName());
      } else {
         html.replace("%clanname%", "NPC");
      }

      player.sendPacket(html);
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }

   @Override
   public void showSubUnitSkillList(Player player) {
      this.onBypassFeedback(player, "subskills");
   }
}
