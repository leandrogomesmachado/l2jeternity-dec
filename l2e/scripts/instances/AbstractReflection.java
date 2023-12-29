package l2e.scripts.instances;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.ReflectionReenterTimeHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.zone.type.ReflectionZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.AbstractNpcAI;

public abstract class AbstractReflection extends AbstractNpcAI {
   protected static final Logger _log = Logger.getLogger(AbstractReflection.class.getName());

   public AbstractReflection(String var1, String var2) {
      super(var1, var2);
   }

   protected abstract void onTeleportEnter(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4);

   protected boolean enterInstance(Player var1, Npc var2, ReflectionWorld var3, int var4) {
      ReflectionTemplate var5 = ReflectionParser.getInstance().getReflectionId(var4);
      if (var5 != null) {
         ReflectionWorld var6 = ReflectionManager.getInstance().getPlayerWorld(var1);
         if (var6 != null) {
            if (var6.getTemplateId() != var4) {
               var1.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
               return false;
            }

            if (var1.isInParty() && var1.getParty().isLeader(var1) && var1.getVarB("autoTeleport@", false)) {
               for(Player var11 : var1.getParty().getMembers()) {
                  if (var11 != null
                     && var11.getObjectId() != var1.getObjectId()
                     && Util.checkIfInRange(1000, var1, var11, true)
                     && BotFunctions.checkCondition(var11, false)
                     && var11.getIPAddress().equalsIgnoreCase(var1.getIPAddress())) {
                     ReflectionWorld var9 = ReflectionManager.getInstance().getPlayerWorld(var11);
                     if (var9 != null && var9.getTemplateId() == var4) {
                        this.onTeleportEnter(var11, var5, var6, false);
                        if (var5.isDispelBuffs()) {
                           this.handleRemoveBuffs(var11);
                        }
                     }
                  }
               }
            }

            if (this.checkReenterConditions(var1, var2, var5)) {
               this.onTeleportEnter(var1, var5, var6, false);
               if (var5.isDispelBuffs()) {
                  this.handleRemoveBuffs(var1);
               }
            }

            return false;
         }

         if (this.checkConditions(var1, var2, var5)) {
            Reflection var7 = ReflectionManager.getInstance().createDynamicReflection(var5);
            var3.setReflection(var7);
            var3.setTemplateId(var4);
            var3.setStatus(0);
            ReflectionManager.getInstance().addWorld(var3);
            Reflection var8 = ReflectionManager.getInstance().getReflection(var3.getReflectionId());
            if (var8.getReturnLoc() == null) {
               var8.setReturnLoc(var1.getLocation());
            }

            switch(var5.getEntryType()) {
               case PARTY_COMMAND_CHANNEL:
               case COMMAND_CHANNEL:
                  if (var1.getParty() != null && var1.getParty().getCommandChannel() != null) {
                     var1.getParty().getCommandChannel().setReflectionId(var8.getId());
                  }
               default:
                  this.onEnterInstance(var1, var5, var3, true);
                  if (var5.getReuseUponEntry()) {
                     this.handleReenterTime(var3);
                  }

                  if (var5.isDispelBuffs()) {
                     this.handleRemoveBuffs(var3);
                  }

                  return true;
            }
         }
      }

      return false;
   }

   protected void handleRemoveBuffs(ReflectionWorld var1) {
      for(int var3 : var1.getAllowed()) {
         Player var4 = World.getInstance().getPlayer(var3);
         if (var4 != null) {
            this.handleRemoveBuffs(var4);
         }
      }
   }

   protected void onEnterInstance(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4) {
      switch(var2.getEntryType()) {
         case PARTY_COMMAND_CHANNEL:
            if (var1.getParty() != null && var1.getParty().getCommandChannel() != null) {
               this.doCommandChannelEnter(var1, var2, var3, var4);
            } else {
               this.doPartyEnter(var1, var2, var3, var4);
            }
            break;
         case COMMAND_CHANNEL:
            this.doCommandChannelEnter(var1, var2, var3, var4);
            break;
         case SOLO:
            this.doSoloEnter(var1, var2, var3, var4);
            break;
         case SOLO_PARTY:
            if (var1.getParty() == null) {
               this.doSoloEnter(var1, var2, var3, var4);
            } else {
               this.doPartyEnter(var1, var2, var3, var4);
            }
            break;
         case PARTY:
            this.doPartyEnter(var1, var2, var3, var4);
      }
   }

   private void doSoloEnter(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4) {
      if (var2.getRemovedItemNecessity()) {
         if (var2.getRemovedItemId() == -100) {
            var1.setPcBangPoints(var1.getPcBangPoints() - var2.getRemovedItemCount());
            SystemMessage var5 = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
            var5.addNumber(var2.getRemovedItemCount());
            var1.sendPacket(var5);
            var1.sendPacket(new ExPCCafePointInfo(var1.getPcBangPoints(), var2.getRemovedItemCount(), false, false, 1));
         } else if (var2.getRemovedItemId() == -200) {
            var1.getClan().takeReputationScore(var2.getRemovedItemCount(), true);
            SystemMessage var6 = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
            var6.addItemNumber((long)var2.getRemovedItemCount());
            var1.sendPacket(var6);
         } else if (var2.getRemovedItemId() == -300) {
            var1.setFame(var1.getFame() - var2.getRemovedItemCount());
            var1.sendUserInfo();
         } else if (var2.getRemovedItemId() > 0) {
            var1.destroyItemByItemId("Instance Check", var2.getRemovedItemId(), (long)var2.getRemovedItemCount(), var1, true);
         }
      }

      if (var2.getGiveItemId() > 0) {
         var1.addItem("Instance reward", var2.getGiveItemId(), (long)var2.getGiveItemCount(), null, true);
      }

      this.onTeleportEnter(var1, var2, var3, var4);
   }

   private void doPartyEnter(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4) {
      if (var2.getRemovedItemId() > 0 && var2.getRemovedItemNecessity() && var2.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.LEADER) {
         var1.destroyItemByItemId("Instance Check", var2.getRemovedItemId(), (long)var2.getRemovedItemCount(), var1, true);
      }

      for(Player var6 : var1.getParty().getMembers()) {
         if (var6 != null) {
            if (var2.getRemovedItemNecessity() && var2.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.ALL) {
               if (var2.getRemovedItemId() == -100) {
                  var6.setPcBangPoints(var6.getPcBangPoints() - var2.getRemovedItemCount());
                  SystemMessage var7 = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  var7.addNumber(var2.getRemovedItemCount());
                  var6.sendPacket(var7);
                  var6.sendPacket(new ExPCCafePointInfo(var6.getPcBangPoints(), var2.getRemovedItemCount(), false, false, 1));
               } else if (var2.getRemovedItemId() == -200) {
                  var6.getClan().takeReputationScore(var2.getRemovedItemCount(), true);
                  SystemMessage var8 = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                  var8.addItemNumber((long)var2.getRemovedItemCount());
                  var6.sendPacket(var8);
               } else if (var2.getRemovedItemId() == -300) {
                  var6.setFame(var6.getFame() - var2.getRemovedItemCount());
                  var6.sendUserInfo();
               } else if (var2.getRemovedItemId() > 0) {
                  var6.destroyItemByItemId("Instance Check", var2.getRemovedItemId(), (long)var2.getRemovedItemCount(), var6, true);
               }
            }

            if (var2.getGiveItemId() > 0) {
               var6.addItem("Instance reward", var2.getGiveItemId(), (long)var2.getGiveItemCount(), null, true);
            }

            this.onTeleportEnter(var6, var2, var3, var4);
         }
      }
   }

   private void doCommandChannelEnter(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4) {
      if (var2.getRemovedItemId() > 0 && var2.getRemovedItemNecessity() && var2.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.LEADER) {
         var1.destroyItemByItemId("Instance Check", var2.getRemovedItemId(), (long)var2.getRemovedItemCount(), var1, true);
      }

      for(Player var6 : var1.getParty().getCommandChannel().getMembers()) {
         if (var6 != null) {
            if (var2.getRemovedItemNecessity() && var2.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.ALL) {
               if (var2.getRemovedItemId() == -100) {
                  var6.setPcBangPoints(var6.getPcBangPoints() - var2.getRemovedItemCount());
                  SystemMessage var7 = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  var7.addNumber(var2.getRemovedItemCount());
                  var6.sendPacket(var7);
                  var6.sendPacket(new ExPCCafePointInfo(var6.getPcBangPoints(), var2.getRemovedItemCount(), false, false, 1));
               } else if (var2.getRemovedItemId() == -200) {
                  var6.getClan().takeReputationScore(var2.getRemovedItemCount(), true);
                  SystemMessage var8 = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
                  var8.addItemNumber((long)var2.getRemovedItemCount());
                  var6.sendPacket(var8);
               } else if (var2.getRemovedItemId() == -300) {
                  var6.setFame(var6.getFame() - var2.getRemovedItemCount());
                  var6.sendUserInfo();
               } else if (var2.getRemovedItemId() > 0) {
                  var6.destroyItemByItemId("Instance Check", var2.getRemovedItemId(), (long)var2.getRemovedItemCount(), var6, true);
               }
            }

            if (var2.getGiveItemId() > 0) {
               var6.addItem("Instance reward", var2.getGiveItemId(), (long)var2.getGiveItemCount(), null, true);
            }

            this.onTeleportEnter(var6, var2, var3, var4);
         }
      }
   }

   protected boolean checkReenterConditions(Player var1, Npc var2, ReflectionTemplate var3) {
      if (!var1.isCursedWeaponEquipped() && !var1.isFlying()) {
         switch(var3.getEntryType()) {
            case PARTY_COMMAND_CHANNEL:
               if (var1.getParty() != null && var1.getParty().getCommandChannel() != null) {
                  return this.checkReenterCommandChannelType(var1, var2, var3);
               }

               return this.checkReenterPartyType(var1, var2, var3);
            case COMMAND_CHANNEL:
               return this.checkReenterCommandChannelType(var1, var2, var3);
            case SOLO:
            default:
               return true;
            case SOLO_PARTY:
               if (var1.getParty() == null) {
                  return true;
               }

               return this.checkReenterPartyType(var1, var2, var3);
            case PARTY:
               return this.checkReenterPartyType(var1, var2, var3);
         }
      } else {
         var1.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
         return false;
      }
   }

   protected boolean checkConditions(Player var1, Npc var2, ReflectionTemplate var3) {
      if (ReflectionManager.getInstance().getCountByIzId(var3.getId()) >= var3.getMaxChannels()) {
         var1.sendPacket(SystemMessageId.MAXIMUM_INSTANCE_ZONE_NUMBER_EXCEEDED_CANT_ENTER);
         return false;
      } else if (!var1.isCursedWeaponEquipped() && !var1.isFlying()) {
         switch(var3.getEntryType()) {
            case PARTY_COMMAND_CHANNEL:
               if (var1.getParty() != null && var1.getParty().getCommandChannel() != null) {
                  return this.checkCommandChannelType(var1, var2, var3);
               }

               return this.checkPartyType(var1, var2, var3);
            case COMMAND_CHANNEL:
               return this.checkCommandChannelType(var1, var2, var3);
            case SOLO:
               return this.checkSoloType(var1, var2, var3);
            case SOLO_PARTY:
               if (var1.getParty() == null) {
                  return this.checkSoloType(var1, var2, var3);
               }

               return this.checkPartyType(var1, var2, var3);
            case PARTY:
               return this.checkPartyType(var1, var2, var3);
            default:
               return true;
         }
      } else {
         var1.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
         return false;
      }
   }

   protected boolean checkSoloType(Player var1, Npc var2, ReflectionTemplate var3) {
      if (!this.isNotCheckTimeRef(var3.getId()) && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(var3.getId(), var1)) {
         SystemMessage var12 = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
         var12.addPcName(var1);
         var1.sendPacket(var12);
         return false;
      } else if (var1.getLevel() < var3.getMinLevel() || var1.getLevel() > var3.getMaxLevel()) {
         SystemMessage var11 = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
         var11.addPcName(var1);
         var1.sendPacket(var11);
         return false;
      } else if (var1.isInParty()) {
         var1.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
         return false;
      } else {
         if (var3.getRemovedItemId() == -100) {
            if (var3.getRemovedItemId() == -100 && var1.getPcBangPoints() < var3.getRemovedItemCount()) {
               SystemMessage var4 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               var4.addPcName(var1);
               var1.sendPacket(var4);
               return false;
            }
         } else if (var3.getRemovedItemId() == -200) {
            if (var3.getRemovedItemId() == -200 && (var1.getClan() == null || var1.getClan().getReputationScore() < var3.getRemovedItemCount())) {
               SystemMessage var7 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               var7.addPcName(var1);
               var1.sendPacket(var7);
               return false;
            }
         } else if (var3.getRemovedItemId() == -300) {
            if (var3.getRemovedItemId() == -300 && var1.getFame() < var3.getRemovedItemCount()) {
               SystemMessage var8 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               var8.addPcName(var1);
               var1.sendPacket(var8);
               return false;
            }
         } else if (var3.getRemovedItemId() > 0
            && (
               var1.getInventory().getItemByItemId(var3.getRemovedItemId()) == null
                  || var1.getInventory().getItemByItemId(var3.getRemovedItemId()).getCount() < (long)var3.getRemovedItemCount()
            )) {
            SystemMessage var10 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
            var10.addPcName(var1);
            var1.sendPacket(var10);
            return false;
         }

         if (var3.getRequiredQuest() != null) {
            QuestState var9 = var1.getQuestState(var3.getRequiredQuest());
            boolean var5 = false;
            if (var3.getQuestType() == ReflectionTemplate.ReflectionQuestType.STARTED) {
               if (var9 == null || !var9.isStarted()) {
                  var5 = true;
               }
            } else if (var9 == null || !var9.isCompleted()) {
               var5 = true;
            }

            if (var5) {
               SystemMessage var6 = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
               var6.addPcName(var1);
               var1.sendPacket(var6);
               return false;
            }
         }

         return true;
      }
   }

   protected boolean checkPartyType(Player var1, Npc var2, ReflectionTemplate var3) {
      Party var4 = var1.getParty();
      if (var4 == null) {
         var1.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
         return false;
      } else if (var4.getLeader() != var1) {
         var1.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
         return false;
      } else if (var4.getMemberCount() < var3.getMinParty()) {
         SystemMessage var14 = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
         var14.addNumber(var3.getMinParty());
         var1.sendPacket(var14);
         return false;
      } else if (var4.getMemberCount() > var3.getMaxParty()) {
         var1.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
         return false;
      } else {
         if (var3.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.LEADER) {
            if (var3.getRemovedItemId() == -100) {
               if (var3.getRemovedItemId() == -100 && var1.getPcBangPoints() < var3.getRemovedItemCount()) {
                  SystemMessage var5 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  var5.addPcName(var1);
                  var1.sendPacket(var5);
                  return false;
               }
            } else if (var3.getRemovedItemId() == -200) {
               if (var3.getRemovedItemId() == -200 && (var1.getClan() == null || var1.getClan().getReputationScore() < var3.getRemovedItemCount())) {
                  SystemMessage var10 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  var10.addPcName(var1);
                  var1.sendPacket(var10);
                  return false;
               }
            } else if (var3.getRemovedItemId() == -300) {
               if (var3.getRemovedItemId() == -300 && var1.getFame() < var3.getRemovedItemCount()) {
                  SystemMessage var11 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  var11.addPcName(var1);
                  var1.sendPacket(var11);
                  return false;
               }
            } else if (var3.getRemovedItemId() > 0
               && (
                  var1.getInventory().getItemByItemId(var3.getRemovedItemId()) == null
                     || var1.getInventory().getItemByItemId(var3.getRemovedItemId()).getCount() < (long)var3.getRemovedItemCount()
               )) {
               SystemMessage var13 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               var13.addPcName(var1);
               var1.sendPacket(var13);
               return false;
            }
         }

         for(Player var6 : var4.getMembers()) {
            if (var6.getLevel() < var3.getMinLevel() || var6.getLevel() > var3.getMaxLevel()) {
               SystemMessage var7 = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
               var7.addPcName(var6);
               var4.broadCast(var7);
               return false;
            }

            if (!var6.isInsideRadius(var1, 500, true, true)) {
               SystemMessage var21 = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
               var21.addPcName(var6);
               var4.broadCast(var21);
               return false;
            }

            if (var3.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.ALL) {
               if (var3.getRemovedItemId() == -100) {
                  if (var3.getRemovedItemId() == -100 && var6.getPcBangPoints() < var3.getRemovedItemCount()) {
                     SystemMessage var15 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var15.addPcName(var6);
                     var4.broadCast(var15);
                     return false;
                  }
               } else if (var3.getRemovedItemId() == -200) {
                  if (var3.getRemovedItemId() == -200 && (var6.getClan() == null || var6.getClan().getReputationScore() < var3.getRemovedItemCount())) {
                     SystemMessage var16 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var16.addPcName(var6);
                     var4.broadCast(var16);
                     return false;
                  }
               } else if (var3.getRemovedItemId() == -300) {
                  if (var3.getRemovedItemId() == -300 && var6.getFame() < var3.getRemovedItemCount()) {
                     SystemMessage var17 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var17.addPcName(var6);
                     var4.broadCast(var17);
                     return false;
                  }
               } else if (var3.getRemovedItemId() > 0
                  && (
                     var6.getInventory().getItemByItemId(var3.getRemovedItemId()) == null
                        || var6.getInventory().getItemByItemId(var3.getRemovedItemId()).getCount() < (long)var3.getRemovedItemCount()
                  )) {
                  SystemMessage var20 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  var20.addPcName(var6);
                  var4.broadCast(var20);
                  return false;
               }
            }

            if (!this.isNotCheckTimeRef(var3.getId())
               && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(var3.getId(), var6)) {
               SystemMessage var19 = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
               var19.addPcName(var6);
               var4.broadCast(var19);
               return false;
            }

            if (var3.getRequiredQuest() != null) {
               QuestState var18 = var6.getQuestState(var3.getRequiredQuest());
               boolean var8 = false;
               if (var3.getQuestType() == ReflectionTemplate.ReflectionQuestType.STARTED) {
                  if (var18 == null || !var18.isStarted()) {
                     var8 = true;
                  }
               } else if (var18 == null || !var18.isCompleted()) {
                  var8 = true;
               }

               if (var8) {
                  SystemMessage var9 = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
                  var9.addPcName(var6);
                  var4.broadCast(var9);
                  return false;
               }
            }
         }

         return true;
      }
   }

   protected boolean checkReenterPartyType(Player var1, Npc var2, ReflectionTemplate var3) {
      Party var4 = var1.getParty();
      if (var4 == null) {
         var1.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
         return false;
      } else if (var4.getMemberCount() > var3.getMaxParty()) {
         var1.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
         return false;
      } else {
         return true;
      }
   }

   protected boolean checkCommandChannelType(Player var1, Npc var2, ReflectionTemplate var3) {
      Party var4 = var1.getParty();
      if (var4 != null && var4.getCommandChannel() != null) {
         CommandChannel var5 = var4.getCommandChannel();
         if (var5.getMemberCount() < var3.getMinParty()) {
            SystemMessage var15 = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER);
            var15.addNumber(var3.getMinParty());
            var1.sendPacket(var15);
            return false;
         } else if (var5.getMemberCount() > var3.getMaxParty()) {
            var1.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
            return false;
         } else {
            if (var3.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.LEADER) {
               if (var3.getRemovedItemId() == -100) {
                  if (var3.getRemovedItemId() == -100 && var1.getPcBangPoints() < var3.getRemovedItemCount()) {
                     SystemMessage var6 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var6.addPcName(var1);
                     var1.sendPacket(var6);
                     return false;
                  }
               } else if (var3.getRemovedItemId() == -200) {
                  if (var3.getRemovedItemId() == -200 && (var1.getClan() == null || var1.getClan().getReputationScore() < var3.getRemovedItemCount())) {
                     SystemMessage var11 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var11.addPcName(var1);
                     var1.sendPacket(var11);
                     return false;
                  }
               } else if (var3.getRemovedItemId() == -300) {
                  if (var3.getRemovedItemId() == -300 && var1.getFame() < var3.getRemovedItemCount()) {
                     SystemMessage var12 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var12.addPcName(var1);
                     var1.sendPacket(var12);
                     return false;
                  }
               } else if (var3.getRemovedItemId() > 0
                  && (
                     var1.getInventory().getItemByItemId(var3.getRemovedItemId()) == null
                        || var1.getInventory().getItemByItemId(var3.getRemovedItemId()).getCount() < (long)var3.getRemovedItemCount()
                  )) {
                  SystemMessage var14 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                  var14.addPcName(var1);
                  var1.sendPacket(var14);
                  return false;
               }
            }

            for(Player var7 : var5.getMembers()) {
               if (var7.getLevel() < var3.getMinLevel() || var7.getLevel() > var3.getMaxLevel()) {
                  SystemMessage var8 = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
                  var8.addPcName(var7);
                  var5.broadCast(var8);
                  return false;
               }

               if (!Util.checkIfInRange(1000, var1, var7, true)) {
                  SystemMessage var22 = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
                  var22.addPcName(var7);
                  var5.broadCast(var22);
                  return false;
               }

               if (var3.getRemoveType() == ReflectionTemplate.ReflectionRemoveType.ALL) {
                  if (var3.getRemovedItemId() == -100) {
                     if (var3.getRemovedItemId() == -100 && var7.getPcBangPoints() < var3.getRemovedItemCount()) {
                        SystemMessage var16 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        var16.addPcName(var7);
                        var5.broadCast(var16);
                        return false;
                     }
                  } else if (var3.getRemovedItemId() == -200) {
                     if (var3.getRemovedItemId() == -200 && (var7.getClan() == null || var7.getClan().getReputationScore() < var3.getRemovedItemCount())) {
                        SystemMessage var17 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        var17.addPcName(var7);
                        var5.broadCast(var17);
                        return false;
                     }
                  } else if (var3.getRemovedItemId() == -300) {
                     if (var3.getRemovedItemId() == -300 && var7.getFame() < var3.getRemovedItemCount()) {
                        SystemMessage var18 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                        var18.addPcName(var7);
                        var5.broadCast(var18);
                        return false;
                     }
                  } else if (var3.getRemovedItemId() > 0
                     && (
                        var7.getInventory().getItemByItemId(var3.getRemovedItemId()) == null
                           || var7.getInventory().getItemByItemId(var3.getRemovedItemId()).getCount() < (long)var3.getRemovedItemCount()
                     )) {
                     SystemMessage var21 = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
                     var21.addPcName(var7);
                     var5.broadCast(var21);
                     return false;
                  }
               }

               if (!this.isNotCheckTimeRef(var3.getId())
                  && System.currentTimeMillis() < ReflectionParser.getInstance().getMinutesToNextEntrance(var3.getId(), var7)) {
                  SystemMessage var20 = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
                  var20.addPcName(var7);
                  var5.broadCast(var20);
                  return false;
               }

               if (var3.getRequiredQuest() != null) {
                  QuestState var19 = var7.getQuestState(var3.getRequiredQuest());
                  boolean var9 = false;
                  if (var3.getQuestType() == ReflectionTemplate.ReflectionQuestType.STARTED) {
                     if (var19 == null || !var19.isStarted()) {
                        var9 = true;
                     }
                  } else if (var19 == null || !var19.isCompleted()) {
                     var9 = true;
                  }

                  if (var9) {
                     SystemMessage var10 = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
                     var10.addPcName(var7);
                     var5.broadCast(var10);
                     return false;
                  }
               }
            }

            return true;
         }
      } else {
         var1.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
         return false;
      }
   }

   protected boolean checkReenterCommandChannelType(Player var1, Npc var2, ReflectionTemplate var3) {
      Party var4 = var1.getParty();
      if (var4 != null && var4.getCommandChannel() != null) {
         CommandChannel var5 = var4.getCommandChannel();
         if (var5.getMemberCount() > var3.getMaxParty()) {
            var1.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
            return false;
         } else {
            return true;
         }
      } else {
         var1.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
         return false;
      }
   }

   protected void handleRemoveBuffs(Player var1) {
      var1.stopAllEffectsExceptThoseThatLastThroughDeath();
      Summon var2 = var1.getSummon();
      if (var2 != null) {
         var2.stopAllEffectsExceptThoseThatLastThroughDeath();
      }
   }

   protected void finishInstance(ReflectionWorld var1, boolean var2) {
      this.finishInstance(var1, 300000, var2);
   }

   protected void finishInstance(ReflectionWorld var1, int var2, boolean var3) {
      Reflection var4 = ReflectionManager.getInstance().getReflection(var1.getReflectionId());
      if (!var4.getReuseUponEntry() && var3) {
         this.handleReenterTime(var1);
      }

      if (var2 == 0) {
         ReflectionManager.getInstance().destroyReflection(var4.getId());
      } else if (var2 > 0) {
         var4.setDuration(var2);
         var4.setEmptyDestroyTime(0L);
      }
   }

   protected void handleReenterTime(ReflectionWorld var1) {
      Reflection var2 = ReflectionManager.getInstance().getReflection(var1.getReflectionId());
      List var3 = var2.getReenterData();
      long var4 = -1L;

      for(ReflectionReenterTimeHolder var7 : var3) {
         if (var7.getTime() > 0L) {
            var4 = System.currentTimeMillis() + var7.getTime();
            break;
         }

         Calendar var8 = Calendar.getInstance();
         var8.set(9, var7.getHour() >= 12 ? 1 : 0);
         var8.set(10, var7.getHour());
         var8.set(12, var7.getMinute());
         var8.set(13, 0);
         if (var8.getTimeInMillis() <= System.currentTimeMillis()) {
            var8.add(5, 1);
         }

         if (var7.getDay() != null) {
            while(var8.get(7) != Math.min(var7.getDay().getValue() + 1, 7)) {
               var8.add(5, 1);
            }
         }

         if (var4 == -1L) {
            var4 = var8.getTimeInMillis();
         } else if (var8.getTimeInMillis() < var4) {
            var4 = var8.getTimeInMillis();
         }
      }

      if (var4 > 0L) {
         this.setReenterTime(var1, var4);
      }
   }

   protected void setReenterTime(ReflectionWorld var1, long var2) {
      for(int var5 : var1.getAllowed()) {
         ReflectionManager.getInstance().setReflectionTime(var5, var1.getTemplateId(), var2);
         Player var6 = World.getInstance().getPlayer(var5);
         if (var6 != null && var6.isOnline()) {
            var6.sendPacket(
               SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED)
                  .addString(ReflectionManager.getInstance().getReflectionName(var6, var1.getTemplateId()))
            );
         }
      }
   }

   public void getActivatedZone(Reflection var1, int var2, boolean var3) {
      ReflectionZone var4 = ZoneManager.getInstance().getZoneById(var2, ReflectionZone.class);
      if (var4 != null) {
         if (var3) {
            var4.addRef(var1.getId());
            var1.addZone(var4.getId());
         } else {
            var4.removeRef(var1.getId());
            var1.removeZone(var4.getId());
         }
      }
   }

   private boolean isNotCheckTimeRef(int var1) {
      return var1 == 127 || var1 == 128 || var1 == 129 || var1 == 130;
   }
}
