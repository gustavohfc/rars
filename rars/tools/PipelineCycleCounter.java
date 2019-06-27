/*
Copyright (c) 2019,  Gustavo Henrique Fernandes Carvalho

Developed by Gustavo Henrique Fernandes Carvalho (gustavohenriquefcarvalho@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */
package rars.tools;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

import static rars.util.Binary.intToBinaryString;

/**
 * Count the number of cycles would be need to execute the code in a 5-stage pipeline.
 *
 * @author Gustavo Henrique Fernandes Carvalho <gustavohenriquefcarvalho@gmail.com>
 */
public class PipelineCycleCounter extends AbstractToolAndApplication {
    private static String name = "Pipeline Cycle Counter (TEST)";

    private int counter = 4; // 4 is the pipeline latency
    private JTextField counterField;

    private int previous_rd_1 = 0, previous_rd_2 = 0;

    private int lastAddress = -1;

    /**
     * Simple constructor
     */
    public PipelineCycleCounter() {
        super(name, name);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Observer all registers and memory.
     */
    @Override
    protected void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    @Override
    protected JComponent buildMainDisplayArea() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        counterField = new JTextField("0", 10);
        counterField.setEditable(false);

        panel.add(new JLabel("Cycles: "));
        panel.add(counterField);

        return panel;
    }

    @Override
    protected void processRISCVUpdate(Observable resource, AccessNotice notice) {
        if (!notice.accessIsFromRISCV() || notice.getAccessType() != AccessNotice.READ) {
            return;
        }

        MemoryAccessNotice memoryAccessNotice = (MemoryAccessNotice) notice;

        // The next three statments are from Felipe Lessa's instruction counter.  Prevents double-counting.
        int a = memoryAccessNotice.getAddress();
        if (a == lastAddress)
            return;

        // Add one bubble per jump taken
        if (lastAddress != -1 && a != lastAddress + 4){
            counter++;
        }

        lastAddress = a;

        try {
            ProgramStatement stmt = Memory.getInstance().getStatementNoNotify(memoryAccessNotice.getAddress());
            if(stmt != null) {
                UpdateCycles(stmt);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }

        updateDisplay();
    }

    private void UpdateCycles(ProgramStatement stmt) {
        BasicInstruction instr = (BasicInstruction) stmt.getInstruction();
        BasicInstructionFormat format = instr.getInstructionFormat();

        int current_rd, current_r1, current_r2;

        switch (format) {
            case R_FORMAT:
                current_rd = stmt.getOperand(0);
                current_r1 = stmt.getOperand(1);
                current_r2 = stmt.getOperand(2);
                break;

            case I_FORMAT:
                if (stmt.getInstruction().getName().equals("lw")){
                    current_rd = stmt.getOperand(0);
                    current_r1 = stmt.getOperand(2);
                    current_r2 = 0;
                } else {
                    current_rd = stmt.getOperand(0);
                    current_r1 = stmt.getOperand(1);
                    current_r2 = 0;
                }
                break;

            case S_FORMAT:
                current_rd = stmt.getOperand(1);
                current_r1 = stmt.getOperand(2);
                current_r2 = stmt.getOperand(0);
                break;

            case B_FORMAT:
                current_rd = stmt.getOperand(0);
                current_r1 = stmt.getOperand(1);
                current_r2 = 0;
                break;

            case U_FORMAT:
                current_rd = stmt.getOperand(0);
                current_r1 = 0;
                current_r2 = 0;
                break;

            case J_FORMAT:
                current_rd = stmt.getOperand(0);
                current_r1 = 0;
                current_r2 = 0;
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + format);
        }

        // Check for data hazard
        if (previous_rd_1 != 0 && (current_r1 == previous_rd_1 || current_r2 == previous_rd_1)){
            counter += 3;
            previous_rd_2 = 0;
            previous_rd_1 = current_rd;
        } else if (previous_rd_2 != 0 && (current_r1 == previous_rd_2 || current_r2 == previous_rd_2)){
            counter += 2;
            previous_rd_2 = 0;
            previous_rd_1 = current_rd;
        } else {
            counter += 1;
            previous_rd_2 = previous_rd_1;
            previous_rd_1 = current_rd;
        }
    }

    @Override
    protected void updateDisplay() {
        counterField.setText(String.valueOf(counter));
    }
}
