package com.pip.sanguo.performancetest.net;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class UWAPUncompress{
    private static final int BITS = 12;
    private static final int MAX_VALUE = (1 << BITS) - 1;
    private static final int MAX_CODE = MAX_VALUE - 1;
    private static final int TABLE_SIZE = 5021;

    private InputStream input = null;
    private OutputStream output = null;

    private int input_bit_count = 0;
    private int input_bit_buffer = 0;

    private short[] decode_buffer = new short[TABLE_SIZE];
    private short[] prefix_code = new short[TABLE_SIZE];
    private short[] append_character = new short[TABLE_SIZE];
    
    public UWAPUncompress(InputStream is, OutputStream os){
        this.input = is;
        this.output = os;
    }

    public void unCompress(){
        short next_code;
        short new_code;
        short old_code;
        int character;

        int string_buffer_index = 0;
        int decode_buffer_index = 0;

        next_code = 256; /* This is the next available code to define */

        old_code = input_code(); /* Read in the first code, initialize the */
        character = old_code; /* character variable, and send the first */

        writeCode(old_code); /* code to the output file                */

        /*
         **  This is the main expansion loop.  It reads in characters from the LZW file
         **  until it sees the special code used to inidicate the end of the data.
         */
        while((new_code = input_code()) != (MAX_VALUE)){
            /*
             ** This code checks for the special STRING+CHARACTER+STRING+CHARACTER+STRING
             ** case which generates an undefined code.  It handles it by decoding
             ** the last code, and adding a single character to the end of the decode string.
             */
            if(new_code >= next_code){
                decode_buffer[decode_buffer_index] = (short)character;
                string_buffer_index = decode_string(decode_buffer_index + 1, old_code);
            }else{
                /*
                 ** Otherwise we do a straight decode of the new code.
                 */
                string_buffer_index = decode_string(decode_buffer_index, new_code);
            }

            /*
             ** Now we output the decoded string in reverse order.
             */
            character = decode_buffer[string_buffer_index];

            while(string_buffer_index >= decode_buffer_index){
                writeCode(decode_buffer[string_buffer_index--]);
            }

            /*
             ** Finally, if possible, add a new code to the string table.
             */
            if(next_code <= MAX_CODE){
                prefix_code[next_code] = old_code;
                append_character[next_code] = (short)character;
                next_code++;
            }

            old_code = new_code;
        }
    }

    private int decode_string(int index, short code){
        int i = 0;

        while(code > 255){
            decode_buffer[index++] = append_character[code];
            code = prefix_code[code];
            if(i++ >= MAX_CODE){
                //#debug
                System.out.println("Fatal error during code expansion.\n");
                System.exit(1);
            }
        }

        decode_buffer[index] = code;

        return index;
    }

    /*
     ** The following two routines are used to output variable length
     ** codes.  They are written strictly for clarity, and are not
     ** particularyl efficient.
     */
    public short input_code(){
        short return_value;
        int inputChar = -1;

        while(input_bit_count <= 24){
            try{
                inputChar = input.read();
            }catch(IOException ioe){
                //#debug
                ioe.printStackTrace();
            }

            input_bit_buffer |= inputChar << (24 - input_bit_count);
            input_bit_count += 8;
        }

        return_value = (short)(input_bit_buffer >>> (32 - BITS));
        input_bit_buffer <<= BITS;
        input_bit_count -= BITS;

        return return_value;
    }
    
    public void writeCode(int code){
        try{
            output.write(code);
        }catch(IOException e){
            //#debug
            e.printStackTrace();
        }
    }
}