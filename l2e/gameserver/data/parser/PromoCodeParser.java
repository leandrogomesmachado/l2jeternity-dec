package l2e.gameserver.data.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.data.dao.PromoCodeDAO;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.promocode.PromoCodeTemplate;
import l2e.gameserver.model.actor.templates.promocode.impl.AbstractCodeReward;
import l2e.gameserver.model.strings.server.ServerMessage;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class PromoCodeParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(PromoCodeParser.class.getName());
   private final Map<String, PromoCodeTemplate> _promoCodes = new HashMap<>();
   private final Map<String, List<String>> _codeHwidList = new ConcurrentHashMap<>();
   private final Map<String, List<String>> _codeAccountList = new ConcurrentHashMap<>();
   private final Map<String, List<Integer>> _codeCharList = new ConcurrentHashMap<>();
   private final Map<String, Long> _delayList = new ConcurrentHashMap<>();
   private final Map<String, Long> _delayRewardList = new ConcurrentHashMap<>();
   private boolean canUse = false;

   private PromoCodeParser() {
      this._promoCodes.clear();
      this._codeHwidList.clear();
      this._codeAccountList.clear();
      this._codeCharList.clear();
      this.load();
      this.restore();
      this.canUse = true;
   }

   public void reload() {
      this.canUse = false;
      this._promoCodes.clear();
      this._codeHwidList.clear();
      this._codeAccountList.clear();
      this._codeCharList.clear();
      this.load();
      this.restore();
      this.canUse = true;
   }

   public void restore() {
      this.restoreCodes();
      this.restoreCharacterCodes();
      this.restoreAccountCodes();
      this.restoreHwidCodes();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/promoCodes.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._promoCodes.size() + " promocode templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("code".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap code = d.getAttributes();
                  String name = code.getNamedItem("name").getNodeValue();
                  int limit = Integer.parseInt(code.getNamedItem("limit").getNodeValue());
                  boolean limitByAccount = code.getNamedItem("limitByAccount") != null
                     ? Boolean.parseBoolean(code.getNamedItem("limitByAccount").getNodeValue())
                     : false;
                  boolean limitHWID = code.getNamedItem("limitByHWID") != null ? Boolean.parseBoolean(code.getNamedItem("limitByHWID").getNodeValue()) : false;
                  List<AbstractCodeReward> rewards = new ArrayList<>();
                  long fromValue = -1L;
                  long toValue = -1L;
                  int minLvl = 1;
                  int maxLvl = 85;
                  boolean canUseSubClass = false;

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     code = cd.getAttributes();
                     if ("date".equalsIgnoreCase(cd.getNodeName())) {
                        try {
                           fromValue = code.getNamedItem("from") != null ? TimeUtils.parse(code.getNamedItem("from").getNodeValue()) : -1L;
                        } catch (Exception var22) {
                        }

                        try {
                           toValue = code.getNamedItem("to") != null ? TimeUtils.parse(code.getNamedItem("to").getNodeValue()) : -1L;
                        } catch (Exception var21) {
                        }
                     } else if ("limit".equalsIgnoreCase(cd.getNodeName())) {
                        minLvl = code.getNamedItem("minLvl") != null ? Integer.parseInt(code.getNamedItem("minLvl").getNodeValue()) : 1;
                        maxLvl = code.getNamedItem("maxLvl") != null ? Integer.parseInt(code.getNamedItem("maxLvl").getNodeValue()) : 85;
                        canUseSubClass = code.getNamedItem("canUseSubClass") != null
                           ? Boolean.parseBoolean(code.getNamedItem("canUseSubClass").getNodeValue())
                           : false;
                     } else if ("item".equalsIgnoreCase(cd.getNodeName())
                        || "exp".equalsIgnoreCase(cd.getNodeName())
                        || "sp".equalsIgnoreCase(cd.getNodeName())
                        || "setLevel".equalsIgnoreCase(cd.getNodeName())
                        || "addLevel".equalsIgnoreCase(cd.getNodeName())
                        || "premium".equalsIgnoreCase(cd.getNodeName())
                        || "pcPoint".equalsIgnoreCase(cd.getNodeName())
                        || "reputation".equalsIgnoreCase(cd.getNodeName())
                        || "fame".equalsIgnoreCase(cd.getNodeName())) {
                        Class<AbstractCodeReward> aClass = null;

                        try {
                           aClass = Class.forName(
                              "l2e.gameserver.model.actor.templates.promocode.impl." + StringUtils.capitalize(cd.getNodeName()) + "CodeReward"
                           );
                        } catch (Exception var25) {
                           _log.warning("Not found class " + cd.getNodeName() + "CodeReward.java!");
                        }

                        Constructor<AbstractCodeReward> constructor = null;

                        try {
                           constructor = aClass.getConstructor(NamedNodeMap.class);
                        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException var24) {
                           _log.warning("Unable to create code reward class " + aClass.getSimpleName() + "!");
                        }

                        AbstractCodeReward reward = null;

                        try {
                           reward = constructor.newInstance(code);
                        } catch (InvocationTargetException | InstantiationException | IllegalArgumentException | IllegalAccessException var23) {
                           _log.warning("Unable to create reward!");
                        }

                        rewards.add(reward);
                     }
                  }

                  this._promoCodes
                     .put(name, new PromoCodeTemplate(name, minLvl, maxLvl, canUseSubClass, fromValue, toValue, limit, rewards, limitByAccount, limitHWID));
               }
            }
         }
      }
   }

   public PromoCodeTemplate getPromoCode(String name) {
      return this._promoCodes.get(name);
   }

   public boolean isValidCheckTime(Player player, boolean isGetReward) {
      long l = System.currentTimeMillis();
      if (isGetReward) {
         if (this._delayRewardList.containsKey(player.getIPAddress()) && l < this._delayRewardList.get(player.getIPAddress())) {
            player.sendMessage(new ServerMessage("PromoCode.DELAY_MSG", player.getLang()).toString());
            return false;
         }

         this._delayRewardList.put(player.getIPAddress(), System.currentTimeMillis() + (long)(Config.PROMOCODES_USE_DELAY * 1000));
      } else {
         if (this._delayList.containsKey(player.getIPAddress()) && l < this._delayList.get(player.getIPAddress())) {
            player.sendMessage(new ServerMessage("PromoCode.DELAY_MSG", player.getLang()).toString());
            return false;
         }

         this._delayList.put(player.getIPAddress(), System.currentTimeMillis() + (long)(Config.PROMOCODES_USE_DELAY * 1000));
      }

      return true;
   }

   public boolean isActivePromoCode(PromoCodeTemplate tpl, Player player, boolean saveInfo) {
      if (!this.canUse) {
         return false;
      } else {
         long l = System.currentTimeMillis();
         if (!tpl.canUseSubClass() && player.isSubClassActive()) {
            player.sendMessage(new ServerMessage("PromoCode.CANT_USE_SUB", player.getLang()).toString());
            return false;
         } else if (player.getLevel() < tpl.getMinLvl() || player.getLevel() > tpl.getMaxLvl()) {
            player.sendMessage(new ServerMessage("PromoCode.WRONG_LEVEL", player.getLang()).toString());
            return false;
         } else if (this._codeCharList.containsKey(tpl.getName()) && this._codeCharList.get(tpl.getName()).contains(player.getObjectId())) {
            player.sendMessage(new ServerMessage("PromoCode.ALREADY_USED", player.getLang()).toString());
            return false;
         } else if (tpl.getStartDate() > 0L && l < tpl.getStartDate()) {
            player.sendMessage(new ServerMessage("PromoCode.NOT_START", player.getLang()).toString());
            return false;
         } else if (tpl.getEndDate() > 0L && l > tpl.getEndDate()) {
            player.sendMessage(new ServerMessage("PromoCode.ALREADY_END", player.getLang()).toString());
            return false;
         } else if (tpl.isLimitByAccount()
            && this._codeAccountList.containsKey(tpl.getName())
            && this._codeAccountList.get(tpl.getName()).contains(player.getHWID())) {
            player.sendMessage(new ServerMessage("PromoCode.ALREADY_USED", player.getLang()).toString());
            return false;
         } else if (tpl.isLimitHWID() && this._codeHwidList.containsKey(tpl.getName()) && this._codeHwidList.get(tpl.getName()).contains(player.getHWID())) {
            player.sendMessage(new ServerMessage("PromoCode.ALREADY_USED", player.getLang()).toString());
            return false;
         } else {
            if (tpl.getLimit() > 0) {
               int i = tpl.getCurLimit();
               if (i >= tpl.getLimit()) {
                  player.sendMessage(new ServerMessage("PromoCode.LIMIT_EXCEEDED", player.getLang()).toString());
                  return false;
               }

               if (saveInfo) {
                  tpl.setCurLimit(i + 1);
               }
            }

            if (saveInfo) {
               PromoCodeDAO.getInstance().insert(player, tpl);
            }

            return true;
         }
      }
   }

   public void addToHwidList(String name, String hwid) {
      if (!this._codeHwidList.containsKey(name)) {
         this._codeHwidList.put(name, new ArrayList<>());
      }

      this._codeHwidList.get(name).add(hwid);
   }

   public void addToAccountList(String name, String account) {
      if (!this._codeAccountList.containsKey(name)) {
         this._codeAccountList.put(name, new ArrayList<>());
      }

      this._codeAccountList.get(name).add(account);
   }

   public void addToCharList(String name, int objId) {
      if (!this._codeCharList.containsKey(name)) {
         this._codeCharList.put(name, new ArrayList<>());
      }

      this._codeCharList.get(name).add(objId);
   }

   private void restoreCodes() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM promocodes");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            PromoCodeTemplate tpl = this.getPromoCode(rset.getString("name"));
            if (tpl != null && tpl.getLimit() > 0) {
               tpl.setCurLimit(rset.getInt("value"));
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not restore promocodes " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void restoreCharacterCodes() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_promocodes");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this.addToCharList(rset.getString("name"), rset.getInt("charId"));
         }

         rset.close();
         statement.close();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not restore character_promocodes " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void restoreAccountCodes() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_promocodes_account");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this.addToAccountList(rset.getString("name"), rset.getString("account"));
         }

         rset.close();
         statement.close();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not restore character_promocodes_account " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void restoreHwidCodes() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_promocodes_hwid");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this.addToHwidList(rset.getString("name"), rset.getString("hwid"));
         }

         rset.close();
         statement.close();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not restore character_promocodes_hwid " + var59.getMessage(), (Throwable)var59);
      }
   }

   public static PromoCodeParser getInstance() {
      return PromoCodeParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final PromoCodeParser _instance = new PromoCodeParser();
   }
}
