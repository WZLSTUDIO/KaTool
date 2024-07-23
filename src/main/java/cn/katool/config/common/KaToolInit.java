package cn.katool.config.common;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
@Component
public class KaToolInit {
    public static String version = "1.9.6.GAMA";
    @Bean("KaTool-Init")
    void katoolConfig() {
        System.out.println(" ___  __    ________  _________  ________  ________  ___\n" +
                "|\\  \\|\\  \\ |\\   __  \\|\\___   ___\\\\   __  \\|\\   __  \\|\\  \\\n" +
                "\\ \\  \\/  /|\\ \\  \\|\\  \\|___ \\  \\_\\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\\n" +
                " \\ \\   ___  \\ \\   __  \\   \\ \\  \\ \\ \\  \\\\\\  \\ \\  \\\\\\  \\ \\  \\\n" +
                "  \\ \\  \\\\ \\  \\ \\  \\ \\  \\   \\ \\  \\ \\ \\  \\\\\\  \\ \\  \\\\\\  \\ \\  \\____\n" +
                "   \\ \\__\\\\ \\__\\ \\__\\ \\__\\   \\ \\__\\ \\ \\_______\\ \\_______\\ \\_______\\\n" +
                "    \\|__| \\|__|\\|__|\\|__|    \\|__|  \\|_______|\\|_______|\\|_______|\n" +
                "                                                          version:" + version);
    }
}
