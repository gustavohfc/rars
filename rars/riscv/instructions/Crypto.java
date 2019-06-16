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

package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

/**
 * Base class for all instructions of the cryptographic coprocessor.
 *
 * @author Gustavo Henrique Fernandes Carvalho <gustavohenriquefcarvalho@gmail.com>
 */
public abstract class Crypto extends BasicInstruction {

    private static final String warning = " *** This instruction does not work in simulation, the current implementation should be used just for assembling the machine code!";

    public static final String CryptoOpcode = "0001011";
    public static final String CryptoEmptyRegister = "00000";

    public static final String CryptoLW = "0000000";
    public static final String CryptoNEXT = "0010000";
    public static final String CryptoLAST = "0100000";
    public static final String CryptoBUSY = "0110000";
    public static final String CryptoDIGEST = "1000000";
    public static final String CryptoRESET = "1010000";

    public static final String CryptoMD5 = "000";
    public static final String CryptoSHA1 = "001";
    public static final String CryptoSHA256 = "010";
    public static final String CryptoSHA512 = "011";

    public Crypto(String usage, String operMask) {
        super(usage, warning, BasicInstructionFormat.R_FORMAT, operMask);
    }

    @Override
    public void simulate(ProgramStatement statement) throws SimulationException {
//        throw new SimulationException(statement, warning);
    }
}
