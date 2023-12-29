package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ConfirmDlg;

public class Escape extends Effect {
   private final TeleportWhereType _escapeType;

   public Escape(Env env, EffectTemplate template) {
      super(env, template);
      this._escapeType = template.getParameters() == null ? null : template.getParameters().getEnum("escapeType", TeleportWhereType.class, null);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.TELEPORT;
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public boolean onStart() {
      if ((this._escapeType != null || this.getSkill().getId() == 2588)
         && (this.getSkill().getId() != 2588 || !this.getEffected().isPlayer() || this.getEffected().getActingPlayer().getBookmarkLocation() != null)) {
         if (this.getSkill().getId() == 1255 && this.getEffected().isPlayer() && this.getEffected().getActingPlayer().getBlockPartyRecall()) {
            this.getEffected().addScript(new SummonRequestHolder(this.getEffector().getActingPlayer(), this.getSkill(), false));
            ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
            confirm.addCharName(this.getEffector());
            Location loc = MapRegionManager.getInstance().getTeleToLocation(this.getEffected(), this._escapeType);
            confirm.addZoneName(loc.getX(), loc.getY(), loc.getZ());
            confirm.addTime(30000);
            confirm.addRequesterId(this.getEffector().getObjectId());
            this.getEffected().sendPacket(confirm);
         } else {
            if (this.getEffected().getReflectionId() != 0) {
               this.getEffected().setReflectionId(0);
            }

            if (this.getSkill().getId() == 2588 && this.getEffected().isPlayer()) {
               this.getEffected().teleToLocation(this.getEffected().getActingPlayer().getBookmarkLocation(), false);
               this.getEffected().getActingPlayer().setBookmarkLocation(null);
            } else {
               this.getEffected().teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this.getEffected(), this._escapeType), true);
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
