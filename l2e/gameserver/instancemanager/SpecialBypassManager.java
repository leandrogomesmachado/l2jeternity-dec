package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.grandboss.Antharas;
import l2e.scripts.ai.grandboss.ValakasManager;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class SpecialBypassManager {
   private static final Logger _log = Logger.getLogger(SpecialBypassManager.class.getName());
   private final IntObjectMap<SpecialBypassManager.BypassTemplate> _npcTemplates = new HashIntObjectMap<>();

   protected SpecialBypassManager() {
      this._npcTemplates.clear();
      this.load();
      if (this._npcTemplates.size() > 0) {
         _log.info("SpecialBypassManager: Loaded " + this._npcTemplates.size() + " special bypasses.");
         new SpecialBypassManager.SpecialBypass(-1, "SpecialBypass", "teleports");
      }
   }

   public void load() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/speacialBypasses.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);

         for(Node n = doc1.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if ("npc".equalsIgnoreCase(d.getNodeName())) {
                     NamedNodeMap ref = d.getAttributes();
                     String requiredQuest = null;
                     boolean removedItemNecessity = false;
                     int removedItemId = 0;
                     int removedItemCount = 0;
                     int giveItemId = 0;
                     int givedItemCount = 0;
                     int minLevel = 0;
                     int maxLevel = 0;
                     int minParty = 1;
                     int maxParty = 9;
                     List<Location> teleportLocs = Collections.emptyList();
                     SpecialBypassManager.BypassTemplate.BypassRemoveType removeType = null;
                     SpecialBypassManager.BypassTemplate.BypassQuestType questType = null;
                     int id = Integer.parseInt(ref.getNamedItem("id").getNodeValue());
                     boolean dispelBuffs = ref.getNamedItem("dispelBuffs") != null
                        ? Boolean.parseBoolean(ref.getNamedItem("dispelBuffs").getNodeValue())
                        : false;

                     for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                        ref = cd.getAttributes();
                        if ("level".equalsIgnoreCase(cd.getNodeName())) {
                           minLevel = ref.getNamedItem("min") == null ? 1 : Integer.parseInt(ref.getNamedItem("min").getNodeValue());
                           maxLevel = ref.getNamedItem("max") == null ? Integer.MAX_VALUE : Integer.parseInt(ref.getNamedItem("max").getNodeValue());
                        } else if ("party".equalsIgnoreCase(cd.getNodeName())) {
                           minParty = Integer.parseInt(ref.getNamedItem("min").getNodeValue());
                           maxParty = Integer.parseInt(ref.getNamedItem("max").getNodeValue());
                        } else if ("teleport".equalsIgnoreCase(cd.getNodeName())) {
                           if (teleportLocs.isEmpty()) {
                              teleportLocs = new ArrayList<>(1);
                           }

                           int x = Integer.parseInt(ref.getNamedItem("x").getNodeValue());
                           int y = Integer.parseInt(ref.getNamedItem("y").getNodeValue());
                           int z = Integer.parseInt(ref.getNamedItem("z").getNodeValue());
                           teleportLocs.add(new Location(x, y, z));
                        } else if ("remove".equalsIgnoreCase(cd.getNodeName())) {
                           removedItemId = Integer.parseInt(ref.getNamedItem("itemId").getNodeValue());
                           removedItemCount = Integer.parseInt(ref.getNamedItem("count").getNodeValue());
                           removedItemNecessity = Boolean.parseBoolean(ref.getNamedItem("necessary").getNodeValue());
                           removeType = ref.getNamedItem("type") != null
                              ? SpecialBypassManager.BypassTemplate.BypassRemoveType.valueOf(ref.getNamedItem("type").getNodeValue())
                              : SpecialBypassManager.BypassTemplate.BypassRemoveType.NONE;
                        } else if ("give".equalsIgnoreCase(cd.getNodeName())) {
                           giveItemId = Integer.parseInt(ref.getNamedItem("itemId").getNodeValue());
                           givedItemCount = Integer.parseInt(ref.getNamedItem("count").getNodeValue());
                        } else if ("quest".equalsIgnoreCase(cd.getNodeName())) {
                           requiredQuest = ref.getNamedItem("name") != null ? ref.getNamedItem("name").getNodeValue() : null;
                           questType = ref.getNamedItem("type") != null
                              ? SpecialBypassManager.BypassTemplate.BypassQuestType.valueOf(ref.getNamedItem("type").getNodeValue())
                              : SpecialBypassManager.BypassTemplate.BypassQuestType.STARTED;
                        }
                     }

                     this.addNpcBypass(
                        new SpecialBypassManager.BypassTemplate(
                           id,
                           dispelBuffs,
                           minLevel,
                           maxLevel,
                           minParty,
                           maxParty,
                           teleportLocs,
                           removedItemId,
                           removedItemCount,
                           removedItemNecessity,
                           removeType,
                           giveItemId,
                           givedItemCount,
                           requiredQuest,
                           questType
                        )
                     );
                  }
               }
            }
         }
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var26) {
         _log.log(Level.WARNING, "SpecialBypassManager: speacialBypasses.xml could not be initialized.", (Throwable)var26);
      } catch (IllegalArgumentException | IOException var27) {
         _log.log(Level.WARNING, "SpecialBypassManager: IOException or IllegalArgumentException.", (Throwable)var27);
      }
   }

   private boolean checkConditions(Player player, SpecialBypassManager.BypassTemplate template) {
      if (!player.isCursedWeaponEquipped() && !player.isFlying()) {
         switch(template.getEntryType()) {
            case SOLO:
               return this.checkSoloType(player, template);
            case SOLO_PARTY:
               if (player.getParty() == null) {
                  return this.checkSoloType(player, template);
               }

               return this.checkPartyType(player, template);
            case PARTY:
               return this.checkPartyType(player, template);
            case PARTY_COMMAND_CHANNEL:
               if (player.getParty() != null && player.getParty().getCommandChannel() != null) {
                  return this.checkCommandChannelType(player, template);
               }

               return this.checkPartyType(player, template);
            case COMMAND_CHANNEL:
               return this.checkCommandChannelType(player, template);
            default:
               return true;
         }
      } else {
         player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
         return false;
      }
   }

   private void onTeleport(Player player, SpecialBypassManager.BypassTemplate template) {
      switch(template.getEntryType()) {
         case SOLO:
            this.doSoloEnter(player, template);
            break;
         case SOLO_PARTY:
            if (player.getParty() == null) {
               this.doSoloEnter(player, template);
            } else {
               this.doPartyEnter(player, template);
            }
            break;
         case PARTY:
            this.doPartyEnter(player, template);
            break;
         case PARTY_COMMAND_CHANNEL:
            if (player.getParty() != null && player.getParty().getCommandChannel() != null) {
               this.doCommandChannelEnter(player, template);
            } else {
               this.doPartyEnter(player, template);
            }
            break;
         case COMMAND_CHANNEL:
            this.doCommandChannelEnter(player, template);
      }
   }

   protected boolean checkSoloType(Player player, SpecialBypassManager.BypassTemplate template) {
      if (player.getLevel() < template.getMinLevel() || player.getLevel() > template.getMaxLevel()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
         sm.addPcName(player);
         player.sendPacket(sm);
         return false;
      } else if (player.isInParty()) {
         player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
         return false;
      } else {
         if (template.getRemovedItemId() == -100) {
            if (template.getRemovedItemId() == -100 && player.getPcBangPoints() < template.getRemovedItemCount()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(player);
               player.sendPacket(sm);
               return false;
            }
         } else if (template.getRemovedItemId() == -200) {
            if (template.getRemovedItemId() == -200 && (player.getClan() == null || player.getClan().getReputationScore() < template.getRemovedItemCount())) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(player);
               player.sendPacket(sm);
               return false;
            }
         } else if (template.getRemovedItemId() == -300) {
            if (template.getRemovedItemId() == -300 && player.getFame() < template.getRemovedItemCount()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(player);
               player.sendPacket(sm);
               return false;
            }
         } else if (template.getRemovedItemId() > 0
            && (
               player.getInventory().getItemByItemId(template.getRemovedItemId()) == null
                  || player.getInventory().getItemByItemId(template.getRemovedItemId()).getCount() < (long)template.getRemovedItemCount()
            )) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
            sm.addPcName(player);
            player.sendPacket(sm);
            return false;
         }

         if (template.getRequiredQuest() != null) {
            QuestState qs = player.getQuestState(template.getRequiredQuest());
            boolean cannot = false;
            if (template.getQuestType() == SpecialBypassManager.BypassTemplate.BypassQuestType.STARTED) {
               if (qs == null || !qs.isStarted()) {
                  cannot = true;
               }
            } else if (qs == null || !qs.isCompleted()) {
               cannot = true;
            }

            if (cannot) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(player);
               player.sendPacket(sm);
               return false;
            }
         }

         return true;
      }
   }

   protected boolean checkPartyType(Player player, SpecialBypassManager.BypassTemplate template) {
      Party party = player.getParty();
      if (party == null) {
         player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
         return false;
      } else if (party.getLeader() != player) {
         player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
         return false;
      } else if (party.getMemberCount() < template.getMinParty()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
         sm.addNumber(template.getMinParty());
         player.sendPacket(sm);
         return false;
      } else if (party.getMemberCount() > template.getMaxParty()) {
         player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
         return false;
      } else {
         if (template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.LEADER) {
            if (template.getRemovedItemId() == -100) {
               if (template.getRemovedItemId() == -100 && player.getPcBangPoints() < template.getRemovedItemCount()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(player);
                  player.sendPacket(sm);
                  return false;
               }
            } else if (template.getRemovedItemId() == -200) {
               if (template.getRemovedItemId() == -200 && (player.getClan() == null || player.getClan().getReputationScore() < template.getRemovedItemCount())
                  )
                {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(player);
                  player.sendPacket(sm);
                  return false;
               }
            } else if (template.getRemovedItemId() == -300) {
               if (template.getRemovedItemId() == -300 && player.getFame() < template.getRemovedItemCount()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(player);
                  player.sendPacket(sm);
                  return false;
               }
            } else if (template.getRemovedItemId() > 0
               && (
                  player.getInventory().getItemByItemId(template.getRemovedItemId()) == null
                     || player.getInventory().getItemByItemId(template.getRemovedItemId()).getCount() < (long)template.getRemovedItemCount()
               )) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(player);
               player.sendPacket(sm);
               return false;
            }
         }

         for(Player partyMember : party.getMembers()) {
            if (partyMember.getLevel() < template.getMinLevel() || partyMember.getLevel() > template.getMaxLevel()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }

            if (!partyMember.isInsideRadius(player, 500, true, true)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }

            if (template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.ALL) {
               if (template.getRemovedItemId() == -100) {
                  if (template.getRemovedItemId() == -100 && partyMember.getPcBangPoints() < template.getRemovedItemCount()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(partyMember);
                     party.broadCast(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() == -200) {
                  if (template.getRemovedItemId() == -200
                     && (partyMember.getClan() == null || partyMember.getClan().getReputationScore() < template.getRemovedItemCount())) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(partyMember);
                     party.broadCast(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() == -300) {
                  if (template.getRemovedItemId() == -300 && partyMember.getFame() < template.getRemovedItemCount()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(partyMember);
                     party.broadCast(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() > 0
                  && (
                     partyMember.getInventory().getItemByItemId(template.getRemovedItemId()) == null
                        || partyMember.getInventory().getItemByItemId(template.getRemovedItemId()).getCount() < (long)template.getRemovedItemCount()
                  )) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(partyMember);
                  party.broadCast(sm);
                  return false;
               }
            }

            if (template.getRequiredQuest() != null) {
               QuestState qs = partyMember.getQuestState(template.getRequiredQuest());
               boolean cannot = false;
               if (template.getQuestType() == SpecialBypassManager.BypassTemplate.BypassQuestType.STARTED) {
                  if (qs == null || !qs.isStarted()) {
                     cannot = true;
                  }
               } else if (qs == null || !qs.isCompleted()) {
                  cannot = true;
               }

               if (cannot) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(partyMember);
                  party.broadCast(sm);
                  return false;
               }
            }
         }

         return true;
      }
   }

   protected boolean checkCommandChannelType(Player player, SpecialBypassManager.BypassTemplate template) {
      Party pt = player.getParty();
      if (pt != null && pt.getCommandChannel() != null) {
         CommandChannel cc = pt.getCommandChannel();
         if (cc.getMemberCount() < template.getMinParty()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
            sm.addNumber(template.getMinParty());
            player.sendPacket(sm);
            return false;
         } else if (cc.getMemberCount() > template.getMaxParty()) {
            player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
            return false;
         } else {
            if (template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.LEADER) {
               if (template.getRemovedItemId() == -100) {
                  if (template.getRemovedItemId() == -100 && player.getPcBangPoints() < template.getRemovedItemCount()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(player);
                     player.sendPacket(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() == -200) {
                  if (template.getRemovedItemId() == -200
                     && (player.getClan() == null || player.getClan().getReputationScore() < template.getRemovedItemCount())) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(player);
                     player.sendPacket(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() == -300) {
                  if (template.getRemovedItemId() == -300 && player.getFame() < template.getRemovedItemCount()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(player);
                     player.sendPacket(sm);
                     return false;
                  }
               } else if (template.getRemovedItemId() > 0
                  && (
                     player.getInventory().getItemByItemId(template.getRemovedItemId()) == null
                        || player.getInventory().getItemByItemId(template.getRemovedItemId()).getCount() < (long)template.getRemovedItemCount()
                  )) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(player);
                  player.sendPacket(sm);
                  return false;
               }
            }

            for(Player channelMember : cc.getMembers()) {
               if (channelMember.getLevel() < template.getMinLevel() || channelMember.getLevel() > template.getMaxLevel()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(channelMember);
                  cc.broadCast(sm);
                  return false;
               }

               if (!Util.checkIfInRange(1000, player, channelMember, true)) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
                  sm.addPcName(channelMember);
                  cc.broadCast(sm);
                  return false;
               }

               if (template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.ALL) {
                  if (template.getRemovedItemId() == -100) {
                     if (template.getRemovedItemId() == -100 && channelMember.getPcBangPoints() < template.getRemovedItemCount()) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        sm.addPcName(channelMember);
                        cc.broadCast(sm);
                        return false;
                     }
                  } else if (template.getRemovedItemId() == -200) {
                     if (template.getRemovedItemId() == -200
                        && (channelMember.getClan() == null || channelMember.getClan().getReputationScore() < template.getRemovedItemCount())) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        sm.addPcName(channelMember);
                        cc.broadCast(sm);
                        return false;
                     }
                  } else if (template.getRemovedItemId() == -300) {
                     if (template.getRemovedItemId() == -300 && channelMember.getFame() < template.getRemovedItemCount()) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        sm.addPcName(channelMember);
                        cc.broadCast(sm);
                        return false;
                     }
                  } else if (template.getRemovedItemId() > 0
                     && (
                        channelMember.getInventory().getItemByItemId(template.getRemovedItemId()) == null
                           || channelMember.getInventory().getItemByItemId(template.getRemovedItemId()).getCount() < (long)template.getRemovedItemCount()
                     )) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(channelMember);
                     cc.broadCast(sm);
                     return false;
                  }
               }

               if (template.getRequiredQuest() != null) {
                  QuestState qs = channelMember.getQuestState(template.getRequiredQuest());
                  boolean cannot = false;
                  if (template.getQuestType() == SpecialBypassManager.BypassTemplate.BypassQuestType.STARTED) {
                     if (qs == null || !qs.isStarted()) {
                        cannot = true;
                     }
                  } else if (qs == null || !qs.isCompleted()) {
                     cannot = true;
                  }

                  if (cannot) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
                     sm.addPcName(channelMember);
                     cc.broadCast(sm);
                     return false;
                  }
               }
            }

            return true;
         }
      } else {
         player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
         return false;
      }
   }

   private void doSoloEnter(Player player, SpecialBypassManager.BypassTemplate template) {
      if (template.getRemovedItemNecessity()) {
         if (template.getRemovedItemId() == -100) {
            player.setPcBangPoints(player.getPcBangPoints() - template.getRemovedItemCount());
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
            smsg.addNumber(template.getRemovedItemCount());
            player.sendPacket(smsg);
            player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), template.getRemovedItemCount(), false, false, 1));
         } else if (template.getRemovedItemId() == -200) {
            player.getClan().takeReputationScore(template.getRemovedItemCount(), true);
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
            smsg.addItemNumber((long)template.getRemovedItemCount());
            player.sendPacket(smsg);
         } else if (template.getRemovedItemId() == -300) {
            player.setFame(player.getFame() - template.getRemovedItemCount());
            player.sendUserInfo();
         } else if (template.getRemovedItemId() > 0) {
            player.destroyItemByItemId("Instance Check", template.getRemovedItemId(), (long)template.getRemovedItemCount(), player, true);
         }
      }

      if (template.getGiveItemId() > 0) {
         player.addItem("Instance reward", template.getGiveItemId(), (long)template.getGiveItemCount(), null, true);
      }

      this.onTeleportEnter(player, template);
   }

   private void doPartyEnter(Player player, SpecialBypassManager.BypassTemplate template) {
      if (template.getRemovedItemId() > 0
         && template.getRemovedItemNecessity()
         && template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.LEADER) {
         player.destroyItemByItemId("Instance Check", template.getRemovedItemId(), (long)template.getRemovedItemCount(), player, true);
      }

      for(Player partyMember : player.getParty().getMembers()) {
         if (partyMember != null) {
            if (template.getRemovedItemNecessity() && template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.ALL) {
               if (template.getRemovedItemId() == -100) {
                  partyMember.setPcBangPoints(partyMember.getPcBangPoints() - template.getRemovedItemCount());
                  SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsg.addNumber(template.getRemovedItemCount());
                  partyMember.sendPacket(smsg);
                  partyMember.sendPacket(new ExPCCafePointInfo(partyMember.getPcBangPoints(), template.getRemovedItemCount(), false, false, 1));
               } else if (template.getRemovedItemId() == -200) {
                  partyMember.getClan().takeReputationScore(template.getRemovedItemCount(), true);
                  SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                  smsg.addItemNumber((long)template.getRemovedItemCount());
                  partyMember.sendPacket(smsg);
               } else if (template.getRemovedItemId() == -300) {
                  partyMember.setFame(partyMember.getFame() - template.getRemovedItemCount());
                  partyMember.sendUserInfo();
               } else if (template.getRemovedItemId() > 0) {
                  partyMember.destroyItemByItemId("Instance Check", template.getRemovedItemId(), (long)template.getRemovedItemCount(), partyMember, true);
               }
            }

            if (template.getGiveItemId() > 0) {
               partyMember.addItem("Instance reward", template.getGiveItemId(), (long)template.getGiveItemCount(), null, true);
            }

            this.onTeleportEnter(partyMember, template);
         }
      }
   }

   private void doCommandChannelEnter(Player player, SpecialBypassManager.BypassTemplate template) {
      if (template.getRemovedItemId() > 0
         && template.getRemovedItemNecessity()
         && template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.LEADER) {
         player.destroyItemByItemId("Instance Check", template.getRemovedItemId(), (long)template.getRemovedItemCount(), player, true);
      }

      for(Player ccMember : player.getParty().getCommandChannel().getMembers()) {
         if (ccMember != null) {
            if (template.getRemovedItemNecessity() && template.getRemoveType() == SpecialBypassManager.BypassTemplate.BypassRemoveType.ALL) {
               if (template.getRemovedItemId() == -100) {
                  ccMember.setPcBangPoints(ccMember.getPcBangPoints() - template.getRemovedItemCount());
                  SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsg.addNumber(template.getRemovedItemCount());
                  ccMember.sendPacket(smsg);
                  ccMember.sendPacket(new ExPCCafePointInfo(ccMember.getPcBangPoints(), template.getRemovedItemCount(), false, false, 1));
               } else if (template.getRemovedItemId() == -200) {
                  ccMember.getClan().takeReputationScore(template.getRemovedItemCount(), true);
                  SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                  smsg.addItemNumber((long)template.getRemovedItemCount());
                  ccMember.sendPacket(smsg);
               } else if (template.getRemovedItemId() == -300) {
                  ccMember.setFame(ccMember.getFame() - template.getRemovedItemCount());
                  ccMember.sendUserInfo();
               } else if (template.getRemovedItemId() > 0) {
                  ccMember.destroyItemByItemId("Instance Check", template.getRemovedItemId(), (long)template.getRemovedItemCount(), ccMember, true);
               }
            }

            if (template.getGiveItemId() > 0) {
               ccMember.addItem("Instance reward", template.getGiveItemId(), (long)template.getGiveItemCount(), null, true);
            }

            this.onTeleportEnter(ccMember, template);
         }
      }
   }

   private void onTeleportEnter(Player player, SpecialBypassManager.BypassTemplate template) {
      player.getAI().setIntention(CtrlIntention.IDLE);
      Location teleLoc = template.getTeleportCoord();
      player.teleToLocation(teleLoc, true);
      if (player.hasSummon()) {
         player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
         player.getSummon().teleToLocation(teleLoc, true);
      }

      if (template.isDispelBuffs()) {
         player.stopAllEffectsExceptThoseThatLastThroughDeath();
         Summon summon = player.getSummon();
         if (summon != null) {
            summon.stopAllEffectsExceptThoseThatLastThroughDeath();
         }
      }
   }

   public void addNpcBypass(SpecialBypassManager.BypassTemplate tpl) {
      this._npcTemplates.put(tpl.getId(), tpl);
   }

   public SpecialBypassManager.BypassTemplate getNpcBypass(int id) {
      return this._npcTemplates.get(id);
   }

   public static SpecialBypassManager getInstance() {
      return SpecialBypassManager.SingletonHolder._instance;
   }

   public static class BypassTemplate {
      private final int _id;
      private final boolean _dispelBuffs;
      private final int _minLevel;
      private final int _maxLevel;
      private final int _minParty;
      private final int _maxParty;
      private final List<Location> _teleportCoords;
      private final int _removedItemId;
      private final int _removedItemCount;
      private final boolean _removedItemNecessity;
      private final SpecialBypassManager.BypassTemplate.BypassRemoveType _removeType;
      private final int _giveItemId;
      private final int _givedItemCount;
      private SpecialBypassManager.BypassTemplate.BypassEntryType _entryType = null;
      private final String _requiredQuest;
      private final SpecialBypassManager.BypassTemplate.BypassQuestType _questType;

      public BypassTemplate(
         int id,
         boolean dispelBuffs,
         int minLevel,
         int maxLevel,
         int minParty,
         int maxParty,
         List<Location> tele,
         int removedItemId,
         int removedItemCount,
         boolean removedItemNecessity,
         SpecialBypassManager.BypassTemplate.BypassRemoveType removeType,
         int giveItemId,
         int givedItemCount,
         String requiredQuest,
         SpecialBypassManager.BypassTemplate.BypassQuestType questType
      ) {
         this._id = id;
         this._dispelBuffs = dispelBuffs;
         this._minLevel = minLevel;
         this._maxLevel = maxLevel;
         this._teleportCoords = tele;
         this._minParty = minParty;
         this._maxParty = maxParty;
         this._removedItemId = removedItemId;
         this._removedItemCount = removedItemCount;
         this._removedItemNecessity = removedItemNecessity;
         this._removeType = removeType;
         this._giveItemId = giveItemId;
         this._givedItemCount = givedItemCount;
         this._requiredQuest = requiredQuest;
         this._questType = questType;
         if (this.getMinParty() == 1 && this.getMaxParty() == 1) {
            this._entryType = SpecialBypassManager.BypassTemplate.BypassEntryType.SOLO;
         } else if (this.getMinParty() == 1 && this.getMaxParty() <= 9) {
            this._entryType = SpecialBypassManager.BypassTemplate.BypassEntryType.SOLO_PARTY;
         } else if (this.getMinParty() > 1 && this.getMaxParty() <= 9) {
            this._entryType = SpecialBypassManager.BypassTemplate.BypassEntryType.PARTY;
         } else if (this.getMinParty() < 9 && this.getMaxParty() > 9) {
            this._entryType = SpecialBypassManager.BypassTemplate.BypassEntryType.PARTY_COMMAND_CHANNEL;
         } else if (this.getMinParty() >= 9 && this.getMaxParty() > 9) {
            this._entryType = SpecialBypassManager.BypassTemplate.BypassEntryType.COMMAND_CHANNEL;
         }

         if (this._entryType == null) {
            throw new IllegalArgumentException("Invalid type for special bypass: " + this._id);
         }
      }

      public int getId() {
         return this._id;
      }

      public boolean isDispelBuffs() {
         return this._dispelBuffs;
      }

      public int getMinLevel() {
         return this._minLevel;
      }

      public int getMaxLevel() {
         return this._maxLevel;
      }

      public int getMinParty() {
         return this._minParty;
      }

      public int getMaxParty() {
         return this._maxParty;
      }

      public Location getTeleportCoord() {
         if (this._teleportCoords != null && !this._teleportCoords.isEmpty()) {
            return this._teleportCoords.size() == 1 ? this._teleportCoords.get(0) : this._teleportCoords.get(Rnd.get(this._teleportCoords.size()));
         } else {
            return null;
         }
      }

      public void setNewTeleportCoords(Location loc) {
         this._teleportCoords.clear();
         this._teleportCoords.add(loc);
      }

      public int getRemovedItemId() {
         return this._removedItemId;
      }

      public int getRemovedItemCount() {
         return this._removedItemCount;
      }

      public boolean getRemovedItemNecessity() {
         return this._removedItemNecessity;
      }

      public SpecialBypassManager.BypassTemplate.BypassRemoveType getRemoveType() {
         return this._removeType;
      }

      public int getGiveItemId() {
         return this._giveItemId;
      }

      public int getGiveItemCount() {
         return this._givedItemCount;
      }

      public SpecialBypassManager.BypassTemplate.BypassEntryType getEntryType() {
         return this._entryType;
      }

      public List<Location> getTeleportCoords() {
         return this._teleportCoords;
      }

      public String getRequiredQuest() {
         return this._requiredQuest;
      }

      public SpecialBypassManager.BypassTemplate.BypassQuestType getQuestType() {
         return this._questType;
      }

      public static enum BypassEntryType {
         SOLO,
         SOLO_PARTY,
         PARTY,
         EVENT,
         PARTY_COMMAND_CHANNEL,
         COMMAND_CHANNEL;
      }

      public static enum BypassQuestType {
         STARTED,
         COMPLETED;
      }

      public static enum BypassRemoveType {
         NONE,
         LEADER,
         ALL;
      }
   }

   private static class SingletonHolder {
      protected static final SpecialBypassManager _instance = new SpecialBypassManager();
   }

   public class SpecialBypass extends Quest {
      public SpecialBypass(int id, String name, String descr) {
         super(id, name, descr);

         for(int npcId : SpecialBypassManager.this._npcTemplates.keys()) {
            this.addStartNpc(npcId);
            this.addTalkId(npcId);
         }
      }

      @Override
      public String onAdvEvent(String event, Npc npc, Player player) {
         if (event.equalsIgnoreCase("teleport")) {
            SpecialBypassManager.BypassTemplate template = SpecialBypassManager.this.getNpcBypass(npc.getId());
            if (template != null) {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               switch(npc.getId()) {
                  case 13001:
                     if (EpicBossManager.getInstance().getBossStatus(29068) == 3) {
                        html.setFile(player, "data/scripts/teleports/GrandBossTeleporters/" + player.getLang() + "/13001-01.htm");
                        player.sendPacket(html);
                        return null;
                     }

                     if (EpicBossManager.getInstance().getBossStatus(29068) == 2) {
                        html.setFile(player, "data/scripts/teleports/GrandBossTeleporters/" + player.getLang() + "/13001-02.htm");
                        player.sendPacket(html);
                        return null;
                     }
                     break;
                  case 31385:
                     if (EpicBossManager.getInstance().getBossStatus(29028) == 2 || EpicBossManager.getInstance().getBossStatus(29028) == 3) {
                        html.setFile(player, "data/scripts/teleports/GrandBossTeleporters/" + player.getLang() + "/31385-04.htm");
                        player.sendPacket(html);
                        return null;
                     }
                     break;
                  case 31862:
                     if (EpicBossManager.getInstance().getBossStatus(29020) == 3) {
                        return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "Baium.DEAD") + "</body></html>";
                     }

                     if (EpicBossManager.getInstance().getBossStatus(29020) == 2) {
                        return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "Baium.IN_FIGHT") + "</body></html>";
                     }
                     break;
                  case 32376:
                     if (EpicBossManager.getInstance().getBossStatus(29118) == 3) {
                        html.setFile(player, "data/scripts/teleports/SteelCitadelTeleport/" + player.getLang() + "/32376-02.htm");
                        player.sendPacket(html);
                        return null;
                     }
               }

               if (SpecialBypassManager.this.checkConditions(player, template)) {
                  SpecialBypassManager.this.onTeleport(player, template);
                  switch(npc.getId()) {
                     case 13001:
                        if (EpicBossManager.getInstance().getBossStatus(29068) == 0) {
                           GrandBossInstance antharas = EpicBossManager.getInstance().getBoss(29068);
                           QuestManager.getInstance().getQuest(Antharas.class.getSimpleName()).notifyEvent("waiting", antharas, player);
                        }
                        break;
                     case 31385:
                        if (EpicBossManager.getInstance().getBossStatus(29028) == 0) {
                           ValakasManager.setValakasSpawnTask();
                        }
                  }
               }
            }
         }

         return null;
      }
   }
}
