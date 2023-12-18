import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Functional Interface representing a binary operation.
 */
@FunctionalInterface
interface BinaryOperation {
    int apply(int a, int b);
}
/**
 * Class representing the Motorola 6809 Emulator.
 */
public class Motorola6809Emulator {

    private JFrame frame;
    private JComboBox<String> instructionBox;
    private JButton executeButton;
    private JButton stepButton;
    private JTextField accumulatorAField;
    private JTextField accumulatorBField;
    private JTextField breakpointField;
    private JTextArea memoryView;
    private String[] memory;

    private JTextField inputField;
    private JButton interruptButton;
    private JTextArea outputArea;
    private JButton printButton;
    private int instructionPointer = 0;
    private JDialog asmEditorDialog;
    private JTextArea asmEditorArea;
    private JButton runAsmCodeButton;
    private JButton openAsmEditorButton;
    private JTextArea asmMemoryView;
    private JTextArea romMemoryView;
    private JTextField indexRegisterXField;
    private JTextField indexRegisterYField;
    private JTextField stackPointerField;
    private JTextField conditionCodeRegisterField;

    private int indexRegisterX;
    private int indexRegisterY;
    private int programCounter;
    private int stackPointer;
    private int conditionCodeRegister;
    private int currentMemoryAddress = 0;

    /**
     * Constructor for the Motorola 6809 Emulator.
     */
    public Motorola6809Emulator()
    {

        indexRegisterX = 0;
        indexRegisterY = 0;
        programCounter = 0xFC00; // Start at FC00
        conditionCodeRegister = 0;

        memory = new String[65536]; // Use 65536 if you need the full addressable range
        Arrays.fill(memory, "00");

        frame = new JFrame("Motorola 6809 Emulator");
        frame.setLayout(new FlowLayout());

        /* GUI components for CPU instructions and registers */
        instructionBox = new JComboBox<>(new String[]{
            "LDA", "LDB", "ADD", "SUB", "MUL", "LDX", "LDY", "PSH", "PUL"
        });
        executeButton = new JButton("Execute");
        accumulatorAField = new JTextField(10);
        accumulatorBField = new JTextField(10);
        breakpointField = new JTextField(10);
        stepButton = new JButton("Step");
        memoryView = new JTextArea(10, 30);
        memory = new String[1000]; // Assume our emulated memory has 1000 slots
        stackPointer = memory.length - 1;
        openAsmEditorButton = new JButton("Open Asm Editor");
        frame.add(openAsmEditorButton);

        indexRegisterXField = new JTextField(10);
        indexRegisterXField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Parse the input as hexadecimal and update the index register
                    indexRegisterX = Integer.parseInt(indexRegisterXField.getText(), 16);
                    // Immediately update the text field to reflect the hexadecimal value
                    indexRegisterXField.setText(String.format("%04X", indexRegisterX));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input for Index Register X");
                }
            }
        });
        frame.add(indexRegisterXField);
        indexRegisterYField = new JTextField(10);
        indexRegisterYField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Parse the input as hexadecimal and update the index register
                    indexRegisterY = Integer.parseInt(indexRegisterYField.getText(), 16);
                    // Immediately update the text field to reflect the hexadecimal value
                    indexRegisterYField.setText(String.format("%04X", indexRegisterY));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input for Index Register Y");
                }
            }
        });
        frame.add(indexRegisterYField);
        stackPointerField = new JTextField(10);
        stackPointerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Parse the input value as a hexadecimal integer
                try {
                    int value = Integer.parseInt(stackPointerField.getText(), 16);
                    // Check if the value is within the bounds of the memory array
                    if (value >= 0 && value < memory.length) {
                        stackPointer = value;
                        // Optionally update the GUI or memory if needed
                    } else {
                        JOptionPane.showMessageDialog(frame, "Stack Pointer value is out of bounds!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input for Stack Pointer");
                }
            }
        });

        frame.add(stackPointerField);
        conditionCodeRegisterField = new JTextField(10);

        /* Add components to frame */
        frame.add(new JLabel("Instruction:"));
        frame.add(instructionBox);
        frame.add(executeButton);
        frame.add(new JLabel("Accumulator A:"));
        frame.add(accumulatorAField);
        frame.add(new JLabel("Accumulator B:"));
        frame.add(accumulatorBField);
        // frame.add(new JLabel("Breakpoint Address:"));
        // frame.add(breakpointField);
        frame.add(stepButton);
        frame.add(new JLabel("Index Register X:"));
        frame.add(indexRegisterXField);
        frame.add(new JLabel("Index Register Y:"));
        frame.add(indexRegisterYField);
        frame.add(new JLabel("Stack Pointer:"));
        frame.add(stackPointerField);
        frame.add(new JLabel("Condition Code Register:"));
        frame.add(conditionCodeRegisterField);
        frame.add(new JLabel("Memory View:"));
        frame.add(new JScrollPane(memoryView));

        /* Input simulation field and interrupt button */
        // JLabel inputLabel = new JLabel("Input:");
        // inputField = new JTextField(10);

        // frame.add(inputLabel);
        // frame.add(inputField);
        // frame.add(interruptButton);

        /* Console-like output area for displaying outputs and debug messages */
        outputArea = new JTextArea(5, 30);
        outputArea.setEditable(false); // Read-only
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        frame.add(new JLabel("Console Output:"));
        frame.add(outputScrollPane);

        /* Print button to print the contents of outputArea */
        printButton = new JButton("Print Results");
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    outputArea.print();
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(frame, "Error printing results: " + ex.getMessage());
                }
            }
        });

        // frame.add(printButton);

        /* Button action for execution */
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeInstruction();
            }
        });

        /* Button action for stepping*/
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stepping will execute the next instruction
                executeInstruction();
                // Update memory view
                updateMemoryView();
            }
        });

        /* Initialize memory with placeholder values */
        for (int i = 0; i < memory.length; i++) {
            memory[i] = "00"; // Placeholder value
        }

        updateMemoryView();

        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        openAsmEditorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAsmEditor();
            }
        });
    }


    /* salma/mouad/adam */
    private void openAsmEditor() {
        // Create the dialog and text areas for assembly code input, memory view, and ROM view
        asmEditorDialog = new JDialog(frame, "Assembly Code Editor", false);

        asmEditorDialog.setLayout(new FlowLayout());

        asmEditorArea = new JTextArea(20, 30);
        asmEditorDialog.add(new JScrollPane(asmEditorArea));

        asmMemoryView = new JTextArea(10, 30); // Initialize asmMemoryView
        asmEditorDialog.add(new JLabel("Memory View:"));
        asmEditorDialog.add(new JScrollPane(asmMemoryView));

        romMemoryView = new JTextArea(10, 30); // Initialize romMemoryView
        asmEditorDialog.add(new JLabel("ROM View:"));
        asmEditorDialog.add(new JScrollPane(romMemoryView));

        JButton runAsmCodeButton = new JButton("Run Asm Code");
        asmEditorDialog.add(runAsmCodeButton);

        // Logic for running assembly code can be implemented here
        runAsmCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For example, you can parse the input in asmEditorArea and execute it
                runAssemblyCode(asmEditorArea.getText());
                updateMemoryViews();
            }
        });

        asmEditorDialog.setSize(400, 800); // Adjusted the height to accommodate the new memory views
        asmEditorDialog.setVisible(true);
    }


    private void updateMemoryViews() {
        // Update the memory views for both the main emulator and the assembly editor
        updateMemoryView();
        updateAsmMemoryView();
        updateRomMemoryView();
    }


    private void updateRegisterFields() {
        // Use String.format to convert to hexadecimal format with leading zeros
        indexRegisterXField.setText(String.format("%04X", indexRegisterX));
        indexRegisterYField.setText(String.format("%04X", indexRegisterY));
        stackPointerField.setText(String.format("%04X", stackPointer).toUpperCase());
        conditionCodeRegisterField.setText(Integer.toBinaryString(conditionCodeRegister));
    }
    /* adam/mouad/salma */
    private void updateRomMemoryView() {
        StringBuilder builder = new StringBuilder();
        // Define the actual ROM size you want to display
        int romSize = 256; // The size of the ROM you wish to display
        int romStartAddress = 0xFC00; // The starting address of the ROM in the emulator
        int actualRomStartSlot = memory.length - romSize; // This is where ROM starts in your 1000-slot array

        for (int i = 0; i < romSize; i++) {
            // Calculate the actual index in the 1000-slot array where the ROM data is stored
            int actualIndex = actualRomStartSlot + i;
            // Check if the actual index is within the bounds of the memory array
            if (actualIndex >= 0 && actualIndex < memory.length) {
                // Format the address as a 4-digit hexadecimal number with leading zeros
                String address = String.format("%04X", romStartAddress + i);
                builder.append("Addr ").append(address).append(": ").append(memory[actualIndex]).append("\n");
            }
        }

        romMemoryView.setText(builder.toString());
        romMemoryView.setCaretPosition(0); // Scroll to the top
    }


    /* salma/mouad/adam */
    private void updateAsmMemoryView() {
        StringBuilder builder = new StringBuilder();
        int startAddress = 0; // Example start address
        int endAddress = 1024; // Example end address, adjust as needed

        for (int i = startAddress; i < endAddress && i < memory.length; i++) {
            builder.append("Addr ").append(String.format("%04X", i)).append(": ").append(memory[i]).append("\n");
        }
        asmMemoryView.setText(builder.toString());
        asmMemoryView.setCaretPosition(0); // Scroll to the top
    }


    /* salma/mouad/adam */
    private void runAssemblyCode(String asmCode) {
        String[] lines = asmCode.split("\\n"); // Split the input text into lines
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+"); // Split each line into instruction and operand(s)
            String instruction = parts[0].toUpperCase();
            String operand = parts.length > 1 ? parts[1] : "";

            // Ignore empty lines and comments
            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            // Process the instruction
            switch (instruction.toUpperCase()) {
                case "LDA":
                    if (parts.length >= 2) {
                        handleImmediateAddressing(parts[1], accumulatorAField);
                        int romSize = 256; // Example ROM size
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts

                        // Convert program counter to memory array index
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;
                        String ldaOpcode = "86";
                        // Store opcode at the current program counter location
                        if (memoryIndex >= romStartIndex && memoryIndex < memory.length) {
                            memory[memoryIndex] = ldaOpcode;

                            // Check if the operand is immediate addressing with a constant value
                            if (parts[1].startsWith("#$")) {
                                // Extract the immediate value and store it in the next address
                                String immediateValue = parts[1].substring(2); // Remove the '#$'
                                memory[memoryIndex + 1] = immediateValue;

                                // Increment program counter by 2, for the opcode and operand
                                programCounter += 2;
                            }

                            // Update the ROM view after modifying memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range.");
                        }

                        printToOutput(String.format("Executed instruction: LDA #%s, PC at: %04X", line, programCounter));

                    }
                    break;
                case "LDB":
                    if (parts.length >= 2) {
                        handleImmediateAddressing(parts[1], accumulatorBField);
                        String ldbOpcode = "C6";  // This is a placeholder opcode for LDB

                        // Convert program counter to memory array index
                        int romSize = 256; // Example ROM size
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;

                        if (memoryIndex >= romStartIndex && memoryIndex < memory.length) {
                            memory[memoryIndex] = ldbOpcode; // Store the LDB opcode at the current program counter location

                            if (parts[1].startsWith("#$")) {
                                // Extract the immediate value and store it in the next address
                                String immediateValue = parts[1].substring(2); // Remove the '#$'
                                memory[memoryIndex + 1] = immediateValue; // Store the immediate value

                                // Increment program counter by 2, for the opcode and operand
                                programCounter += 2;
                            }

                            // Update the ROM view after modifying memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range.");
                        }
                        printToOutput(String.format("Executed instruction: LDB #%s, PC at: %04X", line, programCounter));

                    }
                    break;
                case "LDX":
                    if (parts.length >= 2) {
                        String valueStr = parts[1].replaceAll("#\\$", ""); // Remove the immediate value indicator
                        indexRegisterX = Integer.parseInt(valueStr, 16); // Parse the immediate value as hexadecimal
                        indexRegisterXField.setText(String.format("%04X", indexRegisterX)); // Update the text field to show the register's new value

                        int romSize = 256;
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex; // Translate to the array index

                        if (memoryIndex >= romStartIndex && memoryIndex < memory.length - 1) {
                            memory[memoryIndex] = "8E"; // Opcode for LDX immediate
                            memory[memoryIndex + 1] = String.format("%02X", (indexRegisterX >> 8) & 0xFF); // High byte of operand
                            memory[memoryIndex + 2] = String.format("%02X", indexRegisterX & 0xFF); // Low byte of operand

                            // Update the program counter
                            programCounter += 3; // Opcode + 2 bytes of operand

                            // Update the ROM view to reflect the new state of memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for LDX.");
                        }
                        printToOutput("Executed instruction: LDX with value #$" + valueStr + "; PC = $" + String.format("%04X", programCounter));
                    }
                    break;

                case "LDY":
                    if (parts.length >= 2) {
                        String valueStr = parts[1].replaceAll("#\\$", ""); // Remove the immediate value indicator
                        indexRegisterY = Integer.parseInt(valueStr, 16); // Parse the immediate value as hexadecimal
                        indexRegisterYField.setText(String.format("%04X", indexRegisterY)); // Update the text field to show the register's new value

                        int romSize = 256;
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex; // Translate to the array index

                        if (memoryIndex >= romStartIndex && memoryIndex < memory.length - 3) {
                            memory[memoryIndex] = "10"; // First byte of LDY opcode
                            memory[memoryIndex + 1] = "CE"; // Second byte of LDY opcode
                            memory[memoryIndex + 2] = String.format("%02X", (indexRegisterY >> 8) & 0xFF); // High byte of operand
                            memory[memoryIndex + 3] = String.format("%02X", indexRegisterY & 0xFF); // Low byte of operand

                            // Update the program counter
                            programCounter += 4; // 2 bytes for opcode + 2 bytes for operand

                            // Update the ROM view to reflect the new state of memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for LDY.");
                        }
                        printToOutput("Executed instruction: LDY with value #$" + valueStr + "; PC = $" + String.format("%04X", programCounter));
                    }
                    break;
                case "PSHS":
                    if (stackPointer >= 0 && stackPointer < memory.length) {
                        String accumulatorValue = accumulatorAField.getText();
                        String pshsOpcode = "34";

                        int memoryIndex = programCounter - 0xFC00 + (memory.length - 256);

                        if (memoryIndex >= 0 && memoryIndex < memory.length - 1) {
                            memory[memoryIndex] = pshsOpcode; // Store opcode
                            memory[memoryIndex + 1] = accumulatorValue;
                            memory[stackPointer] = accumulatorValue; // Push accumulator value onto stack
                            stackPointer--; // Decrement stack pointer to simulate stack push

                            programCounter += 2;

                            // Update the ROM view to reflect the new state of memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Stack pointer or program counter out of range.");
                        }

                        // Print the output message with the updated program counter
                        printToOutput(String.format("Executed instruction: PSHS A, SP at: %04X, PC at: %04X", stackPointer, programCounter));
                    } else {
                        JOptionPane.showMessageDialog(frame, "Stack overflow error!"); // The stack is full, cannot push
                    }
                    break;
                case "PULS":
                    // Check if the stack pointer is within the bounds of the memory array
                    if (stackPointer >= 0 && stackPointer < memory.length - 1) {
                        String pulsOpcode = "35";  // Placeholder opcode for PULS
                        String accumulatorValue = accumulatorAField.getText();

                        // Store the PULS opcode at the current program counter location
                        int memoryIndex = programCounter - 0xFC00 + (memory.length - 256); // Adjust as per your ROM setup

                        if (memoryIndex >= 0 && memoryIndex < memory.length) {
                            memory[memoryIndex] = pulsOpcode; // Store opcode
                            memory[memoryIndex + 1 ] = accumulatorValue;

                            // Increment the program counter by 1 for the opcode (assuming PULS only takes 1 byte)
                            programCounter += 2;

                            // Update the ROM view to reflect the new state of memory
                            updateRomMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range.");
                        }

                        // Execute the PULS instruction
                        stackPointer++; // Increment stack pointer to point to the next item to pull
                        String valuePulled = memory[stackPointer]; // Retrieve the value from the stack
                        accumulatorAField.setText(valuePulled); // Update Accumulator A with the retrieved value
                        printToOutput(String.format("Pulled %s from the stack at address: %04X, PC at: %04X", valuePulled, stackPointer, programCounter));
                        memory[stackPointer] = "00"; // Optionally clear the value in memory if your stack behavior requires it

                        // Update the memory view after the operation
                        updateMemoryView();
                        // Update register fields to show the new stack pointer value
                        updateRegisterFields();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Stack underflow error!"); // The stack is empty, cannot pull
                    }
                    break;

                case "ADDA":
                    try {
                        String addaOpcode = "8B"; // Placeholder opcode for ADDA immediate (assuming immediate addressing)
                        String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                        String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                        int a = Integer.parseInt(inputA, 16);
                        int b = Integer.parseInt(inputB, 16);
                        int sum = a + b;
                        sum &= 0xFF; // Ensure the result is within one byte
                        accumulatorAField.setText(String.format("%02X", sum).toUpperCase());

                        // Convert program counter to memory array index
                        int romSize = 256; // Example ROM size
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;

                        if (memoryIndex >= 0 && memoryIndex < memory.length - 1) {
                            memory[memoryIndex] = addaOpcode; // Store the opcode for ADDA
                            memory[memoryIndex + 1] = String.format("%02X", b); // Store the operand (immediate value)

                            // Increment program counter by 2 for the opcode and operand
                            programCounter += 2;

                            // Update the ROM view and the general memory view to reflect the new state of memory
                            updateRomMemoryView();
                            updateMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for ADDA.");
                        }

                        printToOutput(String.format("Executed instruction: ADDA #%s, Result in A: %s, PC at: %04X", inputB, String.format("%02X", sum), programCounter));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input for ADDA.");
                    }
                    break;

                case "SUBA":
                    try {
                        String subaOpcode = "80"; // Placeholder opcode for SUBA immediate (assuming immediate addressing)
                        String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                        String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                        int a = Integer.parseInt(inputA, 16);
                        int b = Integer.parseInt(inputB, 16);
                        int difference = a - b;
                        difference &= 0xFF; // Ensure the result is within one byte
                        accumulatorAField.setText(String.format("%02X", difference).toUpperCase());

                        // Convert program counter to memory array index
                        int romSize = 256; // Example ROM size
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;

                        if (memoryIndex >= 0 && memoryIndex < memory.length - 1) {
                            memory[memoryIndex] = subaOpcode; // Store the opcode for SUBA
                            memory[memoryIndex + 1] = String.format("%02X", b); // Store the operand (immediate value)

                            // Increment program counter by 2 for the opcode and operand
                            programCounter += 2;

                            // Update the ROM view and the general memory view to reflect the new state of memory
                            updateRomMemoryView();
                            updateMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for SUBA.");
                        }

                        printToOutput(String.format("Executed instruction: SUBA #%s, Result in A: %s, PC at: %04X", inputB, String.format("%02X", difference), programCounter));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input for SUBA.");
                    }
                    break;

                case "MUL":
                    try {
                        String mulaOpcode = "3D"; // Replace XX with the actual opcode for MULA immediate
                        String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                        String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                        int a = Integer.parseInt(inputA, 16);
                        int b = Integer.parseInt(inputB, 16);
                        int product = a * b;
                        product &= 0xFF; // Ensure the result is within one byte
                        accumulatorAField.setText(String.format("%02X", product).toUpperCase());

                        // Convert program counter to memory array index
                        int romSize = 256; // Example ROM size
                        int romStartIndex = memory.length - romSize; // Memory array index where ROM starts
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;

                        if (memoryIndex >= 0 && memoryIndex < memory.length - 1) {
                            memory[memoryIndex] = mulaOpcode; // Store the opcode for MULA

                            // Increment program counter by 2 for the opcode and operand
                            programCounter += 1;

                            // Update the ROM view and the general memory view to reflect the new state of memory
                            updateRomMemoryView();
                            updateMemoryView();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for MUL.");
                        }

                        printToOutput(String.format("Executed instruction: MUL #%s, Result in A: %s, PC at: %04X", inputB, String.format("%02X", product), programCounter));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input for MUL.");
                    }
                    break;

                case "STA":
                    if (parts.length >= 2) {
                        String operand1 = parts[1]; // Assume operand is in the format $XXXX or $XX for direct/extended addressing
                        handleSTA(operand1, accumulatorAField); // Function to store accumulator value at memory address

                        String staOpcode = "B7"; // Replace with the actual opcode for STA
                        String addressOperand = operand1.replace("$", ""); // Remove $ for storing in memory

                        // Calculate the index in the ROM area
                        int romSize = 256; // Define the size of the ROM area
                        int romStartIndex = memory.length - romSize; // Calculate the start index of ROM in memory array
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex; // Calculate the actual index in memory array

                        // Store opcode and operand in ROM if there's enough space
                        if (memoryIndex >= 0 && memoryIndex < memory.length - 2) {
                            memory[memoryIndex] = staOpcode; // Store the opcode for STA at the program counter location
                            memory[memoryIndex + 1] = addressOperand.substring(0, 2); // High byte of address
                            memory[memoryIndex + 2] = addressOperand.substring(2); // Low byte of address

                            programCounter += 3; // Increment the program counter by 3 (opcode + 2 bytes of address)

                            updateRomMemoryView(); // Update ROM view to reflect changes
                            updateMemoryView(); // Update general memory view
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for STA.");
                        }

                        printToOutput(String.format("Executed STA at %s, PC at: %04X", operand1, programCounter));
                    }
                    break;
            case "STB":
                    if (parts.length >= 2) {
                        String operand1 = parts[1];
                        handleSTA(operand1, accumulatorBField);

                        String staOpcode = "F7";
                        String addressOperand = operand1.replace("$", "");


                        int romSize = 256;
                        int romStartIndex = memory.length - romSize;
                        int memoryIndex = programCounter - 0xFC00 + romStartIndex;

                        // Store opcode and operand in ROM if there's enough space
                        if (memoryIndex >= 0 && memoryIndex < memory.length - 2) {
                            memory[memoryIndex] = staOpcode; // Store the opcode for STA at the program counter location
                            memory[memoryIndex + 1] = addressOperand.substring(0, 2); // High byte of address
                            memory[memoryIndex + 2] = addressOperand.substring(2); // Low byte of address

                            programCounter += 3; // Increment the program counter by 3 (opcode + 2 bytes of address)

                            updateRomMemoryView(); // Update ROM view to reflect changes
                            updateMemoryView(); // Update general memory view
                        } else {
                            JOptionPane.showMessageDialog(frame, "Program counter is out of ROM range or not enough space for STB.");
                        }

                        printToOutput(String.format("Executed STB at %s, PC at: %04X", operand1, programCounter));
                    }
                    break;
                default:
                    printToOutput("Unknown instruction: " + instruction);
                    break;
            }
        }

        updateRegisterFields();
        updateAsmMemoryView();
    }

    private void updateMemoryWithIndexRegister(int registerValue, String registerName) {
        // Assuming you have a way to determine the memory address
        // where the register value should be stored, let's call it registerAddress.
        int registerAddress = getMemoryAddressForRegister(registerName);

        // Update the memory at the registerAddress with the value of the register.
        // You'll need to convert the integer value to a hex string and store it.
        memory[registerAddress] = String.format("%04X", registerValue);

        // Update the memory view if necessary.
        updateMemoryView();
    }

    private int getMemoryAddressForRegister(String registerName) {
        // This is a placeholder function. You'll need to implement the logic
        // to determine where in memory the index register values should be stored.
        // For example:
        switch (registerName) {
            case "X":
                return 0xFFFA; // Example address for X
            case "Y":
                return 0xFFFB; // Example address for Y
            default:
                return 0xFFFF; // Default case or error
        }
    }


    private int findNextFreeMemoryAddress() {
        // Logic to find the next free address, for example:
        for (int i = 0; i < memory.length; i++) {
            if (memory[i].equals("00")) {
                return i; // Return the index of the next free memory slot
            }
        }
        // If no free address is found, handle this situation (e.g., reset to start or error)
        return 0; // or some error handling
    }


    /* salma/moud/adam */
    private void handleImmediateAddressing(String operand, JTextField accumulatorField) {
        /* Check if there's an immediate value */
        String immediateValue = "";

        if (operand.startsWith("#$")) {
            // Immediate addressing
            immediateValue = operand.substring(2);
        } else if (operand.startsWith("$")) {
            // Absolute addressing
            immediateValue = operand.substring(1);
        }

        accumulatorField.setText(immediateValue);

        accumulatorField.setText(immediateValue);

        // Store immediate value in memory at the current address and increment the address
        if (currentMemoryAddress < memory.length) {
            memory[currentMemoryAddress] = immediateValue;
            currentMemoryAddress++; // Increment the current address for the next call
        } else {
            // Handle the case where memory is full
            JOptionPane.showMessageDialog(frame, "Memory is full!");
            currentMemoryAddress = 0; // Reset the memory address or handle as required
        }
    }

    private void performBinaryOperation(JTextField operand1, JTextField operand2, BinaryOperation operation) {
        int a = Integer.parseInt(operand1.getText());
        int b = Integer.parseInt(operand2.getText());
        int result = operation.apply(a, b);
        operand1.setText(Integer.toString(result));
    }


    private void handleSTA(String operand, JTextField accumulatorField) {
        // Assuming STA stores the value from accumulator to memory
        // Parse the operand as a memory address and update the memory array
        int address;
        try {
            // Check if operand is in hexadecimal format with $ prefix
            if (operand.startsWith("$")) {
                address = Integer.parseInt(operand.substring(1), 16); // Parse as hexadecimal
            } else {
                address = Integer.parseInt(operand); // Parse as decimal
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid memory address format for STA.");
            return;
        }

        // Make sure the address is within the bounds of the memory array
        if (address >= 0 && address < memory.length) {
            memory[address] = accumulatorField.getText(); // Store the accumulator's value into the memory address
        } else {
            JOptionPane.showMessageDialog(frame, "Memory address out of bounds for STA.");
        }

        // Update the memory view to reflect changes
        updateMemoryView();
    }


    private void executeInstruction() {
        String op = (String) instructionBox.getSelectedItem();

        switch (op) {
            case "LDA":
                accumulatorAField.setText(accumulatorAField.getText());
                break;
            case "LDB":
                accumulatorBField.setText(accumulatorBField.getText());
                break;
            case "ADD":
                try {
                    // Remove any leading "0x" or "0X" if present, then parse the hex string
                    String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                    String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                    int a = Integer.parseInt(inputA, 16);
                    int b = Integer.parseInt(inputB, 16);
                    int sum = a + b;
                    // Convert the result back to a hex string, ensuring it's uppercase and prefixed with "0x"
                    accumulatorAField.setText(String.format("%02X", sum).toUpperCase());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input.");
                }
            break;
            case "LDX":
                // Load index register X with a value, perhaps from the next memory location
                indexRegisterX = Integer.parseInt(memory[programCounter + 1], 16); // Assuming memory is hexadecimal
                break;
            case "LDY":
                // Load index register Y with a value
                indexRegisterY = Integer.parseInt(memory[programCounter + 2], 16); // Just an example
                break;
            case "PSH":
                // Push a value onto the stack
                memory[stackPointer--] = accumulatorAField.getText(); // Decrement stack pointer after push
                break;
            case "PUL":
                // Pull a value from the stack
                accumulatorAField.setText(memory[++stackPointer]); // Increment stack pointer before pull
                break;
            case "SUB":
                try {
                    String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                    String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                    int a = Integer.parseInt(inputA, 16);
                    int b = Integer.parseInt(inputB, 16);
                    int difference = a - b;
                    accumulatorAField.setText(String.format("%02X", difference).toUpperCase());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input for SUB.");
                }
                break;
            case "MUL":
                try {
                    String inputA = accumulatorAField.getText().replaceAll("(?i)^0x", "");
                    String inputB = accumulatorBField.getText().replaceAll("(?i)^0x", "");
                    int a = Integer.parseInt(inputA, 16);
                    int b = Integer.parseInt(inputB, 16);
                    int product = a * b;
                    accumulatorAField.setText(String.format("%02X", product).toUpperCase());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid hexadecimal input for MUL.");
                }
                break;

        }
        programCounter += 2;

        if(instructionPointer < memory.length - 1) {
            memory[instructionPointer++] = accumulatorAField.getText();
            memory[instructionPointer++] = accumulatorBField.getText();
        } else {
            JOptionPane.showMessageDialog(frame, "Memory limit reached!");
        }

        /* Update the memory view */
        updateMemoryView();
        printToOutput("Executed instruction: " + op + ", PC now at: " + Integer.toHexString(programCounter));
    }

    private void updateMemoryView() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            // Format the address as a 4-digit hexadecimal number with leading zeros
            String address = String.format("%04X", i);
            builder.append("Addr ").append(address).append(": ").append(memory[i]).append("\n");
        }
        memoryView.setText(builder.toString());
        memoryView.setCaretPosition(0);
    }

    /* Method to print messages to the outputArea */
    private void printToOutput(String message) {
        outputArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Motorola6809Emulator emulator = new Motorola6809Emulator();
        });
    }

}
