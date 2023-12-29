package l2e.gameserver.network.serverpackets;

public class ExEnchantSkillResult extends GameServerPacket {
   private static final ExEnchantSkillResult STATIC_PACKET_TRUE = new ExEnchantSkillResult(true);
   private static final ExEnchantSkillResult STATIC_PACKET_FALSE = new ExEnchantSkillResult(false);
   private final boolean _enchanted;

   public static final ExEnchantSkillResult valueOf(boolean result) {
      return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
   }

   public ExEnchantSkillResult(boolean enchanted) {
      this._enchanted = enchanted;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._enchanted ? 1 : 0);
   }
}
