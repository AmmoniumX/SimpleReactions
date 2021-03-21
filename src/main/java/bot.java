import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class bot extends ListenerAdapter{
    private static String defaultRoleId = null;
    private static HashMap<String, HashMap<String, String>> messageIdToEmojiAndRoleId = new HashMap<>();

    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        JDA jda = JDABuilder.createDefault("ODIyMzIzNjAxMTA4NjMxNTcy.YFQmiA.CEdqbDd_1s0zt7YTZQHbDzzAOy4").build().awaitReady();
        jda.addEventListener(new bot());
        System.out.println("Successfully initiated");

        // ahead is code to read variables from text files, and create them if there aren't

        System.out.println("Current directory: " + System.getProperty("user.dir"));
        // assert there is a folder called "data" where the jar is located, if not, create one
        String pathString = "./data/";
        Path path = Paths.get(pathString);
        File directoryPath = new File(pathString);
        if (!directoryPath.isDirectory()) {
            System.out.println("No data folder detected, creating one...");
            try {
                Files.createDirectory(path);
            } catch (Exception ex) {
                System.out.println("Error caught during attempt to create ./data/ directory:");
                ex.printStackTrace();
            }
        }

        // look for messageIdToEmojiToRoleId.txt, if it doesn't exist, create it
        String filePath = "./data/messageIdToEmojiToRoleId.txt";
        File emojiToRoleFile = new File(filePath);
        if (!emojiToRoleFile.exists()){
            System.out.println("Could not find messageIdToEmojiToRoleId.txt, creating it...");
            boolean fileCreateSuccess = emojiToRoleFile.createNewFile();
            System.out.println(fileCreateSuccess);
        } else {
            // now we can actually read the file
            try {
                FileInputStream fis = new FileInputStream(emojiToRoleFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                //noinspection unchecked
                messageIdToEmojiAndRoleId = (HashMap<String, HashMap<String, String>>) ois.readObject();
                // ignore warning, can't be done much abt it

                ois.close();
                fis.close();

            } catch (Exception ex){
                messageIdToEmojiAndRoleId = new HashMap<>();
                ex.printStackTrace();
            }
        }

        // now do it again for defaultRoleId.txt

        String defaultRolePath = "./data/defaultRoleId.txt";
        File defaultRoleFile = new File(defaultRolePath);
        if (!defaultRoleFile.exists()){
            System.out.println("Could not find defaultRoleId.txt, creating it...");
            boolean fileCreateSuccess = defaultRoleFile.createNewFile();
            System.out.println(fileCreateSuccess);
        } else {
            // Since defaultRoleId is just a string, the process is much simpler
            try {
                FileInputStream fis = new FileInputStream(defaultRoleFile);
                defaultRoleId = new String(fis.readAllBytes());
                System.out.println("Default role ID: " + defaultRoleId);
                fis.close();
            } catch (Exception ex){
                defaultRoleId = null;
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        System.out.println(e.getMessage());
        String[] message = e.getMessage().getContentRaw().split(" ");
        if (!message[0].startsWith(";")) { return;} // only run for commands that start with identificator ";"

        message[0] = message[0].substring(1); // remove identificator for searching commands

        System.out.println(Arrays.toString(message));

        assert e.getMember() != null;
        switch (message[0].toLowerCase()){

            case "help":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }

                String helpString = """
                        help: shows this message.
                        
                        setreactionrole [messageId] [emoji] [@role]: sets [emoji] in message [messageId] to give role [
                        @role] when reacted to. Doesn't accept custom emotes!
                                                
                        setdefaultrole [@role]: sets [@role] to be removed when reacting. Doesn't actually give the role
                         when joining!
                        
                        listactivemessages: shows a list of all active messages by their Id.
                        
                        removeactivemessage [messageId]: deletes all active reaction roles for [messageId].
                        """;

                e.getChannel().sendMessage(helpString).queue();

                break;

            case "setreactionrole":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }

                if (message.length < 4) {
                    e.getChannel().sendMessage("Not enough arguments, (3) needed").queue();
                    return;
                }
                String srrMessageId = message[1];
                String reactionEmoji = message[2];
                java.lang.String roleId = message[3].substring(3, message[3].length() - 1);
                Role reactionRole = e.getGuild().getRoleById(roleId);

                if (reactionRole == null){
                    e.getChannel().sendMessage("That's not a valid role. Was it written correcly?").queue();
                    return;
                }

                System.out.println(reactionEmoji + ", " + reactionRole.getName());
                e.getChannel().sendMessage(" Associated " + reactionEmoji + " to emote " + reactionRole.getAsMention() +
                " on message " + srrMessageId).queue();


                // emojiToRoleId.put(reactionEmoji, reactionRole.getId());

                if(messageIdToEmojiAndRoleId.containsKey(srrMessageId)){
                    HashMap<String, String> thisEmojiToRoleId = messageIdToEmojiAndRoleId.get(srrMessageId);
                    if(thisEmojiToRoleId.containsKey(reactionEmoji)) {
                        thisEmojiToRoleId.remove(reactionEmoji);
                        System.out.println("Removed conflict with same emote association");

                    } else {
                        thisEmojiToRoleId.put(reactionEmoji, roleId);
                    }
                } else{

                    HashMap<String, String> newEmojitoRoleId = new HashMap<>();
                    newEmojitoRoleId.put(reactionEmoji, roleId);
                    messageIdToEmojiAndRoleId.put(srrMessageId, newEmojitoRoleId);
                }

                e.getChannel().retrieveMessageById(srrMessageId).complete().addReaction(reactionEmoji).queue();

                // save messageIdToEmojiToRoleId to the file
                String filePath = "./data/messageIdToEmojiToRoleId.txt";
                File emojiToRoleFile = new File(filePath);

                try {
                    FileOutputStream fos = new FileOutputStream(emojiToRoleFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(messageIdToEmojiAndRoleId);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save emojiToRoleId.txt!");
                }
                break;

            case "setdefaultrole":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }
                if (message.length < 2) {
                    e.getChannel().sendMessage("Not enough arguments, (1) needed").queue();
                    return;
                }
                defaultRoleId = message[1].substring(3, message[1].length() - 1);
                Role defaultRole = e.getGuild().getRoleById(defaultRoleId);
                assert defaultRole != null;
                e.getChannel().sendMessage("Set " + defaultRole.getAsMention() + " as default role").queue();

                // save defaultRoleId to the file
                String defaultRolePath = "./data/defaultRoleId.txt";
                try {
                    FileOutputStream fos = new FileOutputStream(defaultRolePath);
                    fos.write(defaultRoleId.getBytes());
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save defaultRoleId.txt!");
                }
                break;

            case "listactivemessages":
                String activemessagesString = messageIdToEmojiAndRoleId.keySet().toString();
                System.out.println(activemessagesString);
                e.getChannel().sendMessage(activemessagesString).queue();
                break;

            case "removeactivemessage":
                if(message.length < 2) {
                    e.getChannel().sendMessage("Not enough arguments, (1) needed").queue();
                    return;
                }
                String messageToRemove = message[1];

                if(!messageIdToEmojiAndRoleId.containsKey(messageToRemove)) {
                    e.getChannel().sendMessage("That's not an active message (check ;listactivemessages").queue();
                    return;
                }

                messageIdToEmojiAndRoleId.remove(messageToRemove);
                e.getChannel().sendMessage("Successfully removed from active messages").queue();
                break;

            default:
                break;
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e){

        assert e.getMember() != null;
        if(Objects.requireNonNull(e.getUser()).isBot()){
            System.out.println("Ignored action by bot User");
            return;
        }

        if(!messageIdToEmojiAndRoleId.containsKey(e.getMessageId())) {
            System.out.println(e.getMessageId() + " is not an active reaction message.");
            return;
        }

        if(!e.getReactionEmote().isEmoji()){
            System.out.println("Reaction emote is not emoji (i.e. is custom guild emote)");
            return;
        }


        String reactionEmote = e.getReactionEmote().getEmoji();
        String reactionMessageId = e.getMessageId();
        HashMap<String, String> emojiToRoleId = messageIdToEmojiAndRoleId.get(reactionMessageId);

        if(!emojiToRoleId.containsKey(reactionEmote)) {
            System.out.println("Emote " + reactionEmote + "isn't registered as a reaction emote for this message");
            return;
        }

        Role roleToGive = e.getGuild().getRoleById(emojiToRoleId.get(reactionEmote));
        assert roleToGive != null;
        e.getGuild().addRoleToMember(e.getMember(), roleToGive).queue();
        System.out.println("Successfully added role " + roleToGive.getName() + " to " + e.getMember().getEffectiveName());

        if (defaultRoleId != null) {
            Role defaultRole = e.getGuild().getRoleById(defaultRoleId);
            assert defaultRole != null;
            if (e.getMember().getRoles().contains(defaultRole)) {
                e.getGuild().removeRoleFromMember(e.getMember(), defaultRole).queue();
                System.out.println("Successfully removed role " + defaultRole.getName() + " from " + e.getMember().getEffectiveName());
            }
        }

        assert e.getUser() != null;
        e.getReaction().removeReaction(e.getUser()).queue();

    }
}
