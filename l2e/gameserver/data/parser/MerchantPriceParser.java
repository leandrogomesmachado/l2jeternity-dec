package l2e.gameserver.data.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.gameserver.Config;
import l2e.gameserver.InstanceListManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.entity.Castle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class MerchantPriceParser implements InstanceListManager {
   private static Logger _log = Logger.getLogger(MerchantPriceParser.class.getName());
   private final Map<Integer, MerchantPriceParser.MerchantPrice> _mpcs = new HashMap<>();
   private MerchantPriceParser.MerchantPrice _defaultMpc;

   public static MerchantPriceParser getInstance() {
      return MerchantPriceParser.SingletonHolder._instance;
   }

   protected MerchantPriceParser() {
   }

   public MerchantPriceParser.MerchantPrice getMerchantPrice(MerchantInstance npc) {
      for(MerchantPriceParser.MerchantPrice mpc : this._mpcs.values()) {
         if (npc.getWorldRegion() != null && npc.containsZone(mpc.getZoneId())) {
            return mpc;
         }
      }

      return this._defaultMpc;
   }

   public MerchantPriceParser.MerchantPrice getMerchantPrice(int id) {
      return this._mpcs.get(id);
   }

   public void loadXML() throws SAXException, IOException, ParserConfigurationException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      File file = new File(Config.DATAPACK_ROOT + "/data/stats/regions/merchantPrice.xml");
      if (file.exists()) {
         Document doc = factory.newDocumentBuilder().parse(file);
         Node n = doc.getDocumentElement();
         Node dpcNode = n.getAttributes().getNamedItem("defaultPrice");
         if (dpcNode == null) {
            throw new IllegalStateException("merchantPrice must define an 'defaultPriceConfig'");
         }

         int defaultPriceConfigId = Integer.parseInt(dpcNode.getNodeValue());

         for(Node var9 = n.getFirstChild(); var9 != null; var9 = var9.getNextSibling()) {
            MerchantPriceParser.MerchantPrice mpc = this.parseMerchantPrice(var9);
            if (mpc != null) {
               this._mpcs.put(mpc.getId(), mpc);
            }
         }

         MerchantPriceParser.MerchantPrice defaultMpc = this.getMerchantPrice(defaultPriceConfigId);
         if (defaultMpc == null) {
            throw new IllegalStateException("'defaultPriceConfig' points to an non-loaded priceConfig");
         }

         this._defaultMpc = defaultMpc;
      }
   }

   private MerchantPriceParser.MerchantPrice parseMerchantPrice(Node n) {
      if (n.getNodeName().equals("price")) {
         int castleId = -1;
         int zoneId = -1;
         Node node = n.getAttributes().getNamedItem("id");
         if (node == null) {
            throw new IllegalStateException("Must define the price 'id'");
         } else {
            int id = Integer.parseInt(node.getNodeValue());
            node = n.getAttributes().getNamedItem("name");
            if (node == null) {
               throw new IllegalStateException("Must define the price 'name'");
            } else {
               String name = node.getNodeValue();
               node = n.getAttributes().getNamedItem("baseTax");
               if (node == null) {
                  throw new IllegalStateException("Must define the price 'baseTax'");
               } else {
                  int baseTax = Integer.parseInt(node.getNodeValue());
                  node = n.getAttributes().getNamedItem("castleId");
                  if (node != null) {
                     castleId = Integer.parseInt(node.getNodeValue());
                  }

                  node = n.getAttributes().getNamedItem("zoneId");
                  if (node != null) {
                     zoneId = Integer.parseInt(node.getNodeValue());
                  }

                  return new MerchantPriceParser.MerchantPrice(id, name, baseTax, castleId, zoneId);
               }
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public void loadInstances() {
      try {
         this.loadXML();
         _log.info(this.getClass().getSimpleName() + ": Loaded " + this._mpcs.size() + " merchant price configs.");
      } catch (Exception var2) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed loading MerchantPriceParser. Reason: " + var2.getMessage(), (Throwable)var2);
      }
   }

   @Override
   public void updateReferences() {
      for(MerchantPriceParser.MerchantPrice mpc : this._mpcs.values()) {
         mpc.updateReferences();
      }
   }

   @Override
   public void activateInstances() {
   }

   public static final class MerchantPrice {
      private final int _id;
      private final String _name;
      private final int _baseTax;
      private final int _castleId;
      private Castle _castle;
      private final int _zoneId;

      public MerchantPrice(int id, String name, int baseTax, int castleId, int zoneId) {
         this._id = id;
         this._name = name;
         this._baseTax = baseTax;
         this._castleId = castleId;
         this._zoneId = zoneId;
      }

      public int getId() {
         return this._id;
      }

      public String getName() {
         return this._name;
      }

      public int getBaseTax() {
         return this._baseTax;
      }

      public double getBaseTaxRate() {
         return (double)this._baseTax / 100.0;
      }

      public Castle getCastle() {
         return this._castle;
      }

      public int getZoneId() {
         return this._zoneId;
      }

      public boolean hasCastle() {
         return this.getCastle() != null;
      }

      public double getCastleTaxRate() {
         return this.hasCastle() ? this.getCastle().getTaxRate() : 0.0;
      }

      public int getTotalTax() {
         return this.hasCastle() ? this.getCastle().getTaxPercent() + this.getBaseTax() : this.getBaseTax();
      }

      public double getTotalTaxRate() {
         return (double)this.getTotalTax() / 100.0;
      }

      public void updateReferences() {
         if (this._castleId > 0) {
            this._castle = CastleManager.getInstance().getCastleById(this._castleId);
         }
      }
   }

   private static class SingletonHolder {
      protected static final MerchantPriceParser _instance = new MerchantPriceParser();
   }
}
