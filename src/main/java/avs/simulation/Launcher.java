package avs.simulation;

public class Launcher {

    public static void main(String[] args) {
        String osName = System.getProperty("os.name").toLowerCase();
        String platform = osName.contains("win") ? "win" :
                osName.contains("mac") ? "mac" : "linux";

        System.setProperty("javafx.platform", platform);
        avs.simulation.Main.main(args);
    }

}
