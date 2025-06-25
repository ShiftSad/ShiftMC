package dev.shiftsad.lobby;

import lombok.AllArgsConstructor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.SharedInstance;

@AllArgsConstructor
public class PlayerMenu {

    private final Player player;
    private final SharedInstance instance;
    private final PlayerMenuConfiguration configuration;

}
