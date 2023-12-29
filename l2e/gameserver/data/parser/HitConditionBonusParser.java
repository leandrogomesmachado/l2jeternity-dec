package l2e.gameserver.data.parser;

import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.Creature;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class HitConditionBonusParser extends DocumentParser {
   private int frontBonus = 0;
   private int sideBonus = 0;
   private int backBonus = 0;
   private int highBonus = 0;
   private int lowBonus = 0;
   private int darkBonus = 0;
   private int rainBonus = 0;

   protected HitConditionBonusParser() {
      this.load();
   }

   @Override
   public void load() {
      this.parseDatapackFile("data/stats/chars/hitConditionBonus.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded hit condition bonuses.");
      if (Config.DEBUG) {
         this._log.info(this.getClass().getSimpleName() + ": Front bonus: " + this.frontBonus);
         this._log.info(this.getClass().getSimpleName() + ": Side bonus: " + this.sideBonus);
         this._log.info(this.getClass().getSimpleName() + ": Back bonus: " + this.backBonus);
         this._log.info(this.getClass().getSimpleName() + ": High bonus: " + this.highBonus);
         this._log.info(this.getClass().getSimpleName() + ": Low bonus: " + this.lowBonus);
         this._log.info(this.getClass().getSimpleName() + ": Dark bonus: " + this.darkBonus);
         this._log.info(this.getClass().getSimpleName() + ": Rain bonus: " + this.rainBonus);
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Node n = this.getCurrentDocument().getFirstChild();

      for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
         NamedNodeMap attrs = d.getAttributes();
         String var4 = d.getNodeName();
         switch(var4) {
            case "front":
               this.frontBonus = parseInt(attrs, "val");
               break;
            case "side":
               this.sideBonus = parseInt(attrs, "val");
               break;
            case "back":
               this.backBonus = parseInt(attrs, "val");
               break;
            case "high":
               this.highBonus = parseInt(attrs, "val");
               break;
            case "low":
               this.lowBonus = parseInt(attrs, "val");
               break;
            case "dark":
               this.darkBonus = parseInt(attrs, "val");
               break;
            case "rain":
               this.rainBonus = parseInt(attrs, "val");
         }
      }
   }

   public double getConditionBonus(Creature attacker, Creature target) {
      double mod = 100.0;
      if (attacker.getZ() - target.getZ() > 50) {
         mod += (double)this.highBonus;
      } else if (attacker.getZ() - target.getZ() < -50) {
         mod += (double)this.lowBonus;
      }

      if (GameTimeController.getInstance().isNight()) {
         mod += (double)this.darkBonus;
      }

      if (attacker.isBehindTarget()) {
         mod += (double)this.backBonus;
      } else if (attacker.isInFrontOfTarget()) {
         mod += (double)this.frontBonus;
      } else {
         mod += (double)this.sideBonus;
      }

      return Math.max(mod / 100.0, 0.0);
   }

   public static HitConditionBonusParser getInstance() {
      return HitConditionBonusParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final HitConditionBonusParser _instance = new HitConditionBonusParser();
   }
}
