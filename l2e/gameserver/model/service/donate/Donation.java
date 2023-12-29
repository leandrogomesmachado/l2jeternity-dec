package l2e.gameserver.model.service.donate;

import java.util.Iterator;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.ItemsParser;

public class Donation {
   private static final Logger _log = Logger.getLogger(Donation.class.getName());
   private final int _id;
   private final String _nameEn;
   private final String _nameRu;
   private final String _icon;
   private final int _group;
   private final boolean _havefound;
   private SimpleList _simple;
   private FoundList _found;
   private Enchant _enchant;
   private Attribution _attribution;

   public Donation(int id, String nameEn, String nameRu, String icon, int group, boolean havefound) {
      this._id = id;
      this._nameEn = nameEn;
      this._nameRu = nameRu;
      this._icon = icon;
      this._group = group;
      this._havefound = havefound;
   }

   public int getId() {
      return this._id;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   public String getIcon() {
      return this._icon;
   }

   public int getGroup() {
      return this._group;
   }

   public boolean haveFound() {
      return this._havefound;
   }

   public void addSimple(SimpleList list) {
      this._simple = list;
   }

   public SimpleList getSimple() {
      return this._simple;
   }

   public void addFound(FoundList list) {
      this._found = list;
   }

   public FoundList getFound() {
      return this._found;
   }

   public Enchant getEnchant() {
      return this._enchant;
   }

   public void setEnchant(Enchant enchant) {
      this._enchant = enchant;
   }

   public Attribution getAttribution() {
      return this._attribution;
   }

   public void setAttribution(Attribution att) {
      this._attribution = att;
   }

   public void print() {
      Iterator<DonateItem> i = this._simple.getList().iterator();
      _log.info("=========== Donate: " + this._nameEn + " (id: " + this._id + ") ===========");
      _log.info("=== Icon: " + this._icon);
      _log.info("=== Group: " + this._group);
      _log.info("=== Have found: " + this._havefound);
      _log.info("=== Simple items:");

      while(i.hasNext()) {
         DonateItem item = i.next();
         _log.info("====> Item:" + ItemsParser.getInstance().getTemplate(item.getId()).getNameEn() + " (id: " + item.getId() + ")");
         _log.info("====> Count: " + item.getCount());
         _log.info("====> Enchant: " + item.getEnchant());
      }

      if (this._havefound) {
         _log.info("=== Foundation items:");

         for(DonateItem item : this._found.getList()) {
            _log.info("====> Item:" + ItemsParser.getInstance().getTemplate(item.getId()).getNameEn() + " (id: " + item.getId() + ")");
            _log.info("====> Count: " + item.getCount());
            _log.info("====> Enchant: " + item.getEnchant());
         }
      }

      _log.info("=== Enchant: cost -> " + this._enchant.getCount() + " " + this._enchant.getId() + ", value -> " + this._enchant.getEnchant());
      _log.info(
         "=== Attribution: cost -> "
            + this._attribution.getCount()
            + " "
            + this._attribution.getId()
            + ", value -> "
            + this._attribution.getValue()
            + ", size (Element count) -> "
            + this._attribution.getSize()
      );
   }
}
