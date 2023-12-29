package l2e.gameserver.model.entity.mods.votereward.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteApiService;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;

public class MmoTopSite extends AbstractAutoRewardSite {
   private static final DateFormat MMOTOP_SERVER_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
   private final Pattern VOTES_PATTERN = Pattern.compile(
      "(\\d+)\t+(\\d{2}\\.\\d{2}\\.\\d{4} +\\d{2}:\\d{2}:\\d{2})\t+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\t+(\\S+)\t+(\\d+)\\s+", 104
   );
   private final String serverVotesLink;
   private final String identifierType;

   public MmoTopSite(MultiValueSet<String> parameters) {
      super(parameters);
      this.serverVotesLink = parameters.getString("votes_link");
      this.identifierType = parameters.getString("identifier_type");
   }

   @Override
   public boolean isEnabled() {
      return StringUtils.isEmpty(this.serverVotesLink) ? false : super.isEnabled();
   }

   @Override
   protected String getIdentifier(Player player) {
      return this.identifierType.equalsIgnoreCase("ip") ? player.getIPAddress() : player.getName();
   }

   @Override
   protected void parseVotes(Map<String, List<IntIntPair>> votesCache) {
      String serverResponse = VoteApiService.getApiResponse(String.format("https://mmotop.ru/votes/%s", this.serverVotesLink));
      if (serverResponse != null) {
         Matcher m = this.VOTES_PATTERN.matcher(serverResponse);

         while(m.find()) {
            Date voteDate;
            try {
               voteDate = MMOTOP_SERVER_DATE_FORMAT.parse(m.group(2));
            } catch (Exception var7) {
               this.error(String.format("Cannot parse voting date: %s", m.group(2)), var7);
               continue;
            }

            String identifier;
            if (this.identifierType.equalsIgnoreCase("ip")) {
               identifier = m.group(3);
            } else {
               identifier = m.group(4);
            }

            List<IntIntPair> votes = votesCache.computeIfAbsent(identifier.toLowerCase(), list -> new ArrayList());
            votes.add(new IntIntPairImpl((int)(voteDate.getTime() / 1000L), Integer.parseInt(m.group(5))));
         }
      }
   }

   static {
      MMOTOP_SERVER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
   }
}
