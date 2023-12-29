package l2e.gameserver.data.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class VoteRewardParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(VoteRewardParser.class.getName());
   private final Map<String, VoteRewardSite> _voteRewardSites = new HashMap<>();
   private int _minLevel;

   protected VoteRewardParser() {
      this._voteRewardSites.clear();
      this.load();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._voteRewardSites.size() + " vote reward sites templates.");
      this.callInit();
   }

   @Override
   public void load() {
      this.parseDatapackFile("config/mods/votereward.xml");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            this._minLevel = Integer.parseInt(c.getAttributes().getNamedItem("minLevel").getNodeValue());

            for(Node npc = c.getFirstChild(); npc != null; npc = npc.getNextSibling()) {
               if ("vote".equalsIgnoreCase(npc.getNodeName())) {
                  NamedNodeMap attrs = npc.getAttributes();
                  String impl = attrs.getNamedItem("impl").getNodeValue();
                  Class<VoteRewardSite> voteRewardSiteClass = null;

                  try {
                     voteRewardSiteClass = Class.forName("l2e.gameserver.model.entity.mods.votereward.impl." + impl + "Site");
                  } catch (ClassNotFoundException var12) {
                     try {
                        voteRewardSiteClass = Class.forName("l2e.scripts.votereward." + impl + "Site");
                     } catch (ClassNotFoundException var11) {
                        var11.printStackTrace();
                     }
                  }

                  if (voteRewardSiteClass == null) {
                     _log.info("Not found impl class: " + impl);
                  } else {
                     boolean enabled = Boolean.parseBoolean(attrs.getNamedItem("enabled").getNodeValue());
                     Constructor<VoteRewardSite> constructor = null;

                     try {
                        constructor = voteRewardSiteClass.getConstructor(MultiValueSet.class);
                     } catch (NoSuchMethodException | SecurityException | IllegalArgumentException var14) {
                        _log.warning("Unable to create voteRewardSiteClass!");
                        var14.printStackTrace();
                     }

                     MultiValueSet<String> parameters = new MultiValueSet<>();
                     parameters.set("name", impl);
                     parameters.set("enabled", enabled);
                     parameters.set(
                        "run_delay", attrs.getNamedItem("run_delay") != null ? Integer.parseInt(attrs.getNamedItem("run_delay").getNodeValue()) : 0
                     );

                     for(Node cat = npc.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                        if ("parameter".equalsIgnoreCase(cat.getNodeName())) {
                           attrs = cat.getAttributes();
                           parameters.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
                        }
                     }

                     VoteRewardSite voteRewardSite = null;

                     try {
                        voteRewardSite = constructor.newInstance(parameters);
                     } catch (InvocationTargetException | InstantiationException | IllegalArgumentException | IllegalAccessException var13) {
                        _log.warning("Unable to create voteRewardSite!");
                        var13.printStackTrace();
                     }

                     for(Node cat = npc.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                        if ("rewards".equalsIgnoreCase(cat.getNodeName())) {
                           voteRewardSite.addRewardList(RewardList.parseRewardList(_log, cat, cat.getAttributes(), RewardType.NOT_RATED_GROUPED, false, impl));
                        }
                     }

                     this.addVoteRewardSite(voteRewardSite);
                  }
               }
            }
         }
      }
   }

   public void addVoteRewardSite(VoteRewardSite site) {
      if (this._voteRewardSites.containsKey(site.getName())) {
         _log.warning(String.format("Dublicate %s Vote Site registered!", site.getName()));
      }

      this._voteRewardSites.put(site.getName(), site);
   }

   public Map<String, VoteRewardSite> getVoteRewardSites() {
      return this._voteRewardSites;
   }

   private void callInit() {
      for(VoteRewardSite site : this._voteRewardSites.values()) {
         site.init();
      }
   }

   public int getMinLevel() {
      return this._minLevel;
   }

   public static VoteRewardParser getInstance() {
      return VoteRewardParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final VoteRewardParser _instance = new VoteRewardParser();
   }
}
