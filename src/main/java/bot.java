import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
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
import java.util.HashMap;

public class bot extends ListenerAdapter{
    private static HashMap<String, HashMap<String, String>> messageIdToEmojiAndRoleId = new HashMap<>();
    private static HashMap<String, String> messageIdToRemovalRoleId = new HashMap<>();

    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        JDA jda = JDABuilder.createDefault("ODM0NTg3OTE1NDI1ODA4NDI0.YIDEkA.3dU_OaTKLmHe2ulOFeu7jXC0_QE").build().awaitReady();
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
                System.out.println("Exception caught during attempt to create ./data/ directory:");
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

        } else if (emojiToRoleFile.length() > 0){
            // now we can actually read the file
            try {
                FileInputStream fis = new FileInputStream(emojiToRoleFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                //noinspection unchecked
                messageIdToEmojiAndRoleId = (HashMap<String, HashMap<String, String>>) ois.readObject();
                // ignore warning, can't be done much abt it

                ois.close();
                fis.close();

                String activemessagesString = messageIdToEmojiAndRoleId.keySet().toString();
                System.out.println("Active messages: " + activemessagesString);

            } catch (Exception ex){
                messageIdToEmojiAndRoleId = new HashMap<>();
                ex.printStackTrace();
            }
        }

        // look for messageIdToRemovalRoleId.txt, if it doesn't exist, create it
        String filePath2 = "./data/messageIdToRemovalRoleId.txt";
        File messageIdToRemovalRoleIdFile = new File(filePath2);
        if (!messageIdToRemovalRoleIdFile.exists()){
            System.out.println("Could not find messageIdToRemovalRoleId.txt, creating it...");
            boolean fileCreateSuccess = messageIdToRemovalRoleIdFile.createNewFile();
            System.out.println(fileCreateSuccess);

        } else if (messageIdToRemovalRoleIdFile.length() > 0){
            // now we can actually read the file
            try {
                FileInputStream fis = new FileInputStream(messageIdToRemovalRoleIdFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                //noinspection unchecked
                messageIdToRemovalRoleId = (HashMap<String, String>) ois.readObject();
                // ignore warning, can't be done much abt it

                ois.close();
                fis.close();

                System.out.println("Loaded " + messageIdToRemovalRoleId.size() + " objects from messageIdToRemovalRoleId.txt");

            } catch (Exception ex){
                messageIdToRemovalRoleId = new HashMap<>();
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {

        String[] message = e.getMessage().getContentRaw().split(" ");
        if (!message[0].startsWith(";")) { return;} // only run for commands that start with identificator ";"
        System.out.println(e.getMessage());
        message[0] = message[0].substring(1); // remove identificator for searching commands

        assert e.getMember() != null;
        switch (message[0].toLowerCase()){

            case "help":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }

                String helpString = "help: shows this message. \n" +
                        "setreactionrole [messageId] [emoji] [@role]: sets [emoji] in message [messageId] to give role [" +
                        "@role] when reacted to. Doesn't accept custom emotes! \n" +
                        "when joining! \n" +
                        "listactivemessages: shows a list of all active messages by their Id. \n" +
                        "(extra: messageIds have a '~' next to them if they are associated with a removalRole\n" +
                        "removeactivemessage [messageId]: deletes reaction roles for [messageId] and removal roles" +
                        "if present. \n" +
                        "setRemovalRole [messageId] [@role]: sets role to be removed when reacting to [messageId].\n" +
                        "removeRemovalRole [messageId]: unassigns removal role from [messageId] if present."
                        ;

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
                String srrFullMessageId = message[1];
                String srrMessageId = srrFullMessageId.substring(19);
                String srrChannelId = srrFullMessageId.substring(0, 18);
                System.out.println("Channel ID: " + srrChannelId + ", message ID: " + srrMessageId);
                String reactionEmoji = message[2];
                String roleId = message[3].substring(3, message[3].length() - 1);
                Role reactionRole = e.getGuild().getRoleById(roleId);

                if (reactionRole == null){
                    e.getChannel().sendMessage("That's not a valid role. Was it written correcly?").queue();
                    return;
                }

                System.out.println(reactionEmoji + ", " + reactionRole.getName());
                e.getChannel().sendMessage(" Associated " + reactionEmoji + " to emote " + reactionRole.getAsMention() +
                " on message " + srrFullMessageId).queue();

                if(messageIdToEmojiAndRoleId.containsKey(srrFullMessageId)){
                    HashMap<String, String> thisEmojiToRoleId = messageIdToEmojiAndRoleId.get(srrFullMessageId);
                    if(thisEmojiToRoleId.containsKey(reactionEmoji)) {
                        thisEmojiToRoleId.remove(reactionEmoji);
                        System.out.println("Removed conflict with same emote association");

                    } else {
                        thisEmojiToRoleId.put(reactionEmoji, roleId);
                    }
                } else{

                    HashMap<String, String> newEmojitoRoleId = new HashMap<>();
                    newEmojitoRoleId.put(reactionEmoji, roleId);
                    messageIdToEmojiAndRoleId.put(srrFullMessageId, newEmojitoRoleId);
                }

                MessageChannel msgChannel = (MessageChannel) e.getGuild().getGuildChannelById(srrChannelId);
                if (msgChannel == null) {
                   e.getChannel().sendMessage("Could not recognize that message ID! Was it written correctly?").queue();
                   return;
                }
                msgChannel.retrieveMessageById(srrMessageId).complete().addReaction(reactionEmoji).queue();

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
                    System.out.println("Couldn't save messageIdToRoleId.txt!");
                }
                break;

            case "listactivemessages":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }
                StringBuilder activeMessagesList = new StringBuilder();
                for (String activeMessage: messageIdToEmojiAndRoleId.keySet()) {
                    activeMessagesList.append(activeMessage);
                    if (messageIdToRemovalRoleId.containsKey(activeMessage)) {
                        activeMessagesList.append("~");
                    }
                    activeMessagesList.append("\n");
                }
                e.getChannel().sendMessage(activeMessagesList.toString()).queue();
                System.out.println(activeMessagesList.toString());

                break;

            case "removeactivemessage":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }
                if(message.length < 2) {
                    e.getChannel().sendMessage("Not enough arguments, (1) needed").queue();
                    return;
                }

                String ramFullMessageId = message[1];

                if(!messageIdToEmojiAndRoleId.containsKey(ramFullMessageId)) {
                    e.getChannel().sendMessage("That's not an active message (check ;listactivemessages").queue();
                    return;
                }

                messageIdToEmojiAndRoleId.remove(ramFullMessageId);

                messageIdToRemovalRoleId.remove(ramFullMessageId);
                
                e.getChannel().sendMessage("Successfully removed from active messages").queue();

                // save messageIdToEmojiToRoleId to the file
                String ramFilePath = "./data/messageIdToEmojiToRoleId.txt";
                File ramEmojiToRoleFile = new File(ramFilePath);

                try {
                    FileOutputStream fos = new FileOutputStream(ramEmojiToRoleFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(messageIdToEmojiAndRoleId);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save messageIdToEmojiToRoleId.txt!");
                }

                // save messageIdToRemovalRoleId to the file
                String filePath2 = "./data/messageIdToRemovalRoleId.txt";
                File messageIdToRemovalRoleIdFile = new File(filePath2);

                try {
                    FileOutputStream fos = new FileOutputStream(messageIdToRemovalRoleIdFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(messageIdToRemovalRoleIdFile);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save messageIdToRemovalRoleId.txt!");
                }
                
                break;

            case "setremovalrole":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }
                if(message.length < 3) {
                    e.getChannel().sendMessage("Not enough arguments, (2) needed").queue();
                    return;
                }

                String rrMessageId = message[1];
                String rrRoleId = message[2].substring(3, message[2].length() - 1);
                Role removalRole = e.getGuild().getRoleById(rrRoleId);

                if (removalRole == null) {
                    e.getChannel().sendMessage("Removal role could not be read. Was it written correctly?").queue();
                    return;
                }

                if (!messageIdToEmojiAndRoleId.containsKey(rrMessageId)) {
                    e.getChannel().sendMessage("That message isn't registered as an active message, try ;setreactionrole first.").queue();
                    return;
                }


                messageIdToRemovalRoleId.remove(rrMessageId);
                messageIdToRemovalRoleId.put(rrMessageId, rrRoleId);

                e.getChannel().sendMessage("Successfully associated active message " + rrMessageId + "to removal role " +
                removalRole.getAsMention()).queue();

                // save messageIdToRemovalRoleId to the file
                filePath2 = "./data/messageIdToRemovalRoleId.txt";
                messageIdToRemovalRoleIdFile = new File(filePath2);

                try {
                    FileOutputStream fos = new FileOutputStream(messageIdToRemovalRoleIdFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(messageIdToRemovalRoleIdFile);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save messageIdToRemovalRoleId.txt!");
                }
                break;

            case "removeremovalrole":
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                    return;
                }
                if (message.length < 2) {
                    e.getChannel().sendMessage("Not enough arguments, (1) needed").queue();
                    return;
                }

                String removalMessageId = message[1];

                if (!messageIdToRemovalRoleId.containsKey(removalMessageId)) {
                    e.getChannel().sendMessage("That message has no removal roles associated with it.").queue();
                    return;
                }

                messageIdToRemovalRoleId.remove(removalMessageId);

                // save messageIdToRemovalRoleId to the file
                filePath2 = "./data/messageIdToRemovalRoleId.txt";
                messageIdToRemovalRoleIdFile = new File(filePath2);

                try {
                    FileOutputStream fos = new FileOutputStream(messageIdToRemovalRoleIdFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(messageIdToRemovalRoleIdFile);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save messageIdToRemovalRoleId.txt!");
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e){

        assert e.getMember() != null;
        assert e.getUser() != null;
        if(e.getUser().isBot()){
            System.out.println("Ignored action by bot User");
            return;
        }

        String channelID = e.getChannel().getId();
        String messageId = e.getMessageId();
        String fullMessageId = channelID + "-" + messageId;


        if(!messageIdToEmojiAndRoleId.containsKey(fullMessageId)) {
            System.out.println(e.getMessageId() + " is not an active reaction message.");
            return;
        }

        if(!e.getReactionEmote().isEmoji()){
            System.out.println("Reaction emote is not emoji (i.e. is custom guild emote)");
            return;
        }


        String reactionEmote = e.getReactionEmote().getEmoji();
        HashMap<String, String> emojiToRoleId = messageIdToEmojiAndRoleId.get(fullMessageId);

        if(!emojiToRoleId.containsKey(reactionEmote)) {
            System.out.println("Emote " + reactionEmote + "isn't registered as a reaction emote for this message");
            return;
        }

        Role roleToGive = e.getGuild().getRoleById(emojiToRoleId.get(reactionEmote));
        assert roleToGive != null;
        e.getGuild().addRoleToMember(e.getMember(), roleToGive).queue();
        System.out.println("Successfully added role " + roleToGive.getName() + " to " + e.getMember().getEffectiveName());

        if(messageIdToRemovalRoleId.containsKey(fullMessageId)) {
            String removalRoleId = messageIdToRemovalRoleId.get(fullMessageId);
            Role removalRole = e.getGuild().getRoleById(removalRoleId);
            assert removalRole != null;

            if (e.getMember().getRoles().contains(removalRole)) {
                e.getGuild().removeRoleFromMember(e.getMember(), removalRole).queue();
                System.out.println("Removed role " + removalRoleId + " from " + e.getMember().getEffectiveName());
            }

        }

        e.getReaction().removeReaction(e.getUser()).queue();

    }
}
