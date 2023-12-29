package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import org.w3c.dom.NamedNodeMap;

public class PremiumCodeReward extends AbstractCodeReward {
   private final int _premiumId;
   private final String _icon;

   public PremiumCodeReward(NamedNodeMap attr) {
      this._premiumId = Integer.parseInt(attr.getNamedItem("id").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "";
   }

   @Override
   public void giveReward(Player player) {
      PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(this._premiumId);
      if (template != null) {
         if (!player.hasPremiumBonus()) {
            this.getGivePremium(template, player);
         } else if (Config.PREMIUMSERVICE_DOUBLE) {
            player.sendConfirmDlg(
               new PremiumCodeReward.PremiumAnswerListener(player, template),
               60000,
               new ServerMessage("PromoCode.WANT_CHANGE_PREMIUM", player.getLang()).toString()
            );
         }
      }
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   private void getGivePremium(PremiumTemplate template, Player player) {
      long time = !template.isOnlineType() ? System.currentTimeMillis() + template.getTime() * 1000L : 0L;
      if (template.isPersonal()) {
         CharacterPremiumDAO.getInstance().updatePersonal(player, this._premiumId, time);
      } else {
         CharacterPremiumDAO.getInstance().update(player, this._premiumId, time);
      }

      if (player.isInParty()) {
         player.getParty().recalculatePartyData();
      }
   }

   public int getPremiumId() {
      return this._premiumId;
   }

   private class PremiumAnswerListener implements OnAnswerListener {
      private final Player _player;
      private final PremiumTemplate _template;

      protected PremiumAnswerListener(Player player, PremiumTemplate template) {
         this._player = player;
         this._template = template;
      }

      @Override
      public void sayYes() {
         PremiumCodeReward.this.getGivePremium(this._template, this._player);
      }

      @Override
      public void sayNo() {
      }
   }
}
