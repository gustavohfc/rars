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
import rars.riscv.hardware.*;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

import static rars.util.Binary.intToBinaryString;

/**
 * Saves all changes to the register to a file.
 *
 * @author Gustavo Henrique Fernandes Carvalho <gustavohenriquefcarvalho@gmail.com>
 */
public class SaveChanges extends AbstractToolAndApplication {
    private static String name = "Save Changes";

    // GUI components
    private JCheckBox saveRegistersChanges;
    private JCheckBox saveMemoryChanges;

    // Output files
    private boolean outputFilesOpened = false;
    private FileWriter fileRegisterChanges;
    private FileWriter fileMemoryChanges;
    private FileWriter fileChangesWithInstructions;

    /**
     * Simple constructor
     */
    public SaveChanges() {
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
        addAsObserver(Memory.dataSegmentBaseAddress, Memory.dataSegmentLimitAddress + 1);
        for (Register r : RegisterFile.getRegisters()) {
            addAsObserver(r);
        }
    }

    @Override
    protected JComponent buildMainDisplayArea() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        saveRegistersChanges = new JCheckBox("Registers");
        saveRegistersChanges.setSelected(true);

        saveMemoryChanges = new JCheckBox("Memory");
        saveMemoryChanges.setSelected(true);

        panel.add(saveRegistersChanges);
        panel.add(saveMemoryChanges);

        return panel;
    }

    @Override
    protected void processRISCVUpdate(Observable resource, AccessNotice notice) {
        if (!notice.accessIsFromRISCV())
            return;

        if (notice.getAccessType() == AccessNotice.WRITE && resource instanceof Register && saveRegistersChanges.isSelected()) {
            // Write changes on registers
            Register register = (Register) resource;
            String change = intToBinaryString(register.getNumber(), 5) + " " + intToBinaryString(register.getValueNoNotify(), 32) + "\n";

            write(fileRegisterChanges, change);
            write(fileChangesWithInstructions, change);

        } else if (notice.getAccessType() == AccessNotice.WRITE && notice instanceof MemoryAccessNotice && saveMemoryChanges.isSelected()) {
            // Write changes on memory
            MemoryAccessNotice memAccNotice = (MemoryAccessNotice) notice;
            String change = intToBinaryString(memAccNotice.getAddress(), 32) + " " + intToBinaryString(memAccNotice.getValue(), 32) + "\n";

            write(fileMemoryChanges, change);
            write(fileChangesWithInstructions, change);

        } else if (notice.getAccessType() == AccessNotice.READ && notice instanceof MemoryAccessNotice) {
            MemoryAccessNotice memAccNotice = (MemoryAccessNotice) notice;
            int memAccAddr = memAccNotice.getAddress();

            if (memAccAddr >= Memory.textBaseAddress &&  memAccAddr < Memory.textLimitAddress) {
                ProgramStatement stmt;
                try {
                    stmt = Memory.getInstance().getStatementNoNotify(memAccAddr);
                } catch (AddressErrorException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Write the current PC

                // Write the current instruction for debug purposes
                if (stmt != null) {
                    String instruction = "\n" + stmt.getSource() + "\t\t\t" + stmt.getPrintableBasicAssemblyStatement() + "\n";

                    write(fileChangesWithInstructions, instruction);
                }
            }
        }
    }

    private void write(FileWriter file, String str){
        if (!outputFilesOpened) {
            openOutputFiles();
        }

        try {
            file.write(str);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openOutputFiles() {
        // Get source file name
        String sourceFile;
        try {
            sourceFile = Memory.getInstance().getStatementNoNotify(Memory.textBaseAddress).getSourceFile();
        } catch (AddressErrorException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Remove file extension
        String prefix = sourceFile.substring(0, sourceFile.lastIndexOf('.'));

        try {
            fileRegisterChanges = new FileWriter(prefix + "_register_changes.txt");
            fileMemoryChanges = new FileWriter(prefix + "_memory_changes.txt");
            fileChangesWithInstructions = new FileWriter(prefix + "_changes_with_instructions.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
            closeOutputFiles();
        }

        outputFilesOpened = true;
    }

    private void closeOutputFiles(){
        try {
            if(fileRegisterChanges != null)
                fileRegisterChanges.close();
            if(fileChangesWithInstructions != null)
                fileChangesWithInstructions.close();
            if(fileMemoryChanges != null)
                fileMemoryChanges.close();

            outputFilesOpened = false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    public void performSpecialClosingDuties() {
        closeOutputFiles();
    }
}
