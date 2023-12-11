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

    /**
     * Constructor for the Motorola 6809 Emulator.
     */
    public Motorola6809Emulator()
    {

        indexRegisterX = 0;
        indexRegisterY = 0;
        programCounter = 0xFC00; // Start at FC00
        stackPointer = 0xFFFF;   // Typically starts at the top of memory
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
        openAsmEditorButton = new JButton("Open Asm Editor");
        frame.add(openAsmEditorButton);

        indexRegisterXField = new JTextField(10);
        indexRegisterXField.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        // Parse the input value as an integer
        try {
            int value = Integer.parseInt(indexRegisterXField.getText());
            // Update the internal register value
            indexRegisterX = value;
            // Optionally update the GUI or memory if needed
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
                // Parse the input value as an integer
                try {
                    int value = Integer.parseInt(indexRegisterYField.getText());
                    // Update the internal register value
                    indexRegisterY = value;
                    // Optionally update the GUI or memory if needed
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
                    // Update the internal stack pointer value
                    stackPointer = value;
                    // Optionally update the GUI or memory if needed
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

        frame.setSize(400, 400);
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
        asmEditorDialog = new JDialog(frame, "Assembly Code Editor", true);
        asmEditorDialog.setLayout(new FlowLayout());

        asmEditorArea = new JTextArea(20, 30);
        asmEditorDialog.add(new JScrollPane(asmEditorArea));

        asmMemoryView = new JTextArea(10, 30); // Initialize asmMemoryView
        asmEditorDialog.add(new JLabel("Memory View:"));
        asmEditorDialog.add(new JScrollPane(asmMemoryView));

        romMemoryView = new JTextArea(10, 30); // Initialize romMemoryView
        asmEditorDialog.add(new JLabel("ROM View (Starting from FC00):"));
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

        asmEditorDialog.setSize(400, 600); // Adjusted the height to accommodate the new memory views
        asmEditorDialog.setVisible(true);
    }


    private void updateMemoryViews() {
        // Update the memory views for both the main emulator and the assembly editor
        updateMemoryView();
        updateAsmMemoryView();
        updateRomMemoryView();
    }

    private void updateRegisterFields() {
        indexRegisterXField.setText(Integer.toString(indexRegisterX));
        indexRegisterYField.setText(Integer.toString(indexRegisterY));
        stackPointerField.setText(Integer.toHexString(stackPointer).toUpperCase());
        conditionCodeRegisterField.setText(Integer.toBinaryString(conditionCodeRegister));
    }

    private void updateRomMemoryView() {
        StringBuilder builder = new StringBuilder();
        // Assuming FC00 is the start address for ROM view
        for (int i = 0xFC00; i < memory.length && i < 0xFFFF; i++) {
            builder.append("Addr ").append(Integer.toHexString(i).toUpperCase()).append(": ").append(memory[i]).append("\n");
        }
        romMemoryView.setText(builder.toString());
        romMemoryView.setCaretPosition(0); // Scroll to the top
    }

    private void updateAsmMemoryView() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            builder.append("Addr ").append(i).append(": ").append(memory[i]).append("\n");
        }
        asmMemoryView.setText(builder.toString());
    }

    /* salma/mouad/adam */
    private void runAssemblyCode(String asmCode) {
        String[] lines = asmCode.split("\\n"); // Split the input text into lines
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+"); // Split each line into instruction and operand(s)
            String instruction = parts[0];
            line = line.trim();

            // Ignore empty lines and comments
            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            // Process the instruction
            switch (instruction.toUpperCase()) {
                case "LDA":
                    if (parts.length >= 2) {
                        handleImmediateAddressing(parts[1], accumulatorAField, 0x200);
                        printToOutput("Executed instruction: " + line);
                    }
                    break;
                case "LDB":
                    if (parts.length >= 2) {
                        handleImmediateAddressing(parts[1], accumulatorBField, 0x201);
                        printToOutput("Executed instruction: " + line);
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
                case "ADD":
                    performBinaryOperation(accumulatorAField, accumulatorBField, (a, b) -> a + b);
                    printToOutput("Executed instruction: " + line);
                    break;
                case "SUB":
                    performBinaryOperation(accumulatorAField, accumulatorBField, (a, b) -> a - b);
                    printToOutput("Executed instruction: " + line);
                    break;
                case "MUL":
                    performBinaryOperation(accumulatorAField, accumulatorBField, (a, b) -> a * b);
                    printToOutput("Executed instruction: " + line);
                    break;
                case "STA":
                    if (parts.length >= 2) {
                        handleSTA(parts[1], accumulatorAField);
                        printToOutput("Executed instruction: " + line);
                    }
                    break;
                // Add any additional instruction cases here.
                default:
                    printToOutput("Unknown instruction: " + instruction);
                    break;
            }
        }

        updateMemoryViews();
        updateRegisterFields();
    }



    private void handleImmediateAddressing(String operand, JTextField accumulatorField, int memoryAddress) {
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

        // Store immediate value in memory at the specified address
        memory[memoryAddress] = immediateValue;
    }


    private void performBinaryOperation(JTextField operand1, JTextField operand2, BinaryOperation operation) {
        int a = Integer.parseInt(operand1.getText());
        int b = Integer.parseInt(operand2.getText());
        int result = operation.apply(a, b);
        operand1.setText(Integer.toString(result));
    }

    // Handle division logic
    private void handleDivision(String operand) {
        int divisor = Integer.parseInt(operand.substring(2), 16);
        int dividend = Integer.parseInt(accumulatorAField.getText());
        if (divisor == 0) {
            JOptionPane.showMessageDialog(frame, "Division by zero error!");
            return;
        }
        int quotient = dividend / divisor;
        accumulatorAField.setText(Integer.toString(quotient));
    }

    private void handleSTA(String operand, JTextField accumulatorField) {
        // Assuming STA stores the value from accumulator to memory
        // Parse the operand as a memory address and update the memory array
        int address = Integer.parseInt(operand.substring(2), 16);
        memory[address] = accumulatorField.getText();
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
            builder.append("Addr ").append(i).append(": ").append(memory[i]).append("\n");
        }
        memoryView.setText(builder.toString());
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
