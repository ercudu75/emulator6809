import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;

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


    public Motorola6809Emulator()
    {
        frame = new JFrame("Motorola 6809 Emulator");
        frame.setLayout(new FlowLayout());

        /* GUI components for CPU instructions and registers */
        instructionBox = new JComboBox<>(new String[]{"LDA", "LDB", "ADD", "SUB", "MUL", "DIV"});
        executeButton = new JButton("Execute");
        accumulatorAField = new JTextField(10);
        accumulatorBField = new JTextField(10);
        breakpointField = new JTextField(10);
        stepButton = new JButton("Step");
        memoryView = new JTextArea(10, 30);
        memory = new String[1000]; // Assume our emulated memory has 1000 slots
        openAsmEditorButton = new JButton("Open Asm Editor");
        frame.add(openAsmEditorButton);

        // Add components to frame
        frame.add(new JLabel("Instruction:"));
        frame.add(instructionBox);
        frame.add(executeButton);
        frame.add(new JLabel("Accumulator A:"));
        frame.add(accumulatorAField);
        frame.add(new JLabel("Accumulator B:"));
        frame.add(accumulatorBField);
        frame.add(new JLabel("Breakpoint Address:"));
        frame.add(breakpointField);
        frame.add(stepButton);
        frame.add(new JLabel("Memory View:"));
        frame.add(new JScrollPane(memoryView));

        /* Input simulation field and interrupt button */
        JLabel inputLabel = new JLabel("Input:");
        inputField = new JTextField(10);
        interruptButton = new JButton("Generate Interrupt");
        interruptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle interrupt generation here
                JOptionPane.showMessageDialog(frame, "Interrupt signal generated!");
            }
        });

        frame.add(inputLabel);
        frame.add(inputField);
        frame.add(interruptButton);

        /* Console-like output area for displaying outputs and debug messages */
        outputArea = new JTextArea(5, 30);
        outputArea.setEditable(false); // Read-only
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        frame.add(new JLabel("Console Output:"));
        frame.add(outputScrollPane);

        // Print button to print the contents of outputArea
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

        frame.add(printButton);

        // Button action for execution
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeInstruction();
            }
        });

        // Button action for stepping
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stepping will execute the next instruction
                executeInstruction();
                // Update memory view
                updateMemoryView();
            }
        });

        // Initialize memory with placeholder values
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
    private void openAsmEditor() {
        // Create the dialog and text area for assembly code input
        JDialog asmEditorDialog = new JDialog(frame, "Assembly Code Editor", true);
        asmEditorDialog.setLayout(new FlowLayout());

        JTextArea asmEditorArea = new JTextArea(20, 30);
        asmEditorDialog.add(new JScrollPane(asmEditorArea));

        JButton runAsmCodeButton = new JButton("Run Asm Code");
        asmEditorDialog.add(runAsmCodeButton);

        // Logic for running assembly code can be implemented here
        runAsmCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For example, you can parse the input in asmEditorArea and execute it
                runAssemblyCode(asmEditorArea.getText());
                updateMemoryView();
            }
        });

        asmEditorDialog.setSize(400, 400);
        asmEditorDialog.setVisible(true);
    }
    private void runAssemblyCode(String asmCode) {
        String[] lines = asmCode.split("\\n"); // Split the input text into lines
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+"); // Split each line into instruction and operand(s)
            String instruction = parts[0];
            // Process the instruction
            switch (instruction.toUpperCase()) {
                case "LDA":
                    // Assuming that LDA loads the value into accumulatorA
                    // This logic seems incorrect as per assembly language,
                    // normally you would load a value from memory into the accumulator
                    accumulatorAField.setText(accumulatorBField.getText());
                    break;
                case "LDB":
                    // Similarly for LDB, it's supposed to load into accumulatorB
                    accumulatorBField.setText(accumulatorAField.getText());
                    break;
                case "ADD":
                    int a = Integer.parseInt(accumulatorAField.getText());
                    int b = Integer.parseInt(accumulatorBField.getText());
                    int sum = a + b;
                    accumulatorAField.setText(Integer.toString(sum));
                    break;
                case "SUB":
                    a = Integer.parseInt(accumulatorAField.getText());
                    b = Integer.parseInt(accumulatorBField.getText());
                    int difference = a - b;
                    accumulatorAField.setText(Integer.toString(difference));
                    break;
                case "MUL":
                    a = Integer.parseInt(accumulatorAField.getText());
                    b = Integer.parseInt(accumulatorBField.getText());
                    int product = a * b;
                    accumulatorAField.setText(Integer.toString(product));
                    break;
                case "DIV":
                    a = Integer.parseInt(accumulatorAField.getText());
                    b = Integer.parseInt(accumulatorBField.getText());
                    if (b == 0) {
                        JOptionPane.showMessageDialog(frame, "Division by zero error!");
                        return;
                    }
                    int quotient = a / b;
                    accumulatorAField.setText(Integer.toString(quotient));
                    break;
                // Add any additional instruction cases here.
                default:
                    printToOutput("Unknown instruction: " + instruction);
                    break;
            }
        }

        updateMemoryView(); // Refresh the GUI to show the updated accumulator values
    }


    private void executeInstruction() {
        String op = (String) instructionBox.getSelectedItem();

        switch (op) {
            case "LDA":
                accumulatorAField.setText(accumulatorBField.getText());
                break;
            case "LDB":
                accumulatorBField.setText(accumulatorAField.getText());
                break;
            case "ADD":
                int a = Integer.parseInt(accumulatorAField.getText());
                int b = Integer.parseInt(accumulatorBField.getText());
                int sum = a + b;
                accumulatorAField.setText(Integer.toString(sum));
                break;
            case "SUB":
                a = Integer.parseInt(accumulatorAField.getText());
                b = Integer.parseInt(accumulatorBField.getText());
                int difference = a - b;
                accumulatorAField.setText(Integer.toString(difference));
                break;
            case "MUL":
                a = Integer.parseInt(accumulatorAField.getText());
                b = Integer.parseInt(accumulatorBField.getText());
                int product = a * b;
                accumulatorAField.setText(Integer.toString(product));
                break;
            case "DIV":
                a = Integer.parseInt(accumulatorAField.getText());
                b = Integer.parseInt(accumulatorBField.getText());
                if (b == 0) {
                    JOptionPane.showMessageDialog(frame, "Division by zero error!");
                    return;
                }
                int quotient = a / b;
                accumulatorAField.setText(Integer.toString(quotient));
                break;
        }

        if(instructionPointer < memory.length - 1) {
            memory[instructionPointer++] = accumulatorAField.getText();
            memory[instructionPointer++] = accumulatorBField.getText();
        } else {
            JOptionPane.showMessageDialog(frame, "Memory limit reached!");
        }

        /* Update the memory view */
        updateMemoryView();
        printToOutput("Executed instruction: " + instructionBox.getSelectedItem());
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
        new Motorola6809Emulator();
    }
}
