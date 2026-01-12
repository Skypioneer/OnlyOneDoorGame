import java.util.*;

/**
 * Container for all game levels.
 */
public class LevelSet {
    public final List<Level> levels = new ArrayList<>();

    public LevelSet() {
        levels.add(new Level(new String[]{
                "####################################",
                "#                             dD#   #",
                "#                             dd#   #",
                "#                      u5 U5###   #   #",
                "#                      ###   ## #   #",
                "#                k1 K1###T1      t1     #",
                "#             p1u2u2###        u4###    #",
                "#       u1    o#P1P1 U6U6       U4    u3u3u3u3#",
                "#        U1U1###U2U2U2      #####w3       #",
                "#       ###T2    t2u6w2    W2            #",
                "#    ###    W3W3W3W3W3W3W3W3W3W3              #",
                "#S###G   t4 T4W1w1t3               T3U3U3U3U3U3#",
                "####################################"
        }, GamePanel.TILE_SIZE));

        levels.add(new Level(new String[]{
                "####################################",
                "#  S   ##    u3  ###   K4t6K5 T6###T4 G    #",
                "#  ##  ##    U3u4  #     #   # # ###t4  #",
                "#u1u1# u2       # U4  #  k4  k5#w3  # #     #",
                "# U1#w1  t2  T2#   #     # J3 #W3# k6 #   # #",
                "#  #   ###  ##k2  #     #j3 ^###t5  W4W4W4 #",
                "#  #  W1#K1#   #    #       #T5  K6      #",
                "#  # U2 # #p2  #  ###W2  ### #   ###    #",
                "#  ### # #P2P2 #  #K3#   # # #   #w4###  #",
                "#    #U2# #   #p1 # #  W2#w2# #j2j2j2#D# u6u6##",
                "#    ### #   #  # #W2  # #^#J2  #d#    #",
                "#U1     k1j1J1J1J1 k2K2 k3P1P1P1t3   T3###J2J2    W4U6U6#",
                "####################################"
        }, GamePanel.TILE_SIZE));

        levels.add(new Level(new String[]{
                "####################################",
                "#                         U5W1 t4   K7T4#",
                "#            T4          u5  w1 W1k7W1   #",
                "#                       p2####### dd#",
                "#           t2      p1u4###P2        dD#",
                "#T2             ^T2--#P1   ^^     u2####",
                "#     u9  u1U1  ####^^U4U4              #",
                "# u8U8k8  U9 ###-    U7                 #",
                "# ###W4#### ^^  u7W3W3W3W3W3            w3w3#",
                "#T1St1w4K8#W2T5  t5w2          u6^          #",
                "#####P3   K1 W2W2W2W2W2W2W2W2W2W2     - W3 W3 W3W3W3#",
                "#GJ2j2   t6 k1T6W2 w2t3       U6U6J1j1U2U2U2U2U2U2 T3 #",
                "####################################"
        }, GamePanel.TILE_SIZE));

        levels.add(new Level(new String[]{
                "####################################",
                "#  S           K2      #T2     t2       #",
                "#  ####           t4   #  #######u6   j1#",
                "#u1u1#   ##  u2  k2 w2p1   T4##  w8W8         #",
                "#  #     ## U2 ###P1###     T5#U22 u22J1 j2 u21 #",
                "#  #       ##       #t5p2 ###u5 t6## t22 U21##",
                "#  #         ##      #P2#    T6#T22  J2 # #",
                "#U1U1#           ##T1  t1 u3K1  ^#j3#  U6#U6  #",
                "#                u4####  ^   J3   ###  #",
                "#U8U8U8U8U9U9U9U9U10U10U10U10u10u9u8        ^       U12U12U12^#",
                "#F2F2F2F2F2                        u11u11U11 u12  #",
                "#D      t3w3 T3W3   U4U4U7U7U7U7U3U3U3U3 ^u7 ^    k1f2#",
                "####################################"
        }, GamePanel.TILE_SIZE));
    }
}