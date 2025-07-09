package abysswatcher;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class CPU {
    private static final int NUM_V_REGISTERS = 16;
    private static final int PC_START = 0x200;
    private static final Chip8 machine = Chip8.getInstance();
    private static final PrintLogger PRINT_LOGGER = PrintLogger.getInstance();
    private static final WriteLogger WRITE_LOGGER = WriteLogger.getInstance();
    public File dump;
    public int programSize;
    // registers
    @Getter
    private int[] registers;
    @Getter
    private int Iregister;
    @Getter
    private int PCregister;
    @Getter
    private int SPregister;
    // helper variables
    private int currentOp = 0;
    private int currentInstruction = 0;

    public CPU() throws FileNotFoundException {
        init();
    }

    public String printRegisters() {
        StringBuilder sb = new StringBuilder();
        sb.append("Registers: ");
        for (int i = 0; i < registers.length; i++) {
            if (i % 4 == 0)
                sb.append("\n");
            if (registers[i] == 0)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 00");
            else if (registers[i] < 16)
                sb.append(Integer.toHexString(i).toUpperCase()).append(": 0")
                        .append(Integer.toHexString(registers[i]).toUpperCase());
            else
                sb.append(Integer.toHexString(i).toUpperCase()).append(": ")
                        .append(Integer.toHexString(registers[i]).toUpperCase());
            if (i < registers.length - 1)
                sb.append("   ");
        }
        return sb.toString();
    }

    private String printIReg() {
        return "VI: " + Integer.toHexString(Iregister).toUpperCase() +
                " [" + Integer.toHexString(machine.getRam()[Iregister]).toUpperCase() + "]";

    }

    private String printPCReg() {
        return "PC: " + Integer.toHexString(PCregister).toUpperCase() +
                " [" + Integer.toHexString(machine.getRam()[PCregister]).toUpperCase() + " " +
                Integer.toHexString(machine.getRam()[PCregister + 1]).toUpperCase() + "]";

    }

    private String printSPReg() {
        return "SP: " + Integer.toHexString(SPregister).toUpperCase() +
                " [" + Integer.toHexString(machine.getRam()[SPregister]).toUpperCase() + "]";

    }

    private String printStack() {
        return "Stack: " + Arrays.toString(machine.getStack());
    }

    public void init() {
        PCregister = PC_START;
        SPregister = 0;
        Iregister = 0;
        currentOp = 0;
        currentInstruction = 0;

        registers = new int[NUM_V_REGISTERS];
        Arrays.fill(registers, 0);
        dump = new File("./dump.txt");
    }

    public void fetch() {
        currentOp = machine.getRam()[PCregister] << 8 | machine.getRam()[PCregister + 1];
        PCregister += 2;
    }

    public void decode() {
        currentInstruction = currentOp >> 12;
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
                machine.printScreen();
                break;
            //            case 0xE:
            //                break;
            case 0xF:
                decodeiF();
                break;
            default:
                PRINT_LOGGER.log("Instruction not implemented", ELogLevel.ERROR);
                throw new UnsupportedOperationException();
        }

        WRITE_LOGGER.log(machine.dumpState(false));
        PRINT_LOGGER.log(printPCReg() + '\n', ELogLevel.DEBUG);
    }

    public void deinit() {
    }


    // clear the screen //
    private void i00E0() {
        String log = "OP 00E0 CLS";
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        machine.clearScreen();
    }

    // return from subroutine to address pulled from stack //
    private void i00EE() {
        String log = "OP 00EE RET";
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        PCregister = machine.getStack()[SPregister];
        SPregister -= 1;
        PRINT_LOGGER.log(printSPReg(), ELogLevel.DEBUG);
    }

    // jump to address NNN //
    private void i1nnn() {
        int addr = currentOp & 0x0FFF;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() + " JP " +
                Integer.toHexString(addr).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        PCregister = addr;
    }

    // push return address onto stack and call subroutine at address NNN //
    private void i2nnn() {
        SPregister += 1;
        int nnn = currentOp & 0xFFF;
        int[] stack = machine.getStack();
        stack[SPregister] = PCregister;
        PCregister = nnn;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " CALL " + Integer.toHexString(nnn).toUpperCase() + "\n" + printPCReg() +
                "\n" + printStack();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        PRINT_LOGGER.log(printSPReg(), ELogLevel.DEBUG);
    }

    private void i3xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SE V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(kk).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        if (registers[x] == kk)
            PCregister += 2;

        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i4xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SNE V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(kk).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        if (registers[x] != kk)
            PCregister += 2;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i5xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SE V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
        if (Vx == Vy)
            PCregister += 2;
    }


    private void i6xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(kk).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[x] = kk;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i7xkk() {
        int x = (currentOp >> 8) & 0x0F;
        int kk = currentOp & 0xFF;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " ADD V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(kk).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[x] = ((registers[x] & 0xFF) + (kk & 0xFF)) & 0xFF;

        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        registers[x] = registers[y];
    }

    private void i8xy1() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " OR V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[x] |= registers[y];
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy2() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " AND V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[x] &= registers[y];
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy3() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " XOR V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[x] ^= registers[y];
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy4() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " ADD V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        int res = registers[x] + registers[y];
        registers[0xF] = res > 255 ? 1 : 0;

        registers[x] = res & 0xFF;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy5() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SUB V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[0xF] = registers[y] > registers[x] ? 1 : 0;
        registers[x] = (registers[x] - registers[y] + 256) & 0xFF;

        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy6() {
        int x = (currentOp >> 8) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SHR V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(1).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[0xF] = registers[x] % 2 == 1 ? 1 : 0;

        registers[x] = (registers[x] >>> 1) & 0xFF;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xy7() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SUBN V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[0xF] = registers[y] > registers[x] ? 1 : 0;
        registers[x] = (registers[y] - registers[x] + 256) & 0xFF;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void i8xyE() {
        int x = (currentOp >> 8) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SHL V" + Integer.toHexString(x).toUpperCase() +
                ", " + Integer.toHexString(1).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[0xF] = registers[x] % 2 == 1 ? 1 : 0;

        registers[x] = (registers[x] << 1) & 0xFF;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }


    private void i9xy0() {
        int x = (currentOp >> 8) & 0x0F;
        int y = (currentOp >> 4) & 0x0F;
        int Vx = registers[x];
        int Vy = registers[y];

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " SNE V" + Integer.toHexString(x).toUpperCase() +
                ", V" + Integer.toHexString(y).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        if (Vx != Vy)
            PCregister += 2;
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
    }

    private void iAnnn() {
        int addr = currentOp & 0xFFF;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD VI" +
                ", " + Integer.toHexString(addr).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        Iregister = addr;
        PRINT_LOGGER.log(printIReg(), ELogLevel.DEBUG);
    }

    private void iDxyn() {

        int iVx = (currentOp & 0x0F00) >> 8;
        int iVy = (currentOp & 0x00F0) >> 4;
        int x = registers[iVx] % 64;
        int y = registers[iVy] % 32;
        int n = currentOp & 0x000F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() + " DRAW 8x" + n +
                " at (" + x + ", " + y + ")";
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        registers[0xF] = 0;
        int[][] screen = machine.getScreen();
//        machine.clearScreen();

        for (int i = 0; i < n; i++) {
            int pixel = machine.getRam()[Iregister + i];
            PRINT_LOGGER.log("Px: " + Integer.toHexString(pixel).toUpperCase(), ELogLevel.DEBUG);
            for (int j = 0; j < 8; j++) {
                if ((pixel & (0x80 >> j)) != 0) {
                    if (screen[y + i][x + j] == 1)
                        registers[0xF] = 1;

                    screen[y + i][x + j] ^= 0x1;
                }
            }
        }
        PRINT_LOGGER.log(printIReg(), ELogLevel.DEBUG);
//        machine.printScreen();
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
            PRINT_LOGGER.log("Invalid input: " + (char) i, ELogLevel.ERROR);
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
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " ADD I, V" + Integer.toHexString(x).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        Iregister += registers[x];
    }

    private void iFx29() {
        int x = (currentOp >> 8) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD F, V" + Integer.toHexString(x).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);
        Iregister = registers[x] * 0x5;
        PRINT_LOGGER.log(printIReg(), ELogLevel.DEBUG);
    }

    private void iFx33() {
        int x = (currentOp >> 8) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD B, V" + Integer.toHexString(x).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        machine.getRam()[Iregister] = registers[x] / 100;
        machine.getRam()[Iregister + 1] = (registers[x]) / 10 % 10;
        machine.getRam()[Iregister + 2] = (registers[x] % 100) % 10;
        String res = Arrays.toString(Arrays.copyOfRange(machine.getRam(), Iregister,
                Iregister + 2));
        PRINT_LOGGER.log(res, ELogLevel.DEBUG);
        WRITE_LOGGER.log(res);
    }

    private void iFx55() {
        int x = (currentOp >> 8) & 0x0F;

        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD [I], V" + Integer.toHexString(x).toUpperCase();
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        for (int i = 0; i <= x; i++) {
            PRINT_LOGGER.log(
                    "Copying V" + i + " [" + Integer.toHexString(registers[i]).toUpperCase() + "]" +
                            " to I[" + Iregister + i + "]", ELogLevel.DEBUG);
            machine.getRam()[Iregister + i] = registers[i];
        }
        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
        WRITE_LOGGER.log(printRegisters());
    }

    private void iFx65() {
        int x = (currentOp >> 8) & 0x0F;
        String log = "OP " + Integer.toHexString(currentOp).toUpperCase() +
                " LD V" + Integer.toHexString(x).toUpperCase() +
                ", [I]";
        PRINT_LOGGER.log(log, ELogLevel.DEBUG);
        WRITE_LOGGER.log(log);

        for (int i = 0; i <= x; i++) {
            PRINT_LOGGER.log(
                    "Copying V" + i + " from " + Integer.toHexString(Iregister + i).toUpperCase() +
                            " to V" + x, ELogLevel.DEBUG);
            registers[i] = machine.getRam()[Iregister + i];
        }

        PRINT_LOGGER.log(printRegisters(), ELogLevel.DEBUG);
        WRITE_LOGGER.log(printRegisters());
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
                    PRINT_LOGGER.log(
                            "Instruction 0 with code " + (currentOp & 0xFF), ELogLevel.ERROR);
            }
        }
        else {
            PRINT_LOGGER.log("Instruction " + (currentOp) + "ignored. (SYS)", ELogLevel.WARN);
        }

    }

    @SneakyThrows
    private void decodei8() {
        switch (currentOp & 0x0F) {
            case 0x0:
                i8xy0();
                break;
            case 0x1:
                i8xy1();
                break;
            case 0x2:
                i8xy2();
                break;
            case 0x3:
                i8xy3();
                break;
            case 0x4:
                i8xy4();
                break;
            case 0x5:
                i8xy5();
                break;
            case 0x6:
                i8xy6();
                break;
            case 0x7:
                i8xy7();
                break;
            case 0xE:
                i8xyE();
                break;
        }
    }

    private void decodeiF() {
        switch (currentOp & 0xFF) {
            case 0x07:
                iFx07();
                break;
            case 0x0A:
                iFx0A();
                break;
            case 0x15:
                iFx15();
                break;
            case 0x18:
                iFx18();
                break;
            case 0x1E:
                iFx1E();
                break;
            case 0x29:
                iFx29();
                break;
            case 0x33:
                iFx33();
                break;
            case 0x55:
                iFx55();
                break;
            case 0x65:
                iFx65();
                break;
        }
    }
}
