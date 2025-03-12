package abysswatcher;

import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;

public class Chip8 {
    private Chip8() {
    }

    @Getter
    private static final Chip8     instance  = new Chip8();
    private static final Logger    logger    = Logger.getInstance();
    private static final CPU       cpu       = new CPU();
    private static       RomLoader romLoader = new RomLoader();

    @Getter
    private final int[][] screen = new int[Chip8Specs.SCREEN_HEIGHT][Chip8Specs.SCREEN_WIDTH];

    private final int[]   keys        = new int[Chip8Specs.NUM_KEYS];
    private       Boolean key_pressed = false;

    @Getter
    private final int[] ram   = new int[Chip8Specs.TOTAL_RAM];
    @Getter
    private final int[] stack = new int[Chip8Specs.STACK_SIZE];

    private char delay_timer = 0;
    private char sound_timer = 0;

    private Boolean emulatorRunning = false;

    public void printRAM() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ram.length; i++) {
            if (i % 16 == 0)
                sb.append("\n");
            if (ram[i] == 0)
                sb.append("0x00");
            else if (ram[i] < 16)
                sb.append("0x0").append(Integer.toHexString(ram[i]).toUpperCase());
            else
                sb.append("0x").append(Integer.toHexString(ram[i]).toUpperCase());
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

    public void run() {
        emulatorRunning = true;

        while (emulatorRunning) {
            cpu.fetch();
            cpu.decode();
            cpu.execute();
        }
    }

    public void deinit() {
        cpu.deinit();
    }

}
