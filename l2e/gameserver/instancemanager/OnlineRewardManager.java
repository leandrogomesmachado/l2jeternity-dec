package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.online.OnlinePlayerTemplate;
import l2e.gameserver.model.actor.templates.player.online.OnlineRewardTemplate;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class OnlineRewardManager {
   protected static final Logger _log = Logger.getLogger(OnlineRewardManager.class.getName());
   private static final OnlineRewardManager _instance = new OnlineRewardManager();
   private boolean _hwidCheck;
   private boolean _isShowTimer;
   private int _playersLimit;
   private boolean _isActive = false;
   private final Map<String, OnlinePlayerTemplate> _activePlayers = new HashMap<>();
   private final Map<Integer, OnlineRewardTemplate> _rewardList = new HashMap<>();

   public static final OnlineRewardManager getInstance() {
      return _instance;
   }

   public OnlineRewardManager() {
      if (this.isAllowXmlFile() && !this._isActive) {
         this._activePlayers.clear();
         this._rewardList.clear();
         this.loadRewards();
         this._isActive = true;
      }
   }

   public boolean reloadRewards() {
      if (this.isAllowXmlFile() && this._isActive) {
         this._isActive = false;
         this._rewardList.clear();
         this.loadRewards();
         this._isActive = true;
         return true;
      } else {
         return false;
      }
   }

   private void loadRewards() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/onlineRewards.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               this._isShowTimer = n1.getAttributes().getNamedItem("showTimer") != null
                  ? Boolean.parseBoolean(n1.getAttributes().getNamedItem("showTimer").getNodeValue())
                  : true;
               this._hwidCheck = Boolean.parseBoolean(n1.getAttributes().getNamedItem("hwidCheck").getNodeValue());
               this._playersLimit = Integer.parseInt(n1.getAttributes().getNamedItem("playersLimit").getNodeValue());

               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("reward".equalsIgnoreCase(d1.getNodeName())) {
                     ++counter;
                     OnlineRewardTemplate template = null;
                     List<ItemHolder> rewards = new ArrayList<>();
                     int id = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());
                     int time = Integer.parseInt(d1.getAttributes().getNamedItem("time").getNodeValue());
                     boolean printItem = d1.getAttributes().getNamedItem("printItem") != null
                        ? Boolean.parseBoolean(d1.getAttributes().getNamedItem("printItem").getNodeValue())
                        : false;

                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("item".equalsIgnoreCase(s1.getNodeName())) {
                           int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue());
                           long count = Long.parseLong(s1.getAttributes().getNamedItem("count").getNodeValue());
                           double chance = Double.parseDouble(s1.getAttributes().getNamedItem("chance").getNodeValue());
                           rewards.add(new ItemHolder(itemId, count, chance));
                        }
                     }

                     template = new OnlineRewardTemplate(id, time, rewards, printItem);
                     this._rewardList.put(id, template);
                  }
               }
            }
         }

         _log.info("OnlineRewardManager: Loaded " + counter + " online reward templates.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var18) {
         _log.log(Level.WARNING, "OnlineRewardManager: onlineRewards.xml could not be initialized.", (Throwable)var18);
      } catch (IllegalArgumentException | IOException var19) {
         _log.log(Level.WARNING, "OnlineRewardManager: IOException or IllegalArgumentException.", (Throwable)var19);
      }
   }

   public void checkOnlineReward(Player player) {
      if (player != null && this._isActive) {
         if (!player.isInOfflineMode()) {
            OnlinePlayerTemplate tpl = this._activePlayers.get(this._hwidCheck ? player.getHWID() : player.getIPAddress());
            if (tpl != null) {
               if (!tpl.getPlayer(player) && tpl.getPlayers().size() < this._playersLimit) {
                  tpl.addPlayer(player);
                  OnlineRewardTemplate reward = this.getOnlineReward(tpl.getPlayerRewardId(player) + 1);
                  if (reward != null) {
                     this.setTimeLeft(player, tpl, reward);
                  }
               }
            } else {
               tpl = new OnlinePlayerTemplate(player.getHWID(), player.getIPAddress());
               tpl.addPlayer(player);
               this._activePlayers.put(this._hwidCheck ? player.getHWID() : player.getIPAddress(), tpl);
               OnlineRewardTemplate reward = this.getOnlineReward(tpl.getPlayerRewardId(player) + 1);
               if (reward != null) {
                  this.setTimeLeft(player, tpl, reward);
               }
            }
         }
      }
   }

   private void setTimeLeft(Player player, OnlinePlayerTemplate template, OnlineRewardTemplate tpl) {
      if (player != null && this._isActive) {
         if (!player.isInOfflineMode()) {
            long templateTimer = template.getPlayerTimer(player);
            boolean isNewCalc = templateTimer <= 0L;
            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            if (isNewCalc) {
               endTime = startTime + (long)(tpl.getMinutes() * 60000);
            } else {
               endTime = startTime + templateTimer;
            }

            ServerMessage msg = null;
            if (tpl != null && tpl.isPrintItem() && tpl.haveRewards()) {
               msg = new ServerMessage("OnlineRewards.ITEM_INFO", player.getLang());
               msg.add(Util.getItemName(player, tpl.getRewards().get(0).getId()));
            } else {
               msg = new ServerMessage("OnlineRewards.LAST_TIME", player.getLang());
            }

            if (this._isShowTimer) {
               player.sendPacket(new ExSendUIEvent(player, false, false, (int)(endTime / 1000L - startTime / 1000L), 0, msg.toString()));
            } else {
               ServerMessage message = new ServerMessage("OnlineRewards.LAST_TIME_MSG", player.getLang());
               message.add(TimeUtils.formatTime(player, (int)((endTime - System.currentTimeMillis()) / 1000L), false));
               CreatureSay pm = new CreatureSay(0, 2, ServerStorage.getInstance().getString(player.getLang(), "OnlineRewards.TITLE"), message.toString());
               player.sendPacket(pm);
            }

            template.updatePlayerTimer(player, endTime);
            player.startOnlineRewardTask(endTime - System.currentTimeMillis());
         }
      }
   }

   public void activePlayerDisconnect(Player player) {
      if (player != null && this._isActive) {
         OnlinePlayerTemplate tpl = this._activePlayers.get(this._hwidCheck ? player.getHWID() : player.getIPAddress());
         if (tpl == null) {
            for(OnlinePlayerTemplate template : this._activePlayers.values()) {
               if (template != null && template.getPlayer(player)) {
                  tpl = template;
                  break;
               }
            }
         }

         if (tpl != null && tpl.getPlayer(player)) {
            tpl.updatePlayerTimer(player, tpl.getPlayerTimer(player) - System.currentTimeMillis());
            tpl.removePlayer(player);
         }
      }
   }

   public void getOnlineReward(Player player) {
      if (player != null && this._isActive) {
         OnlinePlayerTemplate tpl = this._activePlayers.get(this._hwidCheck ? player.getHWID() : player.getIPAddress());
         if (tpl != null) {
            tpl.updatePlayerRewardId(player, tpl.getPlayerRewardId(player) + 1);
            tpl.updatePlayerTimer(player, 0L);
            OnlineRewardTemplate reward = this.getOnlineReward(tpl.getPlayerRewardId(player));
            if (reward == null) {
               return;
            }

            if (reward.haveRewards()) {
               for(ItemHolder holder : reward.getRewards()) {
                  if (holder != null) {
                     if (holder.getChance() < 100.0) {
                        if (Rnd.chance(holder.getChance())) {
                           this.rewardItems(holder, player);
                        }
                     } else {
                        this.rewardItems(holder, player);
                     }
                  }
               }
            }

            reward = this.getOnlineReward(tpl.getPlayerRewardId(player) + 1);
            if (reward != null) {
               this.setTimeLeft(player, tpl, reward);
            }
         }
      }
   }

   private void rewardItems(ItemHolder holder, Player player) {
      if (holder.getId() == -300) {
         player.setFame((int)((long)player.getFame() + holder.getCount()));
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
         sm.addNumber((int)holder.getCount());
         player.sendPacket(sm);
         player.sendUserInfo();
      } else if (holder.getId() == -200) {
         player.getClan().addReputationScore((int)holder.getCount(), true);
         ServerMessage msg = new ServerMessage("ServiceBBS.ADD_REP", player.getLang());
         msg.add(String.valueOf((int)holder.getCount()));
         player.sendMessage(msg.toString());
      } else if (holder.getId() == -100) {
         player.setPcBangPoints((int)((long)player.getPcBangPoints() + holder.getCount()));
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
         sm.addNumber((int)holder.getCount());
         player.sendPacket(sm);
         player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int)holder.getCount(), false, false, 1));
      } else if (holder.getId() == -1) {
         player.setGamePoints(player.getGamePoints() + holder.getCount());
         ServerMessage msg = new ServerMessage("ServiceBBS.ADD_GAME_POINTS", player.getLang());
         msg.add(String.valueOf((int)holder.getCount()));
         player.sendMessage(msg.toString());
      } else {
         player.addItem("Online", holder.getId(), holder.getCount(), player, true);
      }
   }

   public OnlineRewardTemplate getOnlineReward(int id) {
      return this._rewardList.get(id);
   }

   public Map<Integer, OnlineRewardTemplate> getOnlineRewards() {
      return this._rewardList;
   }

   public boolean isHwidCheck() {
      return this._hwidCheck;
   }

   private boolean isAllowXmlFile() {
      return new File(Config.DATAPACK_ROOT + "/data/stats/services/onlineRewards.xml").exists();
   }
}
