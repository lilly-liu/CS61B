package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.util.*;
import java.io.*;
import java.awt.event.MouseEvent;
import java.security.Key;

public class Engine {
    private static final int KEYBOARD = 0;
    private static final int RANDOM = 1;
    private static final char STRING = 's';
    private TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 60;
    public static final int HEIGHT = 30;
    private int event = MouseEvent.MOUSE_MOVED;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        InputSource inputSource = new KeyboardInputSource();
        inputSource.getNextKey();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {

        input = input.toLowerCase();
        InputSource inputSource = new StringInputDevice(input);
        char inputType = inputSource.getNextKey();
        HashMap<String, Integer> avatarPos;
        String stringSeed = "";
        String stringMoves = "";
        if (inputType == 'n') {
            while (inputSource.possibleNextInput()) {
                char c = inputSource.getNextKey(); //1
                if (c == '0') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '1') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '2') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '3') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '4') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '5') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '6') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '7') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '8') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == '9') {
                    stringSeed = stringSeed + Character.toString(c);
                } else if (c == 's') {
                    break;
                }
                //123
            }
            while (inputSource.possibleNextInput()) {
                char c = inputSource.getNextKey();
                if (c == 'w') {
                    stringMoves += Character.toString('w');
                } else if (c == 'a') {
                    stringMoves += Character.toString('a');
                } else if (c == 'd') {
                    stringMoves += Character.toString('d');
                } else if (c == 's') {
                    stringMoves += Character.toString('s');
                } else if (c == ':') {
                    try {
                        File file = new File(System.getProperty("user.dir") + "/saved.txt"); //Does this error if file does
                        //not exist initially
                        if(file.delete())
                        {
                            System.out.println("File deleted successfully");
                        }
                        else
                        {
                            System.out.println("Failed to delete the file");
                        }
                        File savedWorld = new File(System.getProperty("user.dir") + "/saved.txt");
                        System.out.println("new saved file");
                        FileWriter writeToFile = new FileWriter(savedWorld);
                        BufferedWriter bw = new BufferedWriter(writeToFile);
                        bw.write(stringSeed); //123
                        /*BufferedReader brTest = new BufferedReader(new FileReader(savedWorld));*/
                        bw.newLine();
                        bw.write(stringMoves); //wasd
                        bw.close();
                        /*System.out.println(brTest.readLine());
                        System.out.println(brTest.readLine());*/
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else if (inputType == 'l') {
            String path = System.getProperty("user.dir") + "/saved.txt";
            try {
                FileReader fr = new FileReader(path);
                BufferedReader br = new BufferedReader(fr);
                stringSeed = br.readLine();
                stringMoves = br.readLine();
                br.close();
            } catch (Exception ex) {
                System.out.println("An error occurred.");
                ex.printStackTrace();
            }
            while (inputSource.possibleNextInput()) {
                char c = inputSource.getNextKey();
                if (c == 'w') {
                    stringMoves += Character.toString('w');
                } else if (c == 'a') {
                    stringMoves += Character.toString('a');
                } else if (c == 'd') {
                    stringMoves += Character.toString('d');
                } else if (c == 's') {
                    stringMoves += Character.toString('s');
                } else if (c == ':') {
                    try {
                        File file = new File(System.getProperty("user.dir") + "/saved.txt");
                        if(file.delete())
                        {
                            System.out.println("File deleted successfully");
                        }
                        else
                        {
                            System.out.println("Failed to delete the file");
                        }
                        File savedWorld = new File(System.getProperty("user.dir") + "/saved.txt");
                        FileWriter writeToFile = new FileWriter(savedWorld);
                        BufferedWriter bw = new BufferedWriter(writeToFile);
                        bw.write(stringSeed); //123
                        bw.newLine();
                        bw.write(stringMoves); //wasdwasd
                        bw.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        String[] mainArgument = new String[] {stringSeed};
        /** 0th input is seed. */
        MapGenerator map = new MapGenerator(Long.parseLong(mainArgument[0]),
                WIDTH, HEIGHT, finalWorldFrame);
        map.main(mainArgument); //Generates map
        System.out.println(stringSeed);
        System.out.println(stringMoves);
        if (inputType == 'n' || inputType == 'l') {
            avatarPos = initialAvatarPos(map);
            char[] charMoves = new char[stringMoves.length()];
            for (int i = 0; i < stringMoves.length(); i++) {
                charMoves[i] = stringMoves.charAt(i);
            }
            for (char c : charMoves) {
                moveAvatar(c, avatarPos, finalWorldFrame);
            }
            System.out.println(avatarPos.get("x") + " " + avatarPos.get("y"));
            finalWorldFrame[avatarPos.get("x")][avatarPos.get("y")] = Tileset.AVATAR;
        }

        //TODO: 1. figure out hud: does it use mouselistener or something like that
        //      Friday: HUD + figure out rendering @ problem
        //      Saturday: :)
        //      Sunday: interactWithKeyboard
        //      Monday: Add menu option to change avatar appearance secondary feature


        System.out.println(TETile.toString(finalWorldFrame));
//        System.out.println(map.getWorld()[30][10].description());
        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }


    private HashMap<String, Integer> initialAvatarPos(MapGenerator map) {
        TETile[][] worldTile = map.getWorld();
        HashMap<String, Integer> avatarXY = new HashMap<>();
        int startX;
        int startY;
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (worldTile[j][i] == Tileset.FLOOR) {
                    startX = j;
                    startY = i;
                    avatarXY.put("x", startX);
                    avatarXY.put("y", startY);
                    return avatarXY;
                }
            }
        }
        return null;
    }

    private void moveAvatar(char c, HashMap<String, Integer> aPos, TETile[][] worldFrame) {
        int x = aPos.get("x");
        int y = aPos.get("y");
        if (c == 'w') {
            if (worldFrame[x][y + 1] == Tileset.FLOOR) {
                aPos.replace("y", y + 1);
            }
            return;
        } else if (c == 'a') {
            if (worldFrame[x - 1][y] == Tileset.FLOOR) {
                aPos.replace("x", x - 1);
            }
            return;
        } else if (c == 's') {
            if (worldFrame[x][y - 1] == Tileset.FLOOR) {
                aPos.replace("y", y - 1);
            }
            return;
        } else if (c == 'd') {
            if (worldFrame[x + 1][y] == Tileset.FLOOR) {
                aPos.replace("x", x + 1);
            }
            return;
        }
    }

    public void hud(MapGenerator m) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        TETile[][] worldTile = m.getWorld();

        TETile tile = worldTile[x][y];
        //StdDraw.mouseMoved(event);

        StdDraw.text(1, HEIGHT - 1, tile.description());
        //TODO: find where to put hud
    }


}
