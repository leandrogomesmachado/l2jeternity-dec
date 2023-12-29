package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookIdentityType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;
import l2e.gameserver.model.entity.mods.facebook.template.ActiveTask;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;

public final class FacebookCommentsParser extends DocumentParser {
   private static final String SERVER_NAME_REPLACEMENT = "%serverName%";
   private static final char[] CHARS_TO_NOT_CHECK = new char[]{' ', '!', '$', '^', ',', '.', '?'};
   private static final String NICKNAME_SYNTAX_COMMENT_KEY = "%originalComment%";
   private static final String NICKNAME_SYNTAX_CHAR_NAME_KEY = "%charName%";
   private static final int FIND_NOT_USED_COMMENT_MAX_TRIES = 15;
   private static final long CLEAR_LAST_USED_COMMENTS_DELAY = 30000L;
   private final HashMap<String, ArrayList<String>> _commentsByType = new HashMap<>(8);
   private final ConcurrentHashMap<String, Long> _lastUsedComments = new ConcurrentHashMap<>();
   protected final ScheduledFuture<?> _clearLastUsedCommentsThread;
   private Pattern COMMENT_WITH_CHAR_NAME_PATTERN;

   protected FacebookCommentsParser() {
      this.reloadPattern();
      this._clearLastUsedCommentsThread = ThreadPoolManager.getInstance()
         .scheduleAtFixedDelay(new FacebookCommentsParser.ClearLastUsedCommentsThread(this._lastUsedComments), 30000L, 30000L);
      this.load();
   }

   @Override
   public void load() {
      this.parseDatapackFile("data/stats/services/facebook_comments.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._commentsByType.size() + " facebook comment types.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Node n = this.getCurrentDocument().getFirstChild();

      for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
         if (d.getNodeName().equals("type")) {
            String name = parseString(d.getAttributes(), "name");

            for(Node r = d.getFirstChild(); r != null; r = r.getNextSibling()) {
               if (r.getNodeName().equals("comment")) {
                  String comment = parseString(r.getAttributes(), "value");
                  comment = comment.replace("%serverName%", Config.SERVER_NAME);
                  this.addNewComment(name, comment);
               }
            }
         }
      }
   }

   public void addNewComment(String type, String comment) {
      ArrayList<String> comments = this._commentsByType.get(type);
      if (comments == null) {
         comments = new ArrayList<>(64);
         comments.add(comment.trim());
         this._commentsByType.put(type, comments);
      } else {
         comments.add(comment.trim());
      }
   }

   public String getCommentToWrite(OfficialPost fatherAction, FacebookIdentityType identityType, String identityValue) {
      String[] acceptedCommentTypes = this._commentsByType.keySet().toArray(new String[this._commentsByType.size()]);
      int tryIndex = 0;

      String comment;
      for(comment = null; tryIndex < 15; ++tryIndex) {
         comment = this.chooseRandomComment(acceptedCommentTypes);
         if (comment != null && !this._lastUsedComments.containsKey(comment)) {
            this.addCommentToLastUsed(comment);
            return prepareComment(comment, identityType, identityValue);
         }
      }

      return comment;
   }

   public FacebookCommentsParser.CommentMatchType checkCommentMatches(ActiveTask task, FacebookAction action) {
      if (!task.getRequestedMessage().isEmpty() && !action.getMessage().isEmpty()) {
         if (task.getIdentityType() != FacebookIdentityType.NAME_IN_COMMENT) {
            return commentMatches(action.getMessage(), task.getRequestedMessage())
               ? FacebookCommentsParser.CommentMatchType.FULL_MATCH
               : FacebookCommentsParser.CommentMatchType.COMMENT_NOT_MATCHES;
         } else {
            Matcher wroteMessageMatcher = this.COMMENT_WITH_CHAR_NAME_PATTERN.matcher(action.getMessage());
            if (!wroteMessageMatcher.matches()) {
               return FacebookCommentsParser.CommentMatchType.NONE_MATCHES;
            } else {
               Matcher requestedMessageMatcher = this.COMMENT_WITH_CHAR_NAME_PATTERN.matcher(task.getRequestedMessage());
               if (!requestedMessageMatcher.matches()) {
                  this._log.warning("Requested Message does not match Matcher! Msg: " + task.getRequestedMessage());
                  return FacebookCommentsParser.CommentMatchType.NONE_MATCHES;
               } else {
                  boolean nameMatches = wroteMessageMatcher.group("charName").equalsIgnoreCase(requestedMessageMatcher.group("charName"));
                  boolean commentMatches = commentMatches(wroteMessageMatcher.group("comment"), requestedMessageMatcher.group("comment"));
                  if (nameMatches && commentMatches) {
                     return FacebookCommentsParser.CommentMatchType.FULL_MATCH;
                  } else if (!nameMatches && !commentMatches) {
                     return FacebookCommentsParser.CommentMatchType.NONE_MATCHES;
                  } else {
                     return nameMatches
                        ? FacebookCommentsParser.CommentMatchType.COMMENT_NOT_MATCHES
                        : FacebookCommentsParser.CommentMatchType.IDENTITY_NOT_MATCHES;
                  }
               }
            }
         }
      } else if (task.getIdentityType() == FacebookIdentityType.NAME_IN_COMMENT) {
         return FacebookCommentsParser.CommentMatchType.NONE_MATCHES;
      } else {
         return task.getRequestedMessage().isEmpty() && action.getMessage().isEmpty()
            ? FacebookCommentsParser.CommentMatchType.FULL_MATCH
            : FacebookCommentsParser.CommentMatchType.COMMENT_NOT_MATCHES;
      }
   }

   private static boolean commentMatches(String requestedMessage, String wroteMessage) {
      return prepareMsgToCompere(requestedMessage).equalsIgnoreCase(prepareMsgToCompere(wroteMessage));
   }

   private static String prepareComment(String comment, FacebookIdentityType identityType, CharSequence identityValue) {
      if (identityType == FacebookIdentityType.NAME_IN_COMMENT) {
         String preparedComment = Config.FACEBOOK_NAME_SYNTAX.replace("%originalComment%", comment);
         return preparedComment.replace("%charName%", identityValue);
      } else {
         return comment;
      }
   }

   private String chooseRandomComment(String... fromTypes) {
      int totalSize = this.countTotalSize(fromTypes);
      if (totalSize <= 0) {
         return null;
      } else {
         int commentIndex = Rnd.get(0, totalSize - 1);
         if (commentIndex < 0) {
            this._log
               .warning(
                  "Error while choosing random Comment, commentIndex = "
                     + commentIndex
                     + ". totalSize: "
                     + totalSize
                     + ". fromTypes: "
                     + Arrays.toString((Object[])fromTypes)
               );
            commentIndex = 0;
         }

         int reachedIndex = 0;

         try {
            for(Entry<String, ArrayList<String>> entry : this._commentsByType.entrySet()) {
               if (ArrayUtils.contains(fromTypes, entry.getKey())) {
                  if (reachedIndex + entry.getValue().size() - 1 > commentIndex) {
                     return entry.getValue().get(commentIndex - reachedIndex);
                  }

                  reachedIndex += entry.getValue().size();
               }
            }
         } catch (ArrayIndexOutOfBoundsException var7) {
            this._log
               .log(
                  Level.SEVERE,
                  "OutOfBounds in chooseRandomComment! commentIndex: "
                     + commentIndex
                     + ". totalSize: "
                     + totalSize
                     + ". fromTypes: "
                     + Arrays.toString((Object[])fromTypes)
                     + ". reachedIndex: "
                     + reachedIndex,
                  (Throwable)var7
               );
         }

         return null;
      }
   }

   private int countTotalSize(String... fromTypes) {
      int totalSize = 0;

      for(Entry<String, ArrayList<String>> entry : this._commentsByType.entrySet()) {
         if (ArrayUtils.contains(fromTypes, entry.getKey())) {
            totalSize += entry.getValue().size();
         }
      }

      return totalSize;
   }

   private void addCommentToLastUsed(String comment) {
      this._lastUsedComments.put(comment, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_COMMENT_REUSE, TimeUnit.SECONDS));
   }

   private void reloadPattern() {
      String commentSyntax = Config.FACEBOOK_NAME_SYNTAX;
      String pattern = commentSyntax.replace("%originalComment%", "(?<comment>[\\S\\s]+)");
      pattern = pattern.replace("%charName%", "(?<charName>" + Config.CNAME_TEMPLATE + ")");
      this.COMMENT_WITH_CHAR_NAME_PATTERN = Pattern.compile(pattern, 2);
   }

   public Set<String> getCommentTypesForIterate() {
      return this._commentsByType.keySet();
   }

   public HashSet<String> getCommentTypesCopy() {
      return new HashSet<>(this._commentsByType.keySet());
   }

   public ArrayList<String> getCommentsForIterate(String type) {
      return this._commentsByType.get(type);
   }

   public ArrayList<String> getCommentsCopy(String type) {
      return new ArrayList<>(this._commentsByType.get(type));
   }

   private static String prepareMsgToCompere(String msg) {
      StringBuilder builder = new StringBuilder(msg.length());

      for(char c : msg.toCharArray()) {
         if (!ArrayUtils.contains(CHARS_TO_NOT_CHECK, c)) {
            builder.append(c);
         }
      }

      return builder.toString();
   }

   public static FacebookCommentsParser getInstance() {
      return FacebookCommentsParser.SingletonHolder.INSTANCE;
   }

   private static class ClearLastUsedCommentsThread extends RunnableImpl {
      private final ConcurrentHashMap<String, Long> lastUsedComments;

      ClearLastUsedCommentsThread(ConcurrentHashMap<String, Long> lastUsedComments) {
         this.lastUsedComments = lastUsedComments;
      }

      @Override
      public void runImpl() {
         long currentDate = System.currentTimeMillis();
         ArrayList<String> commentsToDelete = new ArrayList<>(3);

         for(Entry<String, Long> lastUsedCommentEntry : this.lastUsedComments.entrySet()) {
            if (lastUsedCommentEntry.getValue() < currentDate) {
               commentsToDelete.add(lastUsedCommentEntry.getKey());
            }
         }

         for(String commentToDelete : commentsToDelete) {
            this.lastUsedComments.remove(commentToDelete);
         }
      }
   }

   public static enum CommentMatchType {
      FULL_MATCH,
      COMMENT_NOT_MATCHES,
      IDENTITY_NOT_MATCHES,
      NONE_MATCHES;
   }

   private static class SingletonHolder {
      private static final FacebookCommentsParser INSTANCE = new FacebookCommentsParser();
   }
}
