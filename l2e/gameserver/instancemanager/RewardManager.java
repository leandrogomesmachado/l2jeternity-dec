package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class RewardManager {
   private static final Logger _log = Logger.getLogger(RewardManager.class.getName());
   private boolean _allowPvpReward = false;
   private boolean _allowFortPvpReward = false;
   private boolean _allowCastlePvpReward = false;
   private boolean _allowClanWarReward = false;
   private boolean _allowTerritoryWarReward = false;
   private boolean _allowCaptureFortReward = false;
   private boolean _allowCaptureCastleReward = false;
   private boolean _allowDefenceFortReward = false;
   private boolean _allowDefenceCastleReward = false;
   private boolean _allowPvpRewardForParty = false;
   private boolean _allowCastlePvpRewardForParty = false;
   private boolean _allowFortPvpRewardForParty = false;
   private boolean _allowClanWarPvpRewardForParty = false;
   private boolean _allowTerritoryWarPvpRewardForParty = false;
   private boolean _allowCaptureFortRewardForLeader = false;
   private boolean _allowCaptureCastleRewardForLeader = false;
   private boolean _allowDefenceFortRewardForLeader = false;
   private boolean _allowDefenceCastleRewardForLeader = false;
   private final List<ItemHolder> _pvpRewards = new ArrayList<>();
   private final List<ItemHolder> _fortPvpRewards = new ArrayList<>();
   private final List<ItemHolder> _castlePvpRewards = new ArrayList<>();
   private final List<ItemHolder> _fortCaptureRewards = new ArrayList<>();
   private final List<ItemHolder> _castleCaptureRewards = new ArrayList<>();
   private final List<ItemHolder> _fortDefenceRewards = new ArrayList<>();
   private final List<ItemHolder> _castleDefenceRewards = new ArrayList<>();
   private final List<ItemHolder> _clanWarRewards = new ArrayList<>();
   private final List<ItemHolder> _territoryWarRewards = new ArrayList<>();
   private static RewardManager _instance;

   public RewardManager() {
      this._pvpRewards.clear();
      this._fortPvpRewards.clear();
      this._castlePvpRewards.clear();
      this._fortCaptureRewards.clear();
      this._castleCaptureRewards.clear();
      this._fortDefenceRewards.clear();
      this._castleDefenceRewards.clear();
      this._clanWarRewards.clear();
      this._territoryWarRewards.clear();
      this.loadRewards();
   }

   public void reload() {
      this._pvpRewards.clear();
      this._fortPvpRewards.clear();
      this._castlePvpRewards.clear();
      this._fortCaptureRewards.clear();
      this._castleCaptureRewards.clear();
      this._fortDefenceRewards.clear();
      this._castleDefenceRewards.clear();
      this._clanWarRewards.clear();
      this._territoryWarRewards.clear();
      this.loadRewards();
   }

   private void loadRewards() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/config/mods/customRewards.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("pvp".equalsIgnoreCase(d1.getNodeName())) {
                     this._allowPvpReward = Boolean.parseBoolean(d1.getAttributes().getNamedItem("allowReward").getNodeValue());
                     this._allowPvpRewardForParty = Boolean.parseBoolean(d1.getAttributes().getNamedItem("allowForParty").getNodeValue());

                     for(Node e1 = d1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                        if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                           int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                           long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                           double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                           this._pvpRewards.add(new ItemHolder(itemId, count, chance));
                           ++counter;
                        }
                     }
                  } else if ("castle".equalsIgnoreCase(d1.getNodeName())) {
                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("pvp".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowCastlePvpReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowCastlePvpRewardForParty = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowForParty").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._castlePvpRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        } else if ("capture".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowCaptureCastleReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowCaptureCastleRewardForLeader = Boolean.parseBoolean(s1.getAttributes().getNamedItem("onlyForLeader").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._castleCaptureRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        } else if ("defence".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowDefenceCastleReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowDefenceCastleRewardForLeader = Boolean.parseBoolean(s1.getAttributes().getNamedItem("onlyForLeader").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._castleDefenceRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        }
                     }
                  } else if ("fort".equalsIgnoreCase(d1.getNodeName())) {
                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("pvp".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowFortPvpReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowFortPvpRewardForParty = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowForParty").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._fortPvpRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        } else if ("capture".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowCaptureFortReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowCaptureFortRewardForLeader = Boolean.parseBoolean(s1.getAttributes().getNamedItem("onlyForLeader").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._fortCaptureRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        } else if ("defence".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowDefenceFortReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowDefenceFortRewardForLeader = Boolean.parseBoolean(s1.getAttributes().getNamedItem("onlyForLeader").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._fortDefenceRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        }
                     }
                  } else if ("clanWar".equalsIgnoreCase(d1.getNodeName())) {
                     this._allowClanWarReward = Boolean.parseBoolean(d1.getAttributes().getNamedItem("allowReward").getNodeValue());
                     this._allowClanWarPvpRewardForParty = Boolean.parseBoolean(d1.getAttributes().getNamedItem("allowForParty").getNodeValue());

                     for(Node e1 = d1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                        if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                           int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                           long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                           double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                           this._clanWarRewards.add(new ItemHolder(itemId, count, chance));
                           ++counter;
                        }
                     }
                  } else if ("territoryWar".equalsIgnoreCase(d1.getNodeName())) {
                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("pvp".equalsIgnoreCase(s1.getNodeName())) {
                           this._allowTerritoryWarReward = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowReward").getNodeValue());
                           this._allowTerritoryWarPvpRewardForParty = Boolean.parseBoolean(s1.getAttributes().getNamedItem("allowForParty").getNodeValue());

                           for(Node e1 = s1.getFirstChild(); e1 != null; e1 = e1.getNextSibling()) {
                              if ("reward".equalsIgnoreCase(e1.getNodeName())) {
                                 int itemId = Integer.parseInt(e1.getAttributes().getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(e1.getAttributes().getNamedItem("count").getNodeValue());
                                 double chance = Double.parseDouble(e1.getAttributes().getNamedItem("chance").getNodeValue());
                                 this._territoryWarRewards.add(new ItemHolder(itemId, count, chance));
                                 ++counter;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         _log.info("RewardManager: Loaded " + counter + " custom rewards.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var14) {
         _log.log(Level.WARNING, "RewardManager: customRewards.xml could not be initialized.", (Throwable)var14);
      } catch (IllegalArgumentException | IOException var15) {
         _log.log(Level.WARNING, "RewardManager: IOException or IllegalArgumentException.", (Throwable)var15);
      }
   }

   public void checkPvpReward(Player killer, Player target) {
      if (this._allowPvpReward && !this._pvpRewards.isEmpty()) {
         if (this._allowPvpRewardForParty && killer.isInParty()) {
            if (target.isInParty() && target.getParty() == killer.getParty()) {
               return;
            }

            for(Player pm : killer.getParty().getMembers()) {
               if (pm != null) {
                  for(ItemHolder tr : this._pvpRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(pm, tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         } else {
            for(ItemHolder tr : this._pvpRewards) {
               if (Rnd.chance(tr.getChance())) {
                  this.getReward(killer, tr.getId(), tr.getCount());
               }
            }
         }
      }
   }

   public void checkTerritoryWarReward(Player killer, Player target) {
      if (this._allowTerritoryWarReward && !this._territoryWarRewards.isEmpty()) {
         if (this._allowTerritoryWarPvpRewardForParty && killer.isInParty()) {
            if (target.isInParty() && target.getParty() == killer.getParty()) {
               return;
            }

            for(Player pm : killer.getParty().getMembers()) {
               if (pm != null) {
                  for(ItemHolder tr : this._territoryWarRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(pm, tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         } else {
            for(ItemHolder tr : this._territoryWarRewards) {
               if (Rnd.chance(tr.getChance())) {
                  this.getReward(killer, tr.getId(), tr.getCount());
               }
            }
         }
      }
   }

   public void checkClanWarReward(Player killer, Player target) {
      if (this._allowClanWarReward && !this._clanWarRewards.isEmpty()) {
         if (this._allowClanWarPvpRewardForParty && killer.isInParty()) {
            if (target.isInParty() && target.getParty() == killer.getParty()) {
               return;
            }

            for(Player pm : killer.getParty().getMembers()) {
               if (pm != null) {
                  for(ItemHolder tr : this._clanWarRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(pm, tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         } else {
            for(ItemHolder tr : this._clanWarRewards) {
               if (Rnd.chance(tr.getChance())) {
                  this.getReward(killer, tr.getId(), tr.getCount());
               }
            }
         }
      }
   }

   public void checkFortPvpReward(Player killer, Player target) {
      if (this._allowFortPvpReward && !this._fortPvpRewards.isEmpty()) {
         if (this._allowFortPvpRewardForParty && killer.isInParty()) {
            if (target.isInParty() && target.getParty() == killer.getParty()) {
               return;
            }

            for(Player pm : killer.getParty().getMembers()) {
               if (pm != null) {
                  for(ItemHolder tr : this._fortPvpRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(pm, tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         } else {
            for(ItemHolder tr : this._fortPvpRewards) {
               if (Rnd.chance(tr.getChance())) {
                  this.getReward(killer, tr.getId(), tr.getCount());
               }
            }
         }
      }
   }

   public void checkFortCaptureReward(Clan clan) {
      if (this._allowCaptureFortReward && !this._fortCaptureRewards.isEmpty() && clan != null) {
         if (this._allowCaptureFortRewardForLeader) {
            Player leader = World.getInstance().getPlayer(clan.getLeaderId());
            if (leader != null) {
               for(ItemHolder tr : this._fortCaptureRewards) {
                  if (Rnd.chance(tr.getChance())) {
                     this.getReward(leader, tr.getId(), tr.getCount());
                  }
               }
            }
         } else {
            for(ClanMember member : clan.getMembers()) {
               if (member != null && member.isOnline()) {
                  for(ItemHolder tr : this._fortCaptureRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(member.getPlayerInstance(), tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         }
      }
   }

   public void checkFortDefenceReward(Clan clan) {
      if (this._allowDefenceFortReward && !this._fortDefenceRewards.isEmpty() && clan != null) {
         if (this._allowDefenceFortRewardForLeader) {
            Player leader = World.getInstance().getPlayer(clan.getLeaderId());
            if (leader != null) {
               for(ItemHolder tr : this._fortDefenceRewards) {
                  if (Rnd.chance(tr.getChance())) {
                     this.getReward(leader, tr.getId(), tr.getCount());
                  }
               }
            }
         } else {
            for(ClanMember member : clan.getMembers()) {
               if (member != null && member.isOnline()) {
                  for(ItemHolder tr : this._fortDefenceRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(member.getPlayerInstance(), tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         }
      }
   }

   public void checkCastlePvpReward(Player killer, Player target) {
      if (this._allowCastlePvpReward && !this._castlePvpRewards.isEmpty()) {
         if (this._allowCastlePvpRewardForParty && killer.isInParty()) {
            if (target.isInParty() && target.getParty() == killer.getParty()) {
               return;
            }

            for(Player pm : killer.getParty().getMembers()) {
               if (pm != null) {
                  for(ItemHolder tr : this._castlePvpRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(pm, tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         } else {
            for(ItemHolder tr : this._castlePvpRewards) {
               if (Rnd.chance(tr.getChance())) {
                  this.getReward(killer, tr.getId(), tr.getCount());
               }
            }
         }
      }
   }

   public void checkCastleCaptureReward(Clan clan) {
      if (this._allowCaptureCastleReward && !this._castleCaptureRewards.isEmpty() && clan != null) {
         if (this._allowCaptureCastleRewardForLeader) {
            Player leader = World.getInstance().getPlayer(clan.getLeaderId());
            if (leader != null) {
               for(ItemHolder tr : this._castleCaptureRewards) {
                  if (Rnd.chance(tr.getChance())) {
                     this.getReward(leader, tr.getId(), tr.getCount());
                  }
               }
            }
         } else {
            for(ClanMember member : clan.getMembers()) {
               if (member != null && member.isOnline()) {
                  for(ItemHolder tr : this._castleCaptureRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(member.getPlayerInstance(), tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         }
      }
   }

   public void checkCastleDefenceReward(Clan clan) {
      if (this._allowDefenceCastleReward && !this._castleDefenceRewards.isEmpty() && clan != null) {
         if (this._allowDefenceCastleRewardForLeader) {
            Player leader = World.getInstance().getPlayer(clan.getLeaderId());
            if (leader != null) {
               for(ItemHolder tr : this._castleDefenceRewards) {
                  if (Rnd.chance(tr.getChance())) {
                     this.getReward(leader, tr.getId(), tr.getCount());
                  }
               }
            }
         } else {
            for(ClanMember member : clan.getMembers()) {
               if (member != null && member.isOnline()) {
                  for(ItemHolder tr : this._castleDefenceRewards) {
                     if (Rnd.chance(tr.getChance())) {
                        this.getReward(member.getPlayerInstance(), tr.getId(), tr.getCount());
                     }
                  }
               }
            }
         }
      }
   }

   private void getReward(Player player, int itemId, long amount) {
      if (itemId == -100) {
         if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
            player.sendPacket(sm);
            return;
         }

         if ((long)player.getPcBangPoints() + amount > (long)Config.MAX_PC_BANG_POINTS) {
            amount = (long)(Config.MAX_PC_BANG_POINTS - player.getPcBangPoints());
         }

         player.setPcBangPoints((int)((long)player.getPcBangPoints() + amount));
         SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
         smsg.addNumber((int)amount);
         player.sendPacket(smsg);
         player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int)amount, false, false, 1));
      } else if (itemId == -200) {
         player.getClan().addReputationScore((int)amount, true);
      } else if (itemId == -300) {
         player.setFame((int)((long)player.getFame() + amount));
         player.sendUserInfo();
      } else if (itemId > 0) {
         player.addItem("Reward", itemId, amount, player, true);
      }
   }

   public static RewardManager getInstance() {
      if (_instance == null) {
         _instance = new RewardManager();
      }

      return _instance;
   }
}
