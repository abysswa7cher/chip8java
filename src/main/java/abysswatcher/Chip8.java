package abysswatcher;

import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class Chip8 {
    private Chip8() {}

    @Getter
    private static final Chip8       instance     = new Chip8();
    private static final PrintLogger PRINT_LOGGER = PrintLogger.getInstance();
    private static final WriteLogger WRITE_LOGGER = WriteLogger.getInstance();

    private static final CPU cpu;

    static {
        try {
            cpu = new CPU();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static RomLoader romLoader = new RomLoader();

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

    public Boolean emulatorRunning = false;

    public String dumpState(Boolean withRam) {
        int[] registers = cpu.getRegisters();
        int Iregister = cpu.getIregister();
        int PCregister = cpu.getPCregister();
        int SPregister = cpu.getSPregister();
        int[] stack = getStack();

        String IregisterS = Integer.toHexString(Iregister).toUpperCase();
        String PCregisterS = Integer.toHexString(PCregister).toUpperCase();
        String SPregisterS = Integer.toHexString(SPregister).toUpperCase();
        String ramAtI = Integer.toHexString(ram[Iregister]).toUpperCase();
        String ramAtPC = Integer.toHexString(ram[PCregister]).toUpperCase();
        String ramAtPC2 = Integer.toHexString(ram[PCregister + 1]).toUpperCase();
        String ramAtSP = Integer.toHexString(ram[SPregister]).toUpperCase();


        StringBuilder sb = new StringBuilder();
        if (withRam)
            sb.append(printRAM()).append("\n\n======================================================================\n\n");

        for (int i = 0; i < registers.length; i++) {
            String ihex = Integer.toHexString(i).toUpperCase();
            String regval = Integer.toHexString(registers[i]);
            if (i % 4 == 0)
                sb.append("\n");
            if (registers[i] == 0)
                sb.append(ihex).append(": 00");
            else if (registers[i] < 16)
                sb.append(ihex).append(": 0").append(regval);
            else
                sb.append(ihex).append(": ").append(regval);
            if (i < registers.length - 1)
                sb.append("   ");
        }

        sb.append("\n").append("VI: ").append(IregisterS).append(" [").append(ramAtI).append("]").append("\n")
          .append("PC: ").append(PCregisterS).append(" [").append(ramAtPC).append(" ").append(ramAtPC2).append("]").append("\n")
          .append("SP: ").append(SPregisterS).append(" [").append(ramAtSP).append("]").append("\n")
          .append("Stack: ").append(Arrays.toString(stack)).append("\n\n");
        return sb.toString();
    }

    public String printRAM() {
        StringBuilder sb = new StringBuilder();
        int printCount = 0;
        for (int i = 0; i < ram.length; i++) {
            if (ram[i] > 0 || (i >= 512 && i <= 512 + cpu.programSize)) {
                if (printCount % 10 == 0)
                    sb.append("\n");
                sb.append(Integer.toHexString(i).toUpperCase()).append(": ");
//            if (ram[i] == 0)
//                sb.append("00");
                if (ram[i] < 16)
                    sb.append("0").append(Integer.toHexString(ram[i]).toUpperCase());
                else
                    sb.append(Integer.toHexString(ram[i]).toUpperCase());
                if (i < ram.length - 1)
                    sb.append(' ');
                printCount++;
            }
        }
        return sb.toString();
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
            PRINT_LOGGER.log("Failed to read the ROM\n", ELogLevel.ERROR);
            throw new RuntimeException(e);
        } catch (Exception e) {
            PRINT_LOGGER.log("Failed to initialize the Chip8 system\n", ELogLevel.ERROR);
            throw new RuntimeException(e);
        }
        WRITE_LOGGER.log(
                printRAM() + "\n\n=======================================================\n\n");
    }

    public void run() throws IOException {
        emulatorRunning = true;
        int lastOp = 0;
        while (emulatorRunning) {
            cpu.fetch();
            cpu.decode();
            cpu.execute();
            if (cpu.getPCregister() == lastOp) emulatorRunning = false;
            lastOp = cpu.getPCregister();
//            if (cpu.getPCregister() == 0x49C)
//                emulatorRunning = false;
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
