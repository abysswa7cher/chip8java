package abysswatcher;

import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Chip8 {
    private Chip8() {
    }

    @Getter
    private static final Chip8     instance  = new Chip8();
    private static final Logger    logger    = Logger.getInstance();
    private static final CPU       cpu;

    static {
        try {
            cpu = new CPU();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static       RomLoader romLoader = new RomLoader();

    @Getter
    private final int[][] screen = new int[Chip8Specs.SCREEN_HEIGHT][Chip8Specs.SCREEN_WIDTH];

    private final int[]   keys        = new int[Chip8Specs.NUM_KEYS];
    private       Boolean key_pressed = false;

    @Getter
    private final int[] ram   = new int[Chip8Specs.TOTAL_RAM];
    @Getter
    private final int[] stack = new int[Chip8Specs.STACK_SIZE];

    @Getter
    @Setter
    private int delay_timer = 0;
    @Getter
    @Setter
    private int sound_timer = 0;

    private Boolean emulatorRunning = false;

    public void printRAM() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ram.length; i++) {
            if (i % 10 == 0)
                sb.append("\n");
            sb.append(Integer.toHexString(i).toUpperCase()).append(": ");
            if (ram[i] == 0)
                sb.append("00");
            else if (ram[i] < 16)
                sb.append("0").append(Integer.toHexString(ram[i]).toUpperCase());
            else
                sb.append(Integer.toHexString(ram[i]).toUpperCase());
            if (i < ram.length - 1)
                sb.append(' ');
        }
        logger.log(sb.toString(), ELogLevel.DEBUG);
    }

    public void printScreen() {
        for (int[] ints : screen) {
            for (int p : ints) {
                System.out.print(p == 1 ? "0" : " ");
            }
            System.out.print("\n");
        }
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    public void init(String romPath) {

        try {
            // clear screen
            for (int[] arr : screen)
                Arrays.fill(arr, (byte) 0);

            // clear stack and ram
            Arrays.fill(stack, (byte) 0);
            Arrays.fill(ram, (byte) 0);

            // copy font to ram
            System.arraycopy(Chip8Specs.FONTSET, 0, ram, 0, Chip8Specs.FONTSET_SIZE);

            // reset timers
            delay_timer = 0;
            sound_timer = 0;

            // reset keys
            Arrays.fill(keys, (byte) 0);

            key_pressed = false;

            romLoader.loadRom(romPath, ram, Chip8Specs.PROGRAM_START_ADDR);
            printRAM();
            romLoader = null;

        } catch (IOException e) {
            logger.log("Failed to read the ROM\n", ELogLevel.ERROR);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.log("Failed to initialize the Chip8 system\n", ELogLevel.ERROR);
            throw new RuntimeException(e);
        }
    }

    public void run() throws IOException {
        emulatorRunning = true;

        while (emulatorRunning) {
            cpu.fetch();
            cpu.decode();
            cpu.execute();
//            int input = System.in.read();
//
//            if (input == 'q')
//                emulatorRunning = false;
        }
    }

    public void deinit() {
        cpu.deinit();
    }

}
