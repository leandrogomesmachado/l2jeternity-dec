package l2e.gameserver.handler.skillhandlers.impl;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.FishingZone;
import l2e.gameserver.model.zone.type.HotSpringZone;
import l2e.gameserver.model.zone.type.WaterZone;
import l2e.gameserver.network.SystemMessageId;

public class Fishing implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.FISHING};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer() && activeChar.getSkillLevel(1315) != -1) {
         Player player = activeChar.getActingPlayer();
         if (!Config.ALLOWFISHING && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS)) {
            player.sendMessage("Fishing server is currently offline");
         } else if (player.isFishing()) {
            if (player.getFishCombat() != null) {
               player.getFishCombat().doDie(false);
            } else {
               player.endFishing(false);
            }

            player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
         } else {
            Weapon weaponItem = player.getActiveWeaponItem();
            if (weaponItem != null && weaponItem.getItemType() == WeaponType.FISHINGROD) {
               ItemInstance lure = player.getInventory().getPaperdollItem(7);
               if (lure == null) {
                  player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
               } else {
                  player.setLure(lure);
                  ItemInstance lure2 = player.getInventory().getPaperdollItem(7);
                  if (lure2 != null && lure2.getCount() >= 1L) {
                     if (!player.isGM()) {
                        if (player.isInBoat()) {
                           player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
                           return;
                        }

                        if (player.isInCraftMode() || player.isInStoreMode()) {
                           player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
                           return;
                        }

                        if (player.isInWater()) {
                           player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
                           return;
                        }

                        if (player.isInsideZone(ZoneId.PEACE)) {
                           player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
                           return;
                        }
                     }

                     boolean isHotSpringZone = false;
                     int distance = Rnd.get(90, 250);
                     double angle = Util.convertHeadingToDegree(player.getHeading());
                     double radian = Math.toRadians(angle);
                     double sin = Math.sin(radian);
                     double cos = Math.cos(radian);
                     int baitX = player.getX() + (int)(cos * (double)distance);
                     int baitY = player.getY() + (int)(sin * (double)distance);
                     FishingZone fishZone = null;
                     WaterZone water = null;
                     HotSpringZone hszone = null;

                     for(ZoneType zone : ZoneManager.getInstance().isInsideZone(baitX, baitY)) {
                        if (zone instanceof FishingZone) {
                           fishZone = (FishingZone)zone;
                        } else if (zone instanceof WaterZone) {
                           water = (WaterZone)zone;
                        } else if (zone instanceof HotSpringZone) {
                           hszone = (HotSpringZone)zone;
                           isHotSpringZone = true;
                        }

                        if (fishZone != null && water != null && hszone != null) {
                           break;
                        }
                     }

                     int baitZ = computeBaitZ(player, baitX, baitY, fishZone, water, hszone);
                     if (baitZ == Integer.MIN_VALUE) {
                        isHotSpringZone = false;

                        for(int var26 = 250; var26 >= 90; --var26) {
                           baitX = (int)((double)player.getX() + cos * (double)var26);
                           baitY = (int)((double)player.getY() + sin * (double)var26);
                           fishZone = null;
                           water = null;
                           hszone = null;

                           for(ZoneType zone : ZoneManager.getInstance().isInsideZone(baitX, baitY)) {
                              if (zone instanceof FishingZone) {
                                 fishZone = (FishingZone)zone;
                              } else if (zone instanceof WaterZone) {
                                 water = (WaterZone)zone;
                              } else if (zone instanceof HotSpringZone) {
                                 hszone = (HotSpringZone)zone;
                                 isHotSpringZone = true;
                              }

                              if (fishZone != null && water != null && hszone != null) {
                                 break;
                              }
                           }

                           baitZ = computeBaitZ(player, baitX, baitY, fishZone, water, hszone);
                           if (baitZ != Integer.MIN_VALUE) {
                              break;
                           }
                        }

                        if (baitZ == Integer.MIN_VALUE) {
                           player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
                           return;
                        }
                     }

                     if (!player.destroyItem("Fishing", player.getInventory().getPaperdollObjectId(7), 1L, null, false)) {
                        player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
                     } else {
                        player.startFishing(baitX, baitY, baitZ, isHotSpringZone);
                     }
                  } else {
                     player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
                  }
               }
            } else {
               player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
            }
         }
      }
   }

   private static int computeBaitZ(Player player, int baitX, int baitY, FishingZone fishingZone, WaterZone waterZone, HotSpringZone hszone) {
      if (fishingZone == null && waterZone == null) {
         return Integer.MIN_VALUE;
      } else if (fishingZone == null && hszone == null) {
         return Integer.MIN_VALUE;
      } else {
         int baitZ = 0;
         if (waterZone != null) {
            baitZ = waterZone.getWaterZ();
         } else if (fishingZone != null) {
            baitZ = fishingZone.getWaterZ();
         } else if (hszone != null) {
            baitZ = hszone.getWaterZ();
         }

         if (baitZ == 0) {
            return Integer.MIN_VALUE;
         } else if (!GeoEngine.canSeeTarget(player.getX(), player.getY(), player.getZ(), baitX, baitY, baitZ)) {
            return Integer.MIN_VALUE;
         } else {
            if (GeoEngine.hasGeo(baitX, baitY, player.getGeoIndex())) {
               if (GeoEngine.getHeight(baitX, baitY, baitZ, player.getGeoIndex()) > baitZ) {
                  return Integer.MIN_VALUE;
               }

               if (GeoEngine.getHeight(baitX, baitY, player.getZ(), player.getGeoIndex()) > baitZ) {
                  return Integer.MIN_VALUE;
               }
            }

            return baitZ;
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
