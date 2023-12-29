package l2e.gameserver.model.skills.effects;

import java.util.NoSuchElementException;

public enum AbnormalEffect {
   NONE("null", 0),
   BLEEDING("bleed", 1),
   POISON("poison", 2),
   REDCIRCLE("redcircle", 4),
   ICE("ice", 8),
   WIND("wind", 16),
   FEAR("fear", 32),
   STUN("stun", 64),
   SLEEP("sleep", 128),
   MUTED("mute", 256),
   ROOT("root", 512),
   HOLD_1("hold1", 1024),
   HOLD_2("hold2", 2048),
   UNKNOWN_13("unk13", 4096),
   BIG_HEAD("bighead", 8192),
   FLAME("flame", 16384),
   UNKNOWN_16("unknown16", 32768),
   GROW("grow", 65536),
   FLOATING_ROOT("floatroot", 131072),
   DANCE_STUNNED("dancestun", 262144),
   FIREROOT_STUN("firerootstun", 524288),
   STEALTH("stealth", 1048576),
   IMPRISIONING_1("imprison1", 2097152),
   IMPRISIONING_2("imprison2", 4194304),
   MAGIC_CIRCLE("magiccircle", 8388608),
   ICE2("ice2", 16777216),
   EARTHQUAKE("earthquake", 33554432),
   UNKNOWN_27("unknown27", 67108864),
   INVULNERABLE("invulnerable", 134217728),
   VITALITY("vitality", 268435456),
   REAL_TARGET("realtarget", 536870912),
   DEATH_MARK("deathmark", 1073741824),
   SKULL_FEAR("skull_fear", Integer.MIN_VALUE),
   S_INVINCIBLE("invincible", 1, true),
   S_AIR_STUN("airstun", 2, true),
   S_AIR_ROOT("airroot", 4, true),
   S_BAGUETTE_SWORD("baguettesword", 8, true),
   S_YELLOW_AFFRO("yellowafro", 16, true),
   S_PINK_AFFRO("pinkafro", 32, true),
   S_BLACK_AFFRO("blackafro", 64, true),
   S_UNKNOWN8("unknown8", 128, true),
   S_STIGMA_SHILIEN("stigmashilien", 256, true),
   S_STAKATOROOT("stakatoroot", 512, true),
   S_FREEZING("freezing", 1024, true),
   S_VESPER_S("vesper_s", 2048, true),
   S_VESPER_C("vesper_c", 4096, true),
   S_VESPER_D("vesper_d", 8192, true),
   TIME_BOMB("soa_respawn", 16384, true),
   ARCANE_SHIELD("arcane_shield", 32768, true),
   AIRBIND("airbird", 65536, true),
   CHANGEBODY("changebody", 131072, true),
   KNOCKDOWN("knockdown", 262144, true),
   NAVIT_ADVENT("ave_advent_blessing", 524288, true),
   KNOCKBACK("knockback", 1048576, true),
   CHANGE_7ANNIVERSARY("7anniversary", 2097152, true),
   ON_SPOT_MOVEMENT,
   DEPORT,
   AURA_BUFF,
   AURA_BUFF_SELF,
   AURA_DEBUFF,
   AURA_DEBUFF_SELF,
   HURRICANE,
   HURRICANE_SELF,
   BLACK_MARK,
   BR_SOUL_AVATAR,
   CHANGE_GRADE_B,
   BR_BEAM_SWORD_ONEHAND,
   BR_BEAM_SWORD_DUAL,
   D_NOCHAT,
   D_HERB_POWER,
   D_HERB_MAGIC,
   D_TALI_DECO_P,
   UNK_72,
   D_TALI_DECO_C,
   D_TALI_DECO_D,
   D_TALI_DECO_E,
   D_TALI_DECO_F,
   D_TALI_DECO_G,
   D_CHANGESHAPE_TRANSFORM_1,
   D_CHANGESHAPE_TRANSFORM_2,
   D_CHANGESHAPE_TRANSFORM_3,
   D_CHANGESHAPE_TRANSFORM_4,
   D_CHANGESHAPE_TRANSFORM_5,
   UNK_83,
   UNK_84,
   SANTA_SUIT,
   UNK_86,
   UNK_87,
   UNK_88,
   UNK_89,
   UNK_90,
   UNK_91,
   EMPTY_STARS,
   ONE_STAR,
   TWO_STARS,
   THREE_STARS,
   FOUR_STARS,
   FIVE_STARS,
   FACEOFF,
   UNK_99,
   UNK_100,
   UNK_101,
   UNK_102,
   UNK_103,
   UNK_104,
   UNK_105,
   STOCKING_FAIRY,
   TREE_FAIRY,
   SNOWMAN_FAIRY,
   UNK_109,
   UNK_110,
   UNK_111,
   UNK_112,
   UNK_113,
   STIGMA_STORM,
   GREEN_SPEED_UP,
   RED_SPEED_UP,
   WIND_PROTECTION,
   LOVE,
   PERFECT_STORM,
   UNK_120,
   UNK_121,
   UNK_122,
   GREAT_GRAVITY,
   STEEL_MIND,
   UNK_125,
   OBLATE,
   SPALLATION,
   U_HE_ASPECT_AVE,
   UNK_129,
   UNK_130,
   UNK_131,
   UNK_132,
   UNK_133,
   UNK_134,
   UNK_135,
   UNK_136,
   UNK_137,
   UNK_138,
   UNK_139,
   UNK_140,
   U_AVE_PALADIN_DEF,
   U_AVE_GUARDIAN_DEF,
   U_REALTAR2_AVE,
   U_AVE_DIVINITY,
   U_AVE_SHILPROTECTION,
   U_EVENT_STAR_CA,
   U_EVENT_STAR1_TA,
   U_EVENT_STAR2_TA,
   U_EVENT_STAR3_TA,
   U_EVENT_STAR4_TA,
   U_EVENT_STAR5_TA,
   U_AVE_ABSORB_SHIELD,
   U_KN_PHOENIX_AURA,
   U_KN_REVENGE_AURA,
   U_KN_EVAS_AURA,
   U_KN_REMPLA_AURA,
   U_AVE_LONGBOW,
   U_AVE_WIDESWORD,
   U_AVE_BIGFIST,
   U_AVE_SHADOWSTEP,
   U_TORNADO_AVE,
   U_AVE_SNOW_SLOW,
   U_AVE_SNOW_HOLD,
   UNK_164,
   U_AVE_TORNADO_SLOW,
   U_AVE_ASTATINE_WATER,
   U_BIGBD_CAT_NPC,
   U_BIGBD_UNICORN_NPC,
   U_BIGBD_DEMON_NPC,
   U_BIGBD_CAT_PC,
   U_BIGBD_UNICORN_PC,
   U_BIGBD_DEMON_PC,
   U_AVE_DRAGON_ULTIMATE(700),
   BR_POWER_OF_EVA(0),
   VP_KEEP(29),
   UNK_176,
   UNK_177,
   UNK_178,
   UNK_179,
   UNK_180,
   UNK_181,
   UNK_182,
   UNK_183,
   E_AFRO_1(37, "afrobaguette1", 1, false, true),
   E_AFRO_2(38, "afrobaguette2", 2, false, true),
   E_AFRO_3(39, "afrobaguette3", 4, false, true),
   E_EVASWRATH(0, "evaswrath", 8, false, true),
   E_HEADPHONE(0, "headphone", 16, false, true),
   E_VESPER_1(44, "vesper1", 32, false, true),
   E_VESPER_2(45, "vesper2", 64, false, true),
   E_VESPER_3(46, "vesper3", 128, false, true);

   public static final AbnormalEffect[] VALUES = values();
   private final int _id;
   private final int _mask;
   private final String _name;
   private final boolean _special;
   private final boolean _event;

   private AbnormalEffect() {
      this._id = this.ordinal();
      this._name = this.toString();
      this._mask = 0;
      this._special = false;
      this._event = false;
   }

   private AbnormalEffect(int id) {
      this._id = id;
      this._name = this.toString();
      this._mask = 0;
      this._special = false;
      this._event = false;
   }

   private AbnormalEffect(String name, int mask) {
      this._id = this.ordinal();
      this._name = name;
      this._mask = mask;
      this._special = false;
      this._event = false;
   }

   private AbnormalEffect(String name, int mask, boolean special) {
      this._id = this.ordinal();
      this._name = name;
      this._mask = mask;
      this._special = special;
      this._event = false;
   }

   private AbnormalEffect(int id, String name, int mask, boolean special, boolean event) {
      this._id = id;
      this._name = name;
      this._mask = mask;
      this._special = special;
      this._event = event;
   }

   public final int getId() {
      return this._id;
   }

   public final int getMask() {
      return this._mask;
   }

   public final String getName() {
      return this._name;
   }

   public final boolean isSpecial() {
      return this._special;
   }

   public final boolean isEvent() {
      return this._event;
   }

   public static AbnormalEffect getById(int id) {
      if (id > 0) {
         for(AbnormalEffect eff : VALUES) {
            if (eff.getId() == id) {
               return eff;
            }
         }

         throw new NoSuchElementException("AbnormalEffect not found for id: '" + id + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
      } else {
         return NONE;
      }
   }

   public static AbnormalEffect getByName(String name) {
      if (name != null && !name.isEmpty()) {
         for(AbnormalEffect eff : VALUES) {
            if (eff.getName().equalsIgnoreCase(name)) {
               return eff;
            }
         }

         for(AbnormalEffect eff : VALUES) {
            if (eff.toString().equalsIgnoreCase(name)) {
               return eff;
            }
         }

         throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
      } else {
         return NONE;
      }
   }
}
