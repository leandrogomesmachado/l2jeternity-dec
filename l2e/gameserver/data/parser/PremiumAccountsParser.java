package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.service.premium.PremiumGift;
import l2e.gameserver.model.service.premium.PremiumPrice;
import l2e.gameserver.model.service.premium.PremiumRates;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import org.w3c.dom.Node;

public final class PremiumAccountsParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(PremiumAccountsParser.class.getName());
   private final List<PremiumTemplate> _templates = new ArrayList<>();

   private PremiumAccountsParser() {
      this._templates.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDirectory("data/stats/premiumAccounts", false);
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._templates.size() + " premium account templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("premium".equalsIgnoreCase(d.getNodeName())) {
                  int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                  String nameEn = d.getAttributes().getNamedItem("nameEn").getNodeValue();
                  String nameRu = d.getAttributes().getNamedItem("nameRu").getNodeValue();
                  String icon = d.getAttributes().getNamedItem("icon").getNodeValue();
                  boolean isOnlineType = Boolean.parseBoolean(d.getAttributes().getNamedItem("onlineType").getNodeValue());
                  boolean isPersonal = Boolean.parseBoolean(d.getAttributes().getNamedItem("isPersonal").getNodeValue());
                  PremiumTemplate premium = new PremiumTemplate(id, nameEn, nameRu, icon, isOnlineType, isPersonal);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("rate".equalsIgnoreCase(cd.getNodeName())) {
                        String type = cd.getAttributes().getNamedItem("type").getNodeValue();
                        PremiumRates key = PremiumRates.find(type);
                        if (key != null) {
                           premium.setRate(key, cd.getAttributes().getNamedItem("value").getNodeValue());
                        }
                     } else if ("gifts".equalsIgnoreCase(cd.getNodeName())) {
                        for(Node gt = cd.getFirstChild(); gt != null; gt = gt.getNextSibling()) {
                           if ("gift".equalsIgnoreCase(gt.getNodeName())) {
                              int itemId = Integer.parseInt(gt.getAttributes().getNamedItem("id").getNodeValue());
                              long count = Long.parseLong(gt.getAttributes().getNamedItem("count").getNodeValue());
                              boolean removable = Boolean.parseBoolean(gt.getAttributes().getNamedItem("removable").getNodeValue());
                              PremiumGift gift = new PremiumGift(itemId, count, removable);
                              premium.addGift(gift);
                           }
                        }
                     } else if ("time".equalsIgnoreCase(cd.getNodeName())) {
                        int days = Integer.parseInt(cd.getAttributes().getNamedItem("days").getNodeValue());
                        int hours = Integer.parseInt(cd.getAttributes().getNamedItem("hours").getNodeValue());
                        int minutes = Integer.parseInt(cd.getAttributes().getNamedItem("minutes").getNodeValue());
                        int total = (int)((TimeUtils.addDay(days) + TimeUtils.addHours(hours) + TimeUtils.addMinutes(minutes)) / 1000L);
                        premium.setTime((long)total);
                     } else if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        for(Node gt = cd.getFirstChild(); gt != null; gt = gt.getNextSibling()) {
                           if ("item".equalsIgnoreCase(gt.getNodeName())) {
                              int itemId = Integer.parseInt(gt.getAttributes().getNamedItem("id").getNodeValue());
                              long count = Long.parseLong(gt.getAttributes().getNamedItem("count").getNodeValue());
                              PremiumPrice price = new PremiumPrice(itemId, count);
                              premium.addPrice(price);
                           }
                        }
                     }
                  }

                  this._templates.add(premium);
               }
            }
         }
      }
   }

   public List<PremiumTemplate> getTemplates() {
      return this._templates;
   }

   public PremiumTemplate getPremiumTemplate(int id) {
      for(PremiumTemplate template : this._templates) {
         if (template.getId() == id) {
            return template;
         }
      }

      return null;
   }

   public static PremiumAccountsParser getInstance() {
      return PremiumAccountsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final PremiumAccountsParser _instance = new PremiumAccountsParser();
   }
}
