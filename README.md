# Motorola 6809 Emulator

## Overview
This project is an emulator for the Motorola 6809 microprocessor. It simulates the behavior of the 6809, allowing users to input assembly code, execute it, and view the results in the simulated registers and memory. The emulator starts at the memory address `0xFC00`.
## Features
- Assembly Code Editor: Enter and run custom 6809 assembly code.
- Register Display: View and modify the contents of the A and B accumulators, index registers X and Y, stack pointer, and condition code register.
- Memory View: Inspect and interact with a simplified version of the 6809's addressable memory.
- Step Execution: Execute instructions one at a time and observe changes in memory and registers.
- Console Output: View the output log for executed instructions and errors.

## How to Use
1. **Start the Emulator**: Launch the emulator to initialize the memory and registers.
2. **Enter Assembly Code**: Click on the "Open Asm Editor" to write or paste assembly code.
3. **Run Code**: Execute the code to see it reflected in the memory and registers.
4. **Step Through Code**: Use the "Step" button to go through the code line by line.
5. **Inspect Memory and Registers**: The current state of memory and registers will be updated after each operation.

## Instructions Supported
- Load Accumulator (LDA, LDB)
- Add (ADD)
- Subtract (SUB)
- Multiply (MUL)
- Load Index Registers (LDX, LDY)
- Push to Stack (PSH)
- Pull from Stack (PUL)
- Store Accumulator (STA)
- Additional instructions...

## Building and Running
Make sure Java is installed on your system.
Compile the Java source files:
```shell
javac Motorola6809Emulator.java
