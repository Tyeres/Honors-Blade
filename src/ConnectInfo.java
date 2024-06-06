public interface ConnectInfo {
    String LOCAL_IP = "localhost";
    String SERVER_IP = LOCAL_IP; // "10.0.0.194"

    int STARTING_PORT = 7999;
    // Player 1
    int COMBAT_PORT = 8001;
    int DEFENSE_PORT = 8002;
    int INPUT_PORT = 8005;

    // Player 2
    int COMBAT_PORT_2 = 8003;
    int DEFENSE_PORT_2 = 8004;
    int INPUT_PORT_2 = 8006;
}