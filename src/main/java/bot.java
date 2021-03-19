import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
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

public class bot extends ListenerAdapter{
    private static String reactionMessageId;
    private static HashMap<String, String> emojiToRoleId = new HashMap<>();
    private static String defaultRoleId = null;

    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        JDA jda = JDABuilder.createDefault("ODIyMzIzNjAxMTA4NjMxNTcy.YFQmiA.CEdqbDd_1s0zt7YTZQHbDzzAOy4").build().awaitReady();
        System.out.println(jda.getStatus().toString());
        // jda.addEventListener(new msgEvent());
        jda.addEventListener(new bot());
        System.out.println(jda.getRegisteredListeners().toString());
        System.out.println("Successfully initiated");

        // ahead is code to write emojiToRole from a ./data/emojiToRoleId.txt, in order that data is saved after reboot

        System.out.println("Current directory: " + System.getProperty("user.dir"));
        // assert there is a folder called "data" where the jar is located, if not, create one
        String pathString = "./data/";
        Path path = Paths.get(pathString);
        File directoryPath = new File(pathString);
        if (!directoryPath.isDirectory()) {
            System.out.println("No data folder detected, creating one...");
            Files.createDirectory(path);
        }

        // look for emojiToRoleId.txt, if it exists, write from it, if it doesn't, create it
        String filePath = "./data/emojiToRoleId.txt";
        File emojiToRoleFile = new File(filePath);
        if (!emojiToRoleFile.exists()){
            System.out.println("Could not find emojiToRoleId.txt, creating it...");
            boolean fileCreateSuccess = emojiToRoleFile.createNewFile();
            System.out.println(fileCreateSuccess);
        } else {
            // this is the code that actually reads emojiToRole.txt and writes it to emojiToRoleId
            try {
                FileInputStream fis = new FileInputStream(emojiToRoleFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                //noinspection unchecked
                emojiToRoleId = (HashMap<String, String>) ois.readObject(); // ignore warning, can't be done much abt it
                ois.close();
            } catch (Exception ex){
                emojiToRoleId = new HashMap<>();
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

        // and once more for reactionMessageId.txt

        String reactionMessagePath = "./data/reactionMessageId.txt";
        File reactionMessageFile = new File(reactionMessagePath);
        if (!reactionMessageFile.exists()){
            System.out.println("Could not find reactionMessageId.txt, creating it...");
            boolean fileCreateSuccess = reactionMessageFile.createNewFile();
            System.out.println(fileCreateSuccess);
        } else {
            try {
                FileInputStream fis = new FileInputStream(reactionMessageFile);
                reactionMessageId = new String(fis.readAllBytes());
                System.out.println("Reaction message ID: " + defaultRoleId);
                fis.close();
            } catch (Exception ex){
                reactionMessageId = null;
                ex.printStackTrace();
            }
        }




    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        System.out.println(e.getMessage().getContentRaw());
        String[] message = e.getMessage().getContentRaw().split(" ");
        if (!message[0].startsWith(";")) { return;} // only run for commands that start with identificator ";"

        message[0] = message[0].substring(1); // remove identificator for searching commands

        System.out.println(Arrays.toString(message));

        switch (message[0].toLowerCase()){

            case "setmessage":
                if (message.length < 2) {
                    e.getChannel().sendMessage("Not enough arguments, (1) needed").queue();
                    return;
                }
                System.out.println("Setting message as active mesage");
                assert e.getMember() != null;
                if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)){
                    e.getChannel().sendMessage("You don't have enough permissions to do this.").queue();
                }

                Message reactionMessage = e.getChannel().retrieveMessageById(message[1]).complete();
                if (reactionMessage == null){
                    e.getChannel().sendMessage("There was an error retrieving that message! Was it written correctly?").queue();
                    return;
                }
                System.out.println("Successfully set message " + reactionMessage.getContentRaw() + " as reaction message");
                e.getChannel().sendMessage("Successfully set message " + reactionMessage.getContentRaw() + " as reaction message").queue();

                reactionMessageId = message[1];

                // save it to reactionMessageId.txt
                String reactionMessagePath = "./data/reactionMessageId.txt";
                try{
                    FileOutputStream fos = new FileOutputStream(reactionMessagePath);
                    fos.write(reactionMessageId.getBytes());
                    fos.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Couldn't save reactionMessageId.txt!");
                }


                break;
            case "setreactionrole":
                if (message.length < 3) {
                    e.getChannel().sendMessage("Not enough arguments, (2) needed").queue();
                    return;
                }
                String reactionEmoteString = message[1];
                String roleId = message[2].substring(3, message[2].length() - 1);
                Role reactionRole = e.getGuild().getRoleById(roleId);
                assert reactionRole != null;
                System.out.println(reactionEmoteString + ", " + reactionRole.getName());
                e.getChannel().sendMessage(" Associated " + reactionEmoteString + " to emote " + reactionRole.getAsMention()).queue();
                emojiToRoleId.put(reactionEmoteString, reactionRole.getId());

                // save emojiToRoleFile to the file
                String filePath = "./data/emojiToRoleId.txt";
                File emojiToRoleFile = new File(filePath);

                try {
                    FileOutputStream fos = new FileOutputStream(emojiToRoleFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(emojiToRoleId);
                    oos.close();
                    fos.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Couldn't save emojiToRoleId.txt!");
                }

                break;

            case "setdefaultrole":
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


            default:
                break;
        }

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e){
        if(!e.getMessageId().equalsIgnoreCase(reactionMessageId)) {
            System.out.println(e.getMessageId() + " is not active reaction message " + reactionMessageId);
            return;
        }

        if(!e.getReactionEmote().isEmoji()){
            System.out.println("Reaction emote is not emoji (is custom guild emote)");
            return;
        }

        String reactionEmote = e.getReactionEmote().getEmoji();
        if (!emojiToRoleId.containsKey(reactionEmote)){
            System.out.println("Emote " + reactionEmote + "isn't registered as a reaction emote");
            return;
        }

        Role roleToGive = e.getGuild().getRoleById(emojiToRoleId.get(reactionEmote));
        assert e.getMember() != null;
        assert roleToGive != null;
        e.getGuild().addRoleToMember(e.getMember(), roleToGive).queue();
        System.out.println("Successfully added role " + roleToGive.getName() + " to " + e.getMember().getEffectiveName());

        if (defaultRoleId != null) {
            Role defaultRole = e.getGuild().getRoleById(defaultRoleId);
            assert defaultRole != null;
            if (e.getMember().getRoles().contains(defaultRole)) {
                e.getGuild().removeRoleFromMember(e.getMember(), defaultRole).queue();
                System.out.println("Successfully removed role " + defaultRole + " to " + e.getMember().getEffectiveName());
            }
        }

    }
}
