package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.ArrayList;
import java.util.Random;
import edu.princeton.cs.introcs.StdDraw;

public class MapGenerator {
    private static long SEED;
    private static Random r;
    private static int WIDTH;
    private static int HEIGHT;
    private static ArrayList<Room> roomArray;
    private static WeightedQuickUnionUF connected;
    private static TETile[][] world;
    private static final double PROPORTION = 0.2;

    public MapGenerator(Long seed, int w, int h, TETile[][] world) {
        this.WIDTH = w;
        this.HEIGHT = h;
        this.SEED = seed;
        this.world = world;

    }

    public static void main(String[] args) {
        r = new Random(SEED);
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        connected = new WeightedQuickUnionUF(WIDTH * HEIGHT);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        ArrayList<Room> arrayRooms = new ArrayList<>();
        int mapArea = WIDTH * HEIGHT;
        int totalRoomArea = 0;
        while (totalRoomArea <= mapArea * PROPORTION) {
            Room room = new Room(WIDTH, HEIGHT, r);
            arrayRooms.add(room);
            totalRoomArea += room.getArea();
        }
        roomArray = validateRooms(arrayRooms);
        /** connects all the positions in the WQU for each room. */
        connectRooms();

        ArrayList<Room> hallwaysList = new ArrayList<>();
        /** hallways */
        for (Room room: roomArray) {
            hallwaysList.add(room.makeHallway(room));
        }
        hallwaysList = cleanHallways(hallwaysList, connected);
        hallwaysList = newRandomHallways(hallwaysList);
        hallwaysList = cleanHallways(hallwaysList, connected);


        while (!areRoomsConnected()) {
            hallwaysList = makeTwoRandomHallway(hallwaysList);
            hallwaysList = cleanHallways(hallwaysList, connected);
        }

        /** rooms are all connected; draw all rooms and hallways. */
        for (Room room : roomArray) {
            drawPerimeterRoom(room, world);
        }
        for (Room hallway : hallwaysList) {
            drawPerimeterHallway(hallway, world);
        }
        for (Room room : roomArray) {
            fillRoom(room, world);
        }
        for (Room hallway : hallwaysList) {
            fillHallway(hallway, world);
        }
        /**
         * if (args.length == 0) {
         *             ter.renderFrame(world);
         *         }
         */
       //ter.renderFrame(world);
    }

    public static void connectRooms() {
        for (Room room : roomArray) {
            int topLeftWalkableX = room.getTopLeft().getX();
            int topLeftWalkableY = room.getTopLeft().getY();
            int widthWalkableRoom = room.getRoomW();
            int heightWalkableRoom = room.getRoomH();
            int startPositionWQU = topLeftWalkableY * WIDTH + topLeftWalkableX;

            for (int y = topLeftWalkableY; y > topLeftWalkableY - heightWalkableRoom; y--) {
                for (int x = topLeftWalkableX; x < topLeftWalkableX + widthWalkableRoom; x++) {
                    int positionWQU = y * WIDTH + x;
                    connected.union(startPositionWQU, positionWQU);
                }
            }
        }
    }



    public static boolean areRoomsConnected() {
        int numRoomsConnected = 0;
        int numRooms = roomArray.size();
        Room startRoom = roomArray.get(0);

        Position startRoomPos = startRoom.getTopLeft();
        int startRoomTopLeftX = startRoomPos.getX();
        int startRoomTopLeftY = startRoomPos.getY();
        int startRoomPosWQU = startRoomTopLeftY * WIDTH + startRoomTopLeftX;

        for (Room room: roomArray) {
            Position roomPos = room.getTopLeft();
            int roomTopLeftX = roomPos.getX();
            int roomTopLeftY = roomPos.getY();
            int roomPosWQU = roomTopLeftY * WIDTH + roomTopLeftX;
            if (connected.connected(roomPosWQU, startRoomPosWQU)) {
                numRoomsConnected += 1;
            }

        }

        return numRoomsConnected == numRooms;
    }

    public static ArrayList<Room> validateRooms(ArrayList<Room> array) {
        ArrayList<Room> goodRooms = new ArrayList<>(array);
        for (int i = 0; i < array.size(); i++) {
            for (int j = i + 1; j < array.size() - 1; j++) {
                if (overlapRoom(array.get(i), array.get(j))) {
                    goodRooms.remove(array.get(j));
                }
            }
        }
        ArrayList<Room> updateRooms = new ArrayList<>(goodRooms);
        for (Room room : goodRooms) {
            if (!roomInBounds(room)) {
                updateRooms.remove(room);
            }
        }
        return updateRooms;
    }

    /**returns true if overlaps**/
    private static boolean overlapRoom(Room a, Room b) {
        if (a.getTopLeft().getX() >= b.getBottomRight().getX()
                || b.getTopLeft().getX() >= a.getBottomRight().getX()) {
            return false;
        }

        if (a.getTopLeft().getY() <= b.getBottomRight().getY()
                || b.getTopLeft().getY() <= a.getBottomRight().getY()) {
            return false;
        }

        return true;
    }

    /**returns true if room is within TETile boundaries**/
    private static boolean roomInBounds(Room room) {
        if (room.getBottomRight().getX() >= WIDTH || room.getTopLeft().getY() >= HEIGHT) {
            return false;
        }
        if (room.getBottomRight().getX() < 0
                || room.getBottomRight().getY() < 0 || room.getTopLeft().getY() < 0
                || room.getTopLeft().getX() < 0) {
            return false;
        }
        return true;
    }

    private static boolean hallInBounds(Room hall) {
        if (hall.getOrientation().equals("horiz")) {
            if (hall.getEnd().getX() <= 1 || hall.getEnd().getX() >= WIDTH - 1) {
                return false;
            }
        } else if (hall.getOrientation().equals("vert")) {
            if (hall.getEnd().getY() <= 1 || hall.getEnd().getY() >= HEIGHT - 1) {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<Room> cleanHallways(ArrayList<Room> hallwaysList,
                                                 WeightedQuickUnionUF connectedUF) {
        ArrayList<Room> temp2 = new ArrayList<>(hallwaysList);
        for (Room hall : hallwaysList) {
            if (hall.getHallLength() == 1) {
                temp2.remove(hall);
            } else if (!hallInBounds(hall)) {
                temp2.remove(hall);
            } else {
                Position startPos = hall.getStart();
                Position endPos = hall.getEnd();
                int startX = startPos.getX();
                int startY = startPos.getY();
                int endX = endPos.getX();
                int endY = endPos.getY();
                int startPosWQU = startY * WIDTH + startX;
                int currentPosWQU = startPosWQU;

                if (hall.getSideStemmed().equals("left")) {
                    for (int i = startX; i > endX; i--) {
                        connectedUF.union(startPosWQU, currentPosWQU);
                        currentPosWQU = currentPosWQU - 1;
                    }
                } else if (hall.getSideStemmed().equals("right")) {
                    for (int i = startX; i < endX; i++) {
                        connectedUF.union(startPosWQU, currentPosWQU);
                        currentPosWQU = currentPosWQU + 1;
                    }
                } else if (hall.getSideStemmed().equals("top")) {
                    for (int i = startY; i < endY; i++) {
                        connectedUF.union(startPosWQU, currentPosWQU);
                        currentPosWQU = currentPosWQU + WIDTH;
                    }
                } else {
                    for (int i = startY; i > endY; i--) {
                        connectedUF.union(startPosWQU, currentPosWQU);
                        currentPosWQU = currentPosWQU - WIDTH;
                    }
                }
            }
        }
        return temp2;
    }

    private static ArrayList<Room> makeTwoRandomHallway(ArrayList<Room> hallways) {
        ArrayList<Room> allHallways = new ArrayList<>(hallways);
        ArrayList<Room> twoRandomHalls = new ArrayList<>();

        boolean twoRoomsConnected = false;

        /** continue making two halls until the halls connect with the same room. */
        while (!twoRoomsConnected) {
            WeightedQuickUnionUF tempWQU = connected.clone();
            for (int i = 0; i < 2; i++) {
                int randomHallwayIndex = RandomUtils.uniform(r, 0, hallways.size());
                Room randomHallway = allHallways.get(randomHallwayIndex);
                Room newHallway = randomHallway.makeHallwayHallway(randomHallway);
                allHallways.add(newHallway);
                twoRandomHalls.add(newHallway);
            }
            allHallways = cleanHallways(allHallways, tempWQU);
            if (allHallways.contains(twoRandomHalls.get(0))
                    && allHallways.contains(twoRandomHalls.get(1))) {
                Position pos1Start = twoRandomHalls.get(0).getStart();
                Position pos2Start = twoRandomHalls.get(1).getStart();
                int pos1X = pos1Start.getX();
                int pos1Y = pos1Start.getY();
                int pos2X = pos2Start.getX();
                int pos2Y = pos2Start.getY();
                int pos1WQU = pos1Y * WIDTH + pos1X;
                int pos2WQU = pos2Y * WIDTH + pos2X;
                if (tempWQU.connected(pos1WQU, pos2WQU)) {
                    twoRoomsConnected = true;
                } else {
                    allHallways.remove(twoRandomHalls.get(0));
                    allHallways.remove(twoRandomHalls.get(1));
                    twoRandomHalls.clear();
                }
            } else {
                allHallways.remove(twoRandomHalls.get(0));
                allHallways.remove(twoRandomHalls.get(1));
                twoRandomHalls.clear();
            }
        }

        return allHallways;
    }

    private static ArrayList<Room> newRandomHallways(ArrayList<Room> hallways) {
        ArrayList<Room> allHallways = new ArrayList<>(hallways);
        for (Room hall : hallways) {
            int randNumberHallways = RandomUtils.uniform(r, 5, 11);
            while (randNumberHallways > 0) {
                Room newHallway = hall.makeHallwayHallway(hall);
                allHallways.add(newHallway);
                randNumberHallways--;
            }
        }
        return allHallways;

    }

    private static void drawPerimeterRoom(Room room, TETile[][] worlds) {
        for (int i = room.getTopLeft().getX(); i <= room.getBottomRight().getX(); i++) {
            worlds[i][room.getTopLeft().getY()] = Tileset.WALL;
            worlds[i][room.getBottomRight().getY()] = Tileset.WALL;
        }
        for (int j = room.getBottomRight().getY(); j <= room.getTopLeft().getY(); j++) {
            worlds[room.getTopLeft().getX()][j] = Tileset.WALL;
            worlds[room.getBottomRight().getX()][j] = Tileset.WALL;
        }
    }
    private static void drawPerimeterHallway(Room hallway, TETile[][] worlds) {
        Position start = hallway.getStart();
        Position end = hallway.getEnd();
        if (hallway.getOrientation().equals("horiz")) {
            if (hallway.getSideStemmed().equals("left")) {
                for (int i = end.getX(); i <= start.getX(); i++) {
                    worlds[i][start.getY() + 1] = Tileset.WALL;
                    worlds[i][start.getY() - 1] = Tileset.WALL;
                }
            } else if (hallway.getSideStemmed().equals("right")) {
                for (int i = start.getX(); i <= end.getX(); i++) {
                    worlds[i][start.getY() + 1] = Tileset.WALL;
                    worlds[i][start.getY() - 1] = Tileset.WALL;
                }
            }
        } else if (hallway.getOrientation().equals("vert")) {
            if (hallway.getSideStemmed().equals("top")) {
                for (int j = start.getY(); j <= end.getY(); j++) {
                    worlds[start.getX() - 1][j] = Tileset.WALL;
                    worlds[start.getX() + 1][j] = Tileset.WALL;
                }
            } else if (hallway.getSideStemmed().equals("bottom")) {
                for (int j = end.getY(); j <= start.getY(); j++) {
                    worlds[start.getX() - 1][j] = Tileset.WALL;
                    worlds[start.getX() + 1][j] = Tileset.WALL;
                }
            }
        }
        worlds[end.getX()][end.getY()] = Tileset.WALL;
    }

    private static void fillRoom(Room room, TETile[][] worlds) {
        for (int x = room.getTopLeft().getX() + 1; x <= room.getBottomRight().getX() - 1; x++) {
            for (int y = room.getBottomRight().getY() + 1;
                 y <= room.getTopLeft().getY() - 1; y++) {
                worlds[x][y] = Tileset.FLOOR;
            }
        }
    }

    private static void fillHallway(Room hallway, TETile[][] worlds) {
        Position start = hallway.getStart();
        Position end = hallway.getEnd();
        if (hallway.getOrientation().equals("horiz")) {
            if (hallway.getSideStemmed().equals("left")) {
                for (int i = end.getX() + 1; i < start.getX(); i++) {
                    worlds[i][start.getY()] = Tileset.FLOOR;
                }
            } else if (hallway.getSideStemmed().equals("right")) {
                for (int i = start.getX(); i < end.getX(); i++) {
                    worlds[i][start.getY()] = Tileset.FLOOR;
                }
            }
        }
        if (hallway.getOrientation().equals("vert")) {
            if (hallway.getSideStemmed().equals("top")) {
                for (int j = start.getY(); j < end.getY(); j++) {
                    worlds[start.getX()][j] = Tileset.FLOOR;
                }
            } else if (hallway.getSideStemmed().equals("bottom")) {
                for (int j = end.getY() + 1; j < start.getY(); j++) {
                    worlds[start.getX()][j] = Tileset.FLOOR;
                }
            }
        }
        worlds[start.getX()][start.getY()] = Tileset.FLOOR;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static TETile[][] getWorld() {
        return world;
    }

}
