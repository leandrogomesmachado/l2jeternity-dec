package l2e.gameserver.data.parser;

import java.util.Collection;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.model.petition.PetitionSection;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.w3c.dom.Node;

public class PetitionGroupParser extends DocumentParser {
   private final IntObjectMap<PetitionMainGroup> _petitionGroups = new HashIntObjectMap<>();

   protected PetitionGroupParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this._petitionGroups.clear();
      this.parseDatapackFile("data/stats/admin/petitions.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded: " + this._petitionGroups.size() + " petition groups.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node d = c.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("petition".equalsIgnoreCase(d.getNodeName())) {
                  PetitionMainGroup group = new PetitionMainGroup(Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue()));
                  this._petitionGroups.put(group.getId(), group);

                  for(Node n = d.getFirstChild(); n != null; n = n.getNextSibling()) {
                     if ("name".equalsIgnoreCase(n.getNodeName())) {
                        group.setName(n.getAttributes().getNamedItem("lang").getNodeValue(), n.getAttributes().getNamedItem("val").getNodeValue());
                     } else if ("descr".equals(n.getNodeName())) {
                        group.setDescription(n.getAttributes().getNamedItem("lang").getNodeValue(), n.getAttributes().getNamedItem("val").getNodeValue());
                     } else if ("section".equals(n.getNodeName())) {
                        PetitionSection subGroup = new PetitionSection(Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue()));
                        group.addSubGroup(subGroup);

                        for(Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
                           if ("name".equalsIgnoreCase(e.getNodeName())) {
                              subGroup.setName(e.getAttributes().getNamedItem("lang").getNodeValue(), e.getAttributes().getNamedItem("val").getNodeValue());
                           } else if ("descr".equals(e.getNodeName())) {
                              subGroup.setDescription(
                                 e.getAttributes().getNamedItem("lang").getNodeValue(), e.getAttributes().getNamedItem("val").getNodeValue()
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public PetitionMainGroup getPetitionGroup(int val) {
      return this._petitionGroups.get(val);
   }

   public Collection<PetitionMainGroup> getPetitionGroups() {
      return this._petitionGroups.valueCollection();
   }

   public static PetitionGroupParser getInstance() {
      return PetitionGroupParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final PetitionGroupParser _instance = new PetitionGroupParser();
   }
}
