package byow.Core;
import java.util.*;

public class Room {
    private int maxHeight;
    private int maxWidth;
    private final int minHeight = 3;
    private final int minWidth = 3;
    private Position topLeft;
    private Position bottomRight;
    private int area;
    private int teWidth = MapGenerator.getWidth();
    private int teHeight = MapGenerator.getHeight();
    private Random rand;
    private Position start;
    private Position end;
    private String sideStemmed;
    private int roomH;
    private int roomW;
    private int hallLength;
    private String orientation;
    private boolean isOriginal;

    /**
     * end position is part of wall
     * start position is surrounded by wall but is a floor tile
     */
    public Room(Position start, Position end, String side, String o, Random r, boolean i) {
        this.start = start;
        this.end = end;
        this.sideStemmed = side;
        this.hallLength = Math.max(Math.abs(end.getX() - start.getX()),
                Math.abs(end.getY() - start.getY()));
        this.orientation = o;
        this.rand = r;
        this.isOriginal = i;
    }

    /**
     * top left is wall
     * bottom right is also wall
     */
    public Room(int tWidth, int tHeight, Random r) {

        maxHeight = tHeight / 4;
        maxWidth = tWidth / 4;
        int roomHeight = RandomUtils.uniform(r, minHeight, maxHeight);
        int roomWidth = RandomUtils.uniform(r, minWidth, maxWidth);
        area = roomHeight * roomWidth;

        int topLeftX = RandomUtils.uniform(r, tWidth);
        int topLeftY = RandomUtils.uniform(r, tHeight);
        int bottomRightX = topLeftX + roomWidth;
        int bottomRightY = topLeftY - roomHeight;

        this.topLeft = new Position(topLeftX, topLeftY);
        this.bottomRight = new Position(bottomRightX, bottomRightY);

        this.teWidth = tWidth;
        this.teHeight = tHeight;
        this.rand = r;
        this.roomH = topLeftY - bottomRightY + 1;
        this.roomW = bottomRightX - topLeftX + 1;

    }


    public Room makeHallway(Room connectedRoom) {
        /** rooms can have multiple hallways
         * hallways need to eventually be indirectly connected to a room
         *
         */
        HashMap wallsDict = checkWalls(connectedRoom);
        HashMap temp = new HashMap<>(wallsDict);
        for (Object s : wallsDict.keySet()) {
            if (!(boolean) wallsDict.get(s)) {
                temp.remove(s);
            }
        }
        wallsDict = temp;
        int side = RandomUtils.uniform(rand, wallsDict.size());
        String instO = "";
        Object[] objArr = wallsDict.keySet().toArray();
        String[] sideArray = Arrays.asList(objArr).toArray(new String[objArr.length]);
        String sideUsed = sideArray[side];
        ArrayList<Position> hallwayPositions = positionsUsedSide(sideUsed);
        if (sideUsed.equals("left") || sideUsed.equals("right")) {
            instO = "horiz";
        } else if (sideUsed.equals("top") || sideUsed.equals("bottom")) {
            instO = "vert";
        }
        return new Room(hallwayPositions.get(0), hallwayPositions.get(1),
                sideUsed, instO, connectedRoom.rand, true);
    }

    private HashMap<String, Boolean> checkWalls(Room r) {
        HashMap<String, Boolean> wallDict  = new HashMap<String, Boolean>();
        wallDict.put("left", true);
        wallDict.put("top", true);
        wallDict.put("right", true);
        wallDict.put("bottom", true);
        if (r.topLeft.getX() == 0 || r.topLeft.getX() == 1 || r.topLeft.getX() == 2) {
            wallDict.replace("left", false);
        }
        if (r.topLeft.getY() == teHeight || r.topLeft.getY() == teHeight - 1
                || r.topLeft.getY() == teHeight - 2) {
            wallDict.replace("top", false);
        }
        if (r.bottomRight.getX() == teWidth || r.bottomRight.getX() == teWidth - 1
                || r.bottomRight.getX() == teWidth - 2) {
            wallDict.replace("right", false);
        }
        if (r.bottomRight.getY() == 0 || r.bottomRight.getY() == 1
                || r.bottomRight.getY() == 2) {
            wallDict.replace("bottom", false);
        }
        return wallDict;
    }

    private ArrayList<Position> positionsUsedSide(String side) {
        Position startPos;
        Position endPos;
        ArrayList<Position> positions = new ArrayList<>();
        if (side.equals("left")) {
            int randY = RandomUtils.uniform(rand,  bottomRight.getY() + 1, topLeft.getY());
            startPos = new Position(topLeft.getX(), randY);
            int randLength = RandomUtils.uniform(rand, 2, topLeft.getX());
            endPos = new Position(topLeft.getX() - randLength, startPos.getY());
            positions.add(startPos);
            positions.add(endPos);
        }
        if (side.equals("top")) {
            int randX = RandomUtils.uniform(rand, topLeft.getX() + 1, bottomRight.getX());
            startPos = new Position(randX, topLeft.getY());
            int randLength = RandomUtils.uniform(rand, 2, teHeight - topLeft.getY());
            endPos = new Position(startPos.getX(), randLength + topLeft.getY());
            positions.add(startPos);
            positions.add(endPos);
        }
        if (side.equals("right")) {
            int randY = RandomUtils.uniform(rand, bottomRight.getY() + 1, topLeft.getY());
            startPos = new Position(bottomRight.getX(), randY);
            int randLength = RandomUtils.uniform(rand, 2, teWidth - bottomRight.getX());
            endPos = new Position(bottomRight.getX() + randLength, startPos.getY());
            positions.add(startPos);
            positions.add(endPos);
        }
        if (side.equals("bottom")) {
            int randX = RandomUtils.uniform(rand, topLeft.getX() + 1, bottomRight.getX());
            startPos = new Position(randX, bottomRight.getY());
            int randLength = RandomUtils.uniform(rand, 2, bottomRight.getY());
            endPos = new Position(startPos.getX(), bottomRight.getY() - randLength);
            positions.add(startPos);
            positions.add(endPos);
        }
        return positions;
    }

    public Room makeHallwayHallway(Room hallway) {
        HashMap wallsDict = checkWallsHallway(hallway);
        HashMap temp = new HashMap<>(wallsDict);
        for (Object s : wallsDict.keySet()) {
            if (!(boolean) wallsDict.get(s)) {
                temp.remove(s);
            }
        }
        wallsDict = temp;

        String instO = "";
        int side = RandomUtils.uniform(rand, wallsDict.size());
        Object[] objArr = wallsDict.keySet().toArray();
        String[] sideArray = Arrays.asList(objArr).toArray(new String[objArr.length]);
        String sideUsed = sideArray[side];
        ArrayList<Position> hallwayPositions = positionsUsedSideHallway(sideUsed, hallway);
        if (sideUsed.equals("left") || sideUsed.equals("right")) {
            instO = "horiz";
        } else if (sideUsed.equals("top") || sideUsed.equals("bottom")) {
            instO = "vert";
        }
        return new Room(hallwayPositions.get(0), hallwayPositions.get(1),
                sideUsed, instO, hallway.rand, false);
    }

    private HashMap<String, Boolean> checkWallsHallway(Room hallway) {
        HashMap<String, Boolean> wallDict  = new HashMap<String, Boolean>();
        wallDict.put("left", true);
        wallDict.put("top", true);
        wallDict.put("right", true);
        wallDict.put("bottom", true);
        if (hallway.end.getX() == 0 || hallway.end.getX() == 1
                || hallway.end.getX() == 2 || hallway.sideStemmed.equals("right")) {
            wallDict.replace("left", false);
        }
        if (hallway.end.getY() == 0 || hallway.end.getY() == 1
                || hallway.end.getY() == 2 || hallway.sideStemmed.equals("top")) {
            wallDict.replace("bottom", false);
        }
        if (hallway.end.getX() == teWidth || hallway.end.getX() == teWidth - 1
                || hallway.end.getX() == teWidth - 2 || hallway.sideStemmed.equals("left")) {
            wallDict.replace("right", false);
        }
        if (hallway.end.getY() == teHeight || hallway.end.getY() == teHeight - 1
                || hallway.end.getY() == teHeight - 2 || hallway.sideStemmed.equals("bottom")) {
            wallDict.replace("top", false);
        }
        return wallDict;
    }

    private ArrayList<Position> positionsUsedSideHallway(String side, Room hallway) {
        Position startPos;
        Position endPos;
        int bigX = Math.max(hallway.start.getX(), hallway.end.getX());
        int smallX = Math.min(hallway.start.getX(), hallway.end.getX());
        int bigY = Math.max(hallway.start.getY(), hallway.end.getY());
        int smallY = Math.min(hallway.start.getY(), hallway.end.getY());
        int randY;
        int randX;
        ArrayList<Position> positions = new ArrayList<>();
        if (side.equals("left")) {
            if (hallway.orientation.equals("horiz")) {
                startPos = new Position(hallway.end.getX(), hallway.end.getY());
            } else {
                if (smallY + 1 == bigY) {
                    randY = bigY;
                } else {
                    randY = RandomUtils.uniform(rand, smallY + 1, bigY);
                }
                startPos = new Position(hallway.start.getX(), randY);
            }
            int randLength = RandomUtils.uniform(rand, 2, startPos.getX());
            endPos = new Position(startPos.getX() - randLength, startPos.getY());
            positions.add(startPos);
            positions.add(endPos);
        }
        if (side.equals("top")) {
            if (hallway.orientation.equals("horiz")) {
                if (smallX + 1 == bigX) {
                    randX = bigX;
                } else {
                    randX = RandomUtils.uniform(rand, smallX + 1, bigX);
                }
                startPos = new Position(randX, hallway.start.getY());
            } else {
                startPos = new Position(hallway.end.getX(), hallway.end.getY());
            }
            int randLength = RandomUtils.uniform(rand, 2, teHeight);
            endPos = new Position(startPos.getX(), startPos.getY() + randLength);
            positions.add(startPos);
            positions.add(endPos);
        }
        if (side.equals("right")) {
            if (hallway.orientation.equals("horiz")) {
                startPos = new Position(hallway.end.getX(), hallway.end.getY());
            } else {
                if (smallY + 1 == bigY) {
                    randY = bigY;
                } else {
                    randY = RandomUtils.uniform(rand, smallY + 1, bigY);
                }
                startPos = new Position(hallway.start.getX(), randY);
            }
            int randLength = RandomUtils.uniform(rand, 2, teWidth - startPos.getX());
            endPos = new Position(startPos.getX() + randLength, startPos.getY());
            positions.add(startPos);
            positions.add(endPos);
        }

        if (side.equals("bottom")) {
            if (hallway.orientation.equals("horiz")) {
                if (smallX + 1 == bigX) {
                    randX = bigX;
                } else {
                    randX = RandomUtils.uniform(rand, smallX + 1, bigX);
                }
                startPos = new Position(randX, hallway.start.getY());
            } else {
                startPos = new Position(hallway.end.getX(), hallway.end.getY());
            }
            int randLength = RandomUtils.uniform(rand, 2, startPos.getY());
            endPos = new Position(startPos.getX(), startPos.getY() - randLength);
            positions.add(startPos);
            positions.add(endPos);
        }
        return positions;
    }


    public Position getTopLeft() {
        return this.topLeft;
    }

    public Position getBottomRight() {
        return this.bottomRight;
    }

    public int getRoomH() {
        return this.roomH;
    }

    public int getRoomW() {
        return this.roomW;
    }

    public String getOrientation() {
        return this.orientation;
    }

    public String getSideStemmed() {
        return this.sideStemmed;
    }

    public Position getStart() {
        return this.start;
    }

    public Position getEnd() {
        return this.end;
    }

    public int getHallLength() {
        return this.hallLength;
    }

    public int getArea() {
        return area;
    }

}
