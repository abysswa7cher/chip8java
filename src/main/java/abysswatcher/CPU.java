package abysswatcher;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

public class CPU {
    private static final int NUM_V_REGISTERS = 16;
    private static final int PC_START        = 0x200;

    public CPU() throws FileNotFoundException {
        init();
    }

    public File dump;

    // registers
    private int[] registers;
    public void printRegisters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < registers.length; i++) {
            if (i % 4 == 0)
                sb.append("\n");
            if (registers[i] == 0)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 00");
            else if (registers[i] < 16)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 0").append(Integer.toHexString(registers[i]).toUpperCase());
            else
                sb.append(Integer.toHexString(i).toUpperCase()).append(": ").append(Integer.toHexString(registers[i]).toUpperCase());
            if (i < registers.length - 1)
                sb.append("   ");
        }
        logger.log("Registers: " + sb, ELogLevel.DEBUG);
    }

    private int   Iregister;
    private void printIReg() {
        logger.log("VI: " + Integer.toHexString(Iregister).toUpperCase() + " [" + Integer.toHexString(machine.getRam()[Iregister]).toUpperCase() + "]", ELogLevel.DEBUG);

    }

    private int   PCregister;
    private void printPCReg() {
        logger.log("PC: " + Integer.toHexString(PCregister).toUpperCase() + " [" + Integer.toHexString(machine.getRam()[PCregister]).toUpperCase() + " " + Integer.toHexString(machine.getRam()[PCregister+1]).toUpperCase() + "]", ELogLevel.DEBUG);

    }

    private int   SPregister;
    private void printSPReg() {
        logger.log("SP: " + Integer.toHexString(Iregister).toUpperCase() + " [" + Integer.toHexString(machine.getRam()[SPregister]).toUpperCase() + "]", ELogLevel.DEBUG);

    }

    private void printStack() {
        logger.log("Stack: " + Arrays.toString(machine.getStack()), ELogLevel.DEBUG);
    }

    // helper variables
    private int currentOp          = 0;
    private int currentInstruction = 0;

    private static final Chip8  machine = Chip8.getInstance();
    private static final Logger logger  = Logger.getInstance();

    public void init() throws FileNotFoundException {
        PCregister = PC_START;
        SPregister = 0;
        Iregister = 0;
        currentOp = 0;
        currentInstruction = 0;

        registers = new int[NUM_V_REGISTERS];
        Arrays.fill(registers, 0);
        dump = new File("D:\\projects\\java\\chip8\\dump.txt");
    }

    public void fetch() {
        currentOp = machine.getRam()[PCregister] << 8 | machine.getRam()[PCregister + 1];
        PCregister += 2;
    }

    public void decode() {
        currentInstruction = currentOp >> 12;
//        logger.log("Op: " + Integer.toHexString(currentOp).toUpperCase(), ELogLevel.DEBUG);
    }
    @SneakyThrows
    public void execute() {
        switch (currentInstruction) {
            case 0x0:
                decodei0();
                break;
            case 0x1:
                i1nnn();
                break;
            case 0x2:
                i2nnn();
                break;
            case 0x3:
                i3xkk();
                break;
            case 0x4:
                i4xkk();
                break;
            case 0x5:
                i5xy0();
                break;
            case 0x6:
                i6xkk();
                break;
            case 0x7:
                i7xkk();
                break;
            case 0x8:
                decodei8();
                break;
            case 0x9:
                i9xy0();
                break;
            case 0xA:
                iAnnn();
                break;
//            case 0xB:
//                break;
//            case 0xC:
//                break;
            case 0xD:
                iDxyn();
//                machine.clearScreen();
//                machine.printScreen();

//                System.in.read();
                break;
//            case 0xE:
//                break;
            case 0xF:
                decodeiF();
                break;
            default:
                logger.log("Instruction not implemented", ELogLevel.ERROR);
                throw new UnsupportedOperationException();
        }
        try (FileWriter out = new FileWriter(dump, true)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < registers.length; i++) {
                if (i % 4 == 0)
                    sb.append("\n");
                if (registers[i] == 0)
                    sb.append(Integer.toHexString(i).toUpperCase()).append(": 00");
                else if (registers[i] < 16)
                    sb.append(Integer.toHexString(i).toUpperCase()).append(": 0").append(Integer.toHexString(registers[i]).toUpperCase());
                else
                    sb.append(Integer.toHexString(i).toUpperCase()).append(": ").append(Integer.toHexString(registers[i]).toUpperCase());
                if (i < registers.length - 1)
                    sb.append("   ");
            }
            sb.append("\n").append("VI: ").append(Integer.toHexString(Iregister).toUpperCase()).append(" [").append(Integer.toHexString(machine.getRam()[Iregister]).toUpperCase()).append("]");
            sb.append("\n").append("PC: ").append(Integer.toHexString(PCregister).toUpperCase()).append(" [").append(Integer.toHexString(machine.getRam()[PCregister]).toUpperCase()).append(" ").append(Integer.toHexString(machine.getRam()[PCregister + 1]).toUpperCase()).append("]");
            sb.append("\n").append("SP: ").append(Integer.toHexString(Iregister).toUpperCase()).append(" [").append(Integer.toHexString(machine.getRam()[SPregister]).toUpperCase()).append("]");
            sb.append("\n").append("Stack: ").append(Arrays.toString(machine.getStack())).append("\n");
            out.write(sb);
        }
    }

    public void deinit() {
    }


    // clear the screen //
    private void i00E0() {
        logger.log("OP 00E0 CLS", ELogLevel.DEBUG);
        for (int[] row : machine.getScreen())
            Arrays.fill(row, 0);
        printPCReg();
    }

    // return from subroutine to address pulled from stack //
    private void i00EE() {
        logger.log("OP 00EE RET", ELogLevel.DEBUG);
        SPregister -= 1;
        PCregister = machine.getStack()[SPregister];
        printPCReg();
    }

    // jump to address NNN //
    private void i1nnn() {
        int addr = currentOp & 0x0FFF;
        logger.log("OP " + currentOp + " JP " + Integer.toHexString(addr).toUpperCase(), ELogLevel.DEBUG);
        PCregister = addr;
        printPCReg();
    }

    // push return address onto stack and call subroutine at address NNN //
    private void i2nnn() {
        SPregister += 1;
        int nnn = currentOp & 0xFFF;
        int[] stack = machine.getStack();
        stack[SPregister] = PCregister;
        PCregister = nnn;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                " CALL " + Integer.toHexString(nnn).toUpperCase(),
                ELogLevel.DEBUG);
        printPCReg();
        printStack();
    }

    private void i3xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SE V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(kk).toUpperCase(),
                ELogLevel.DEBUG);

        printRegisters();
        if (registers[x] == kk)
            PCregister += 2;
        printPCReg();
    }

    private void i4xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SNE V" + Integer.toHexString(x).toUpperCase() +
                        ", " + Integer.toHexString(kk).toUpperCase(),
                ELogLevel.DEBUG);
        printRegisters();
        if (registers[x] != kk)
            PCregister += 2;
        printPCReg();
    }

    private void i5xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SE V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);
        printRegisters();
        if (Vx == Vy)
            PCregister += 2;
        printPCReg();
    }


    private void i6xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " LD V" + Integer.toHexString(x).toUpperCase() +
                        ", " + Integer.toHexString(kk).toUpperCase(),
                ELogLevel.DEBUG);
        registers[x] = kk;
        printRegisters();
        printPCReg();
    }

    private void i7xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " LD V" + Integer.toHexString(x).toUpperCase() +
                        ", " + Integer.toHexString(kk).toUpperCase(),
                ELogLevel.DEBUG);
        registers[x] += kk;
        printRegisters();
        printPCReg();
    }

    private void i8xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " LD V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);
        registers[x] = registers[y];
    }

    private void i8xy1() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " OR V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        registers[x] |= registers[y];
        printRegisters();
        printPCReg();
    }

    private void i8xy2() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " AND V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        registers[x] &= registers[y];
        printRegisters();
        printPCReg();
    }

    private void i8xy3() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " XOR V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        registers[x] ^= registers[y];
        printRegisters();
        printPCReg();
    }

    private void i8xy4() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " ADD V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        int res = registers[x] + registers[y];
        registers[0xF] = res > 255 ? 1 : 0;

        registers[x] = res & 0xFF;
        printRegisters();
        printPCReg();
    }

    private void i8xy5() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SUB V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        registers[0xF] = registers[x] > registers[y] ? 1 : 0;

        registers[x] -= registers[y];
        printRegisters();
        printPCReg();
    }

    private void i8xy6() {
        int x = (currentOp >> 8) & 0x0F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SHR V" + Integer.toHexString(x).toUpperCase() +
                        ", " + Integer.toHexString(1).toUpperCase(),
                ELogLevel.DEBUG);

        registers[0xF] = registers[x] % 2 == 1 ? 1 : 0;

        registers[x] >>= 1;
        printRegisters();
        printPCReg();
    }

    private void i8xy7() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SUBN V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);

        registers[0xF] = registers[y] > registers[x] ? 1 : 0;
        int res = registers[y] - registers[x];
        registers[x] = res;
        printRegisters();
        printPCReg();
    }

    private void i8xyE() {
        int x = (currentOp >> 8) & 0x0F;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SHL V" + Integer.toHexString(x).toUpperCase() +
                        ", " + Integer.toHexString(1).toUpperCase(),
                ELogLevel.DEBUG);

        registers[0xF] = registers[x] % 2 == 1 ? 1 : 0;

        registers[x] <<= 1;
        printRegisters();
        printPCReg();
    }


    private void i9xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " SNE V" + Integer.toHexString(x).toUpperCase() +
                        ", V" + Integer.toHexString(y).toUpperCase(),
                ELogLevel.DEBUG);
        if (Vx != Vy)
            PCregister += 2;
        printRegisters();
        printPCReg();
    }

    private void iAnnn() {
        int addr = currentOp & 0xFFF;
        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() +
                        " LD VI" +
                        ", " + Integer.toHexString(addr).toUpperCase(),
                ELogLevel.DEBUG);
        Iregister = addr;
        printIReg();
        printPCReg();
    }

    private void iDxyn() {

        int iVx = (currentOp & 0x0F00) >> 8;
        int iVy = (currentOp & 0x00F0) >> 4;
        int x = registers[iVx] % 64;
        int y = registers[iVy] % 32;
        int n = currentOp & 0x000F;

        logger.log("OP " + Integer.toHexString(currentOp).toUpperCase() + " DRAW 8x" + n +
                        " at (" + x + ", " + y + ")",
                ELogLevel.DEBUG);

        registers[0xF] = 0;
        int[][] screen = machine.getScreen();

        for (int i = 0; i < n; i++) {
            int pixel = machine.getRam()[Iregister + i];
            logger.log("Px: " + Integer.toHexString(pixel).toUpperCase(), ELogLevel.DEBUG);
            for (int j = 0; j < 8; j++) {
                if ((pixel & (0x80 >> j)) != 0) {
                    if (screen[y + i][x + j] == 1)
                        registers[0xF] = 1;

                    screen[y + i][x + j] ^= 0x1;
                }
            }
        }
        printIReg();
        printPCReg();
    }

    private void iFx07() {
        int x = (currentOp >> 8) & 0x0F;
        registers[x] = machine.getDelay_timer();
    }

    @SneakyThrows
    private void iFx0A() {
        int x = (currentOp >> 8) & 0x0F;
        int i = System.in.read();
        if (i >= 0)
            registers[x] = i;
        else
            logger.log("Invalid input: " + (char) i, ELogLevel.ERROR);
    }
    private void iFx15() {
        int x = (currentOp >> 8) & 0x0F;
        machine.setDelay_timer(registers[x]);
    }
    private void iFx18() {
        int x = (currentOp >> 8) & 0x0F;
        machine.setSound_timer(registers[x]);
    }

    private void iFx1E() {
        int x = (currentOp >> 8) & 0x0F;
        Iregister += registers[x];
    }
    private void iFx29() {
        int x = (currentOp >> 8) & 0x0F;
        Iregister = registers[(x * 8) % Chip8Specs.FONTSET_SIZE];
    }

    private void iFx33() {
        int x = (currentOp >> 8) & 0x0F;

        machine.getRam()[Iregister] = (registers[x] >> 8) & 0x0F;
        machine.getRam()[Iregister + 1] = (registers[x] >> 4) & 0x0F;
        machine.getRam()[Iregister + 2] = registers[x] & 0x0F;
    }

    private void iFx55() {
        int x = (currentOp >> 8) & 0x0F;

        System.arraycopy(registers, 0, machine.getRam(), Iregister, x);
    }

    private void iFx65() {
        int x = (currentOp >> 8) & 0x0F;
        System.arraycopy(machine.getRam(), Iregister, registers, 0, x);
    }

    private void decodei0() {
        if (currentOp < 0x00FF) {
            switch (currentOp & 0xFF) {
                case 0xE0:
                    i00E0();
                    break;
                case 0xEE:
                    i00EE();
                    break;
                default:
                    logger.log("Instruction 0 with code " + (currentOp & 0xFF), ELogLevel.ERROR);
            }

        } else {
            logger.log("Instruction " + (currentOp) + "ignored. (SYS)", ELogLevel.WARN);
        }

    }

    private void decodei8() {
        switch (currentOp & 0x0F) {
            case 0x1:
                i8xy1();
            case 0x2:
                i8xy2();
            case 0x3:
                i8xy3();
            case 0x4:
                i8xy4();
            case 0x5:
                i8xy5();
            case 0x6:
                i8xy6();
            case 0x7:
                i8xy7();
            case 0xE:
                i8xyE();
        }
    }

    private void decodeiF() {
        switch (currentOp & 0xFF) {
            case 0x07:
                iFx07();
            case 0x0A:
                iFx0A();
            case 0x15:
                iFx15();
            case 0x18:
                iFx18();
            case 0x1E:
                iFx1E();
            case 0x29:
                iFx29();
            case 0x33:
                iFx33();
            case 0x55:
                iFx55();
            case 0x65:
                iFx65();
        }
    }
}
