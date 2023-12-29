package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.service.donate.Attribution;
import l2e.gameserver.model.service.donate.DonateItem;
import l2e.gameserver.model.service.donate.Donation;
import l2e.gameserver.model.service.donate.Enchant;
import l2e.gameserver.model.service.donate.FoundList;
import l2e.gameserver.model.service.donate.SimpleList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DonationParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(ExchangeItemParser.class.getName());
   private final List<Donation> _donate = new ArrayList<>();

   protected DonationParser() {
      this._donate.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/donation.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._donate.size() + " donation templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("donation".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap donation = d.getAttributes();
                  int id = Integer.parseInt(donation.getNamedItem("id").getNodeValue());
                  String nameEn = donation.getNamedItem("nameEn").getNodeValue();
                  String nameRu = donation.getNamedItem("nameRu").getNodeValue();
                  String icon = donation.getNamedItem("icon").getNodeValue();
                  int group = Integer.parseInt(donation.getNamedItem("group").getNodeValue());
                  boolean found = Boolean.parseBoolean(donation.getNamedItem("found").getNodeValue());
                  Donation donate = new Donation(id, nameEn, nameRu, icon, group, found);
                  this.addDonate(donate);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     donation = cd.getAttributes();
                     if ("simples".equalsIgnoreCase(cd.getNodeName())) {
                        int s_id = Integer.parseInt(donation.getNamedItem("costId").getNodeValue());
                        long s_count = Long.parseLong(donation.getNamedItem("count").getNodeValue());
                        SimpleList s_list = new SimpleList(s_id, s_count, this.simple_parse(cd, donation));
                        donate.addSimple(s_list);
                     }

                     if ("foundations".equalsIgnoreCase(cd.getNodeName()) && found) {
                        int enchant = Integer.parseInt(donation.getNamedItem("costId").getNodeValue());
                        long attribution = Long.parseLong(donation.getNamedItem("count").getNodeValue());
                        FoundList a_count = new FoundList(enchant, attribution, this.donate_parse(cd, donation));
                        donate.addFound(a_count);
                     }

                     if ("enchant".equalsIgnoreCase(cd.getNodeName())) {
                        int attribution1 = Integer.parseInt(donation.getNamedItem("costId").getNodeValue());
                        long a_id = Long.parseLong(donation.getNamedItem("count").getNodeValue());
                        int e_value = Integer.parseInt(donation.getNamedItem("value").getNodeValue());
                        Enchant a_value = new Enchant(attribution1, a_id, e_value);
                        donate.setEnchant(a_value);
                     }

                     if ("attribution".equalsIgnoreCase(cd.getNodeName())) {
                        int a_id1 = Integer.parseInt(donation.getNamedItem("costId").getNodeValue());
                        long a_count1 = Long.parseLong(donation.getNamedItem("count").getNodeValue());
                        int a_value1 = Integer.parseInt(donation.getNamedItem("value").getNodeValue());
                        int size = Integer.parseInt(donation.getNamedItem("size").getNodeValue());
                        Attribution atr = new Attribution(a_id1, a_count1, a_value1, size);
                        donate.setAttribution(atr);
                     }
                  }
               }
            }
         }
      }
   }

   private List<DonateItem> simple_parse(Node d, NamedNodeMap attrs) {
      ArrayList<DonateItem> list = new ArrayList<>();

      for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("simple".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            long count = Long.parseLong(attrs.getNamedItem("count").getNodeValue());
            int enchant = Integer.parseInt(attrs.getNamedItem("enchant").getNodeValue());
            DonateItem donate = new DonateItem(id, count, enchant);
            list.add(donate);
         }
      }

      return list;
   }

   private List<DonateItem> donate_parse(Node d, NamedNodeMap attrs) {
      ArrayList<DonateItem> list = new ArrayList<>();

      for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("foundation".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            long count = Long.parseLong(attrs.getNamedItem("count").getNodeValue());
            int enchant = Integer.parseInt(attrs.getNamedItem("enchant").getNodeValue());
            DonateItem donate = new DonateItem(id, count, enchant);
            list.add(donate);
         }
      }

      return list;
   }

   public void addDonate(Donation donate) {
      this._donate.add(donate);
   }

   public List<Donation> getAllDonates() {
      return this._donate;
   }

   public Donation getDonate(int id) {
      for(Donation donate : this._donate) {
         if (donate.getId() == id) {
            return donate;
         }
      }

      return null;
   }

   public List<Donation> getGroup(int id) {
      ArrayList<Donation> group = new ArrayList<>();

      for(Donation donate : this._donate) {
         if (donate.getGroup() == id) {
            group.add(donate);
         }
      }

      return group;
   }

   public static DonationParser getInstance() {
      return DonationParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DonationParser _instance = new DonationParser();
   }
}
