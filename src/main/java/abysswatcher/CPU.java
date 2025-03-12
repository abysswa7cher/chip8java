package abysswatcher;

import java.util.Arrays;

public class CPU {
    private static final int NUM_V_REGISTERS = 16;
    private static final int PC_START        = 0x200;

    public CPU() {
        init();
    }

    public void printRegisters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < registers.length; i++) {
            if (i % 4 == 0)
                sb.append("\n");
            if (registers[i] == 0)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 0x00");
            else if (registers[i] < 16)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 0x0").append(Integer.toHexString(registers[i]).toUpperCase());
            else
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 0x").append(Integer.toHexString(registers[i]).toUpperCase());
            if (i < registers.length - 1)
                sb.append("   ");
        }
        sb.append("\n").append("[VI: 0x").append(Integer.toHexString(Iregister).toUpperCase());
        sb.append("]\n").append("[PC: 0x").append(Integer.toHexString(PCregister).toUpperCase());
        sb.append("]\n").append("[SP: 0x").append(Integer.toHexString(PCregister).toUpperCase());
        sb.append("]");
        logger.log("Registers: " + sb, ELogLevel.DEBUG);
    }


    // registers
    private int[] registers;
    private int   Iregister;
    private int   PCregister;
    private int   SPregister;

    // helper variables
    private int currentOp          = 0;
    private int currentInstruction = 0;

    private static final Chip8  machine = Chip8.getInstance();
    private static final Logger logger  = Logger.getInstance();


    public void init() {
        PCregister = PC_START;
        SPregister = 0;
        Iregister = 0;
        currentOp = 0;
        currentInstruction = 0;

        registers = new int[NUM_V_REGISTERS];
        Arrays.fill(registers, 0);
    }

    public void fetch() {
        currentOp = machine.getRam()[PCregister] << 8 | machine.getRam()[PCregister + 1];
        PCregister += 2;
    }

    public void decode() {
        currentInstruction = currentOp >> 12;
    }

    public void execute() {
        logger.log(
                "INST " + Integer.toHexString(currentInstruction).toUpperCase(), ELogLevel.DEBUG);

        switch (currentInstruction) {
            case 0x0:
                i00E0();
                printRegisters();
                break;
            case 0x1:
                i1nnn();
                printRegisters();
                break;
            case 0x2:
                i2nnn();
                printRegisters();
                break;
            case 0x3:
                i3xkk();
                printRegisters();
                break;
            case 0x4:
                i4xkk();
                printRegisters();
                break;
            case 0x5:
                i5xy0();
                printRegisters();
                break;
            case 0x6:
                i6xkk();
                printRegisters();
                break;
            case 0x7:
                i7xkk();
                printRegisters();
                break;
//            case 0x8:
//                break;
            case 0x9:
                i9xy0();
                printRegisters();
                break;
            case 0xA:
                iAnnn();
                printRegisters();
                break;
//            case 0xB:
//                break;
//            case 0xC:
//                break;
            case 0xD:
                iDxyn();
                printRegisters();
                machine.printScreen();
                break;
//            case 0xE:
//                break;
//            case 0xF:
//                break;
            default:
                logger.log("Instruction not implemented", ELogLevel.ERROR);
                throw new UnsupportedOperationException();
        }
    }

    public void deinit() {
    }

    private void i00E0() {
        for (int[] row : machine.getScreen())
            Arrays.fill(row, 0);
    }

    private void i1nnn() {
        int addr = currentOp & 0x0FFF;
        logger.log("JP: 0x" + Integer.toHexString(addr), ELogLevel.DEBUG);
        PCregister = addr;
    }

    private void i2nnn() {
        int nnn = currentOp & 0x0FFF;
        machine.getStack()[SPregister] = PCregister;
        SPregister += 1;
        PCregister = nnn;

    }

    private void i3xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        if (registers[x] == kk)
            PCregister += 2;
    }

    private void i4xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        if (registers[x] != kk)
            PCregister += 2;
    }

    private void i5xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];
        if (Vx == Vy)
            PCregister += 2;
    }


    private void i6xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        registers[x] = kk;
    }

    private void i7xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;

        registers[x] += kk;
    }

    private void i9xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];
        if (Vx != Vy)
            PCregister += 2;
    }

    private void iAnnn() {
        Iregister = currentOp & 0xFFF;
    }

    private void iDxyn() {
        int iVx = (currentOp & 0x0F00) >> 8;
        int iVy = (currentOp & 0x00F0) >> 4;
        int x = registers[iVx] % 64;
        int y = registers[iVy] % 32;
        int n = currentOp & 0x000F;

        registers[0xF] = 0;
        int[][] screen = machine.getScreen();
        for (int i = 0; i < n; i++) {
            int pixel = machine.getRam()[Iregister + i];
            for (int j = 0; j < 8; j++) {
                if ((pixel & (0x80 >> j)) != 0) {
                    if (screen[y + i][x + j] == 1)
                        registers[0xF] = 1;

                    screen[y + i][x + j] ^= 0x1;
                }
            }
        }
    }
}
