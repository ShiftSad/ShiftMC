package dev.shiftsad.lobby;

import lombok.NoArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.SharedInstance;
import dev.shiftsad.core.config.Value;

import java.util.WeakHashMap;

@NoArgsConstructor
public class PlayerMenu {
    @Value("menuConfiguration.npcPosition")
    private static Pos npcPosition;
    @Value("menuConfiguration.cameraPosition")
    private static Pos cameraPosition;
    @Value("menuConfiguration.portalPosition")
    private static Pos portalPosition;
    @Value("menuConfiguration.animationDuration")
    private static Integer animationDuration;
    @Value("menuConfiguration.npcDisplayname")
    private static String npcDisplayname;
    @Value("menuConfiguration.npcSkin")
    private static String npcSkin;
    @Value("menuConfiguration.targetServer")
    private static String targetServer;

    private final WeakHashMap<Player, SharedInstance> players = new WeakHashMap<>();
}
