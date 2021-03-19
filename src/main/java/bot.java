import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class bot extends ListenerAdapter{

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = JDABuilder.createDefault("ODIyMzIzNjAxMTA4NjMxNTcy.YFQmiA.CEdqbDd_1s0zt7YTZQHbDzzAOy4").build().awaitReady();
        System.out.println(jda.getStatus().toString());
        // jda.addEventListener(new msgEvent());
        jda.addEventListener(new bot());
        System.out.println(jda.getRegisteredListeners().toString());
        System.out.println("Successfully initiated");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {

    }
}
