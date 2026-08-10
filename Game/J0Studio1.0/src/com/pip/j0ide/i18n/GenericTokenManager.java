package com.pip.j0ide.i18n;
import java.io.*;

/** Token Manager. */
public class GenericTokenManager implements GenericParserConstants
{

	  /** Debug output. */
	  public  java.io.PrintStream debugStream = System.out;
	  /** Set debug output. */
	  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
	private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
	{
	   switch (pos)
	   {
	      case 0:
	         if ((active0 & 0x1ffffffffe0L) != 0L)
	         {
	            jjmatchedKind = 46;
	            return 16;
	         }
	         if ((active1 & 0x2040008L) != 0L)
	            return 1;
	         return -1;
	      case 1:
	         if ((active0 & 0x20400L) != 0L)
	            return 16;
	         if ((active0 & 0x1fffffdfbe0L) != 0L)
	         {
	            if (jjmatchedPos != 1)
	            {
	               jjmatchedKind = 46;
	               jjmatchedPos = 1;
	            }
	            return 16;
	         }
	         return -1;
	      case 2:
	         if ((active0 & 0x1002000800L) != 0L)
	            return 16;
	         if ((active0 & 0x1effdfdf3e0L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 2;
	            return 16;
	         }
	         return -1;
	      case 3:
	         if ((active0 & 0x4004280280L) != 0L)
	            return 16;
	         if ((active0 & 0x1aff9d5f160L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 3;
	            return 16;
	         }
	         return -1;
	      case 4:
	         if ((active0 & 0x1aff9d5a120L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 4;
	            return 16;
	         }
	         if ((active0 & 0x5040L) != 0L)
	            return 16;
	         return -1;
	      case 5:
	         if ((active0 & 0x26a8402000L) != 0L)
	            return 16;
	         if ((active0 & 0x18951958120L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 5;
	            return 16;
	         }
	         return -1;
	      case 6:
	         if ((active0 & 0x8800018020L) != 0L)
	            return 16;
	         if ((active0 & 0x10151940100L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 6;
	            return 16;
	         }
	         return -1;
	      case 7:
	         if ((active0 & 0x10140140000L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 7;
	            return 16;
	         }
	         if ((active0 & 0x11800100L) != 0L)
	            return 16;
	         return -1;
	      case 8:
	         if ((active0 & 0x100040000L) != 0L)
	            return 16;
	         if ((active0 & 0x10040100000L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 8;
	            return 16;
	         }
	         return -1;
	      case 9:
	         if ((active0 & 0x10000000000L) != 0L)
	            return 16;
	         if ((active0 & 0x40100000L) != 0L)
	         {
	            jjmatchedKind = 46;
	            jjmatchedPos = 9;
	            return 16;
	         }
	         return -1;
	      default :
	         return -1;
	   }
	}
	private final int jjStartNfa_0(int pos, long active0, long active1)
	{
	   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
	}
	private int jjStopAtPos(int pos, int kind)
	{
	   jjmatchedKind = kind;
	   jjmatchedPos = pos;
	   return pos + 1;
	}
	private int jjMoveStringLiteralDfa0_0()
	{
	   switch(curChar)
	   {
	      case 33:
	         jjmatchedKind = 76;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x2L);
	      case 37:
	         jjmatchedKind = 70;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x200000L);
	      case 38:
	         jjmatchedKind = 73;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x400080L);
	      case 40:
	         return jjStopAtPos(0, 49);
	      case 41:
	         return jjStopAtPos(0, 50);
	      case 42:
	         jjmatchedKind = 68;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x80000L);
	      case 43:
	         jjmatchedKind = 66;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x1020000L);
	      case 44:
	         return jjStopAtPos(0, 54);
	      case 45:
	         jjmatchedKind = 67;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x2040000L);
	      case 46:
	         return jjStopAtPos(0, 57);
	      case 47:
	         jjmatchedKind = 69;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x100000L);
	      case 58:
	         return jjStopAtPos(0, 58);
	      case 59:
	         return jjStopAtPos(0, 53);
	      case 60:
	         jjmatchedKind = 61;
	         return jjMoveStringLiteralDfa1_0(0x8000000000000000L, 0x4008000L);
	      case 61:
	         jjmatchedKind = 59;
	         return jjMoveStringLiteralDfa1_0(0x4000000000000000L, 0x0L);
	      case 62:
	         jjmatchedKind = 60;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x8010001L);
	      case 63:
	         return jjStopAtPos(0, 75);
	      case 65:
	         return jjMoveStringLiteralDfa1_0(0x40000L, 0x0L);
	      case 67:
	         return jjMoveStringLiteralDfa1_0(0x1000000L, 0x0L);
	      case 68:
	         return jjMoveStringLiteralDfa1_0(0x300000L, 0x0L);
	      case 70:
	         return jjMoveStringLiteralDfa1_0(0x800000L, 0x0L);
	      case 72:
	         return jjMoveStringLiteralDfa1_0(0x100000000L, 0x0L);
	      case 73:
	         return jjMoveStringLiteralDfa1_0(0x10020000L, 0x0L);
	      case 76:
	         return jjMoveStringLiteralDfa1_0(0x10000L, 0x0L);
	      case 78:
	         return jjMoveStringLiteralDfa1_0(0x80000L, 0x0L);
	      case 79:
	         return jjMoveStringLiteralDfa1_0(0x200000000L, 0x0L);
	      case 83:
	         return jjMoveStringLiteralDfa1_0(0x428002000L, 0x0L);
	      case 85:
	         return jjMoveStringLiteralDfa1_0(0x40000000L, 0x0L);
	      case 86:
	         return jjMoveStringLiteralDfa1_0(0x80008000L, 0x0L);
	      case 91:
	         return jjStopAtPos(0, 55);
	      case 93:
	         return jjStopAtPos(0, 56);
	      case 94:
	         jjmatchedKind = 78;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x20000000L);
	      case 98:
	         return jjMoveStringLiteralDfa1_0(0xe0L, 0x0L);
	      case 99:
	         return jjMoveStringLiteralDfa1_0(0x4000000100L, 0x0L);
	      case 100:
	         return jjMoveStringLiteralDfa1_0(0x8000000000L, 0x0L);
	      case 101:
	         return jjMoveStringLiteralDfa1_0(0x800000200L, 0x0L);
	      case 102:
	         return jjMoveStringLiteralDfa1_0(0x1004000000L, 0x0L);
	      case 105:
	         return jjMoveStringLiteralDfa1_0(0x10000000c00L, 0x0L);
	      case 110:
	         return jjMoveStringLiteralDfa1_0(0x2000000L, 0x0L);
	      case 114:
	         return jjMoveStringLiteralDfa1_0(0x400000L, 0x0L);
	      case 115:
	         return jjMoveStringLiteralDfa1_0(0x2000001000L, 0x0L);
	      case 119:
	         return jjMoveStringLiteralDfa1_0(0x4000L, 0x0L);
	      case 123:
	         return jjStopAtPos(0, 51);
	      case 124:
	         jjmatchedKind = 74;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x800100L);
	      case 125:
	         return jjStopAtPos(0, 52);
	      case 126:
	         jjmatchedKind = 77;
	         return jjMoveStringLiteralDfa1_0(0x0L, 0x10000000L);
	      default :
	         return jjMoveNfa_0(0, 0);
	   }
	}
	private int jjMoveStringLiteralDfa1_0(long active0, long active1)
	{
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(0, active0, active1);
	      return 1;
	   }
	   switch(curChar)
	   {
	      case 38:
	         if ((active1 & 0x80L) != 0L)
	            return jjStopAtPos(1, 71);
	         break;
	      case 43:
	         if ((active1 & 0x1000000L) != 0L)
	            return jjStopAtPos(1, 88);
	         break;
	      case 45:
	         if ((active1 & 0x2000000L) != 0L)
	            return jjStopAtPos(1, 89);
	         break;
	      case 60:
	         if ((active1 & 0x8000L) != 0L)
	         {
	            jjmatchedKind = 79;
	            jjmatchedPos = 1;
	         }
	         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x4000000L);
	      case 61:
	         if ((active0 & 0x4000000000000000L) != 0L)
	            return jjStopAtPos(1, 62);
	         else if ((active0 & 0x8000000000000000L) != 0L)
	            return jjStopAtPos(1, 63);
	         else if ((active1 & 0x1L) != 0L)
	            return jjStopAtPos(1, 64);
	         else if ((active1 & 0x2L) != 0L)
	            return jjStopAtPos(1, 65);
	         else if ((active1 & 0x20000L) != 0L)
	            return jjStopAtPos(1, 81);
	         else if ((active1 & 0x40000L) != 0L)
	            return jjStopAtPos(1, 82);
	         else if ((active1 & 0x80000L) != 0L)
	            return jjStopAtPos(1, 83);
	         else if ((active1 & 0x100000L) != 0L)
	            return jjStopAtPos(1, 84);
	         else if ((active1 & 0x200000L) != 0L)
	            return jjStopAtPos(1, 85);
	         else if ((active1 & 0x400000L) != 0L)
	            return jjStopAtPos(1, 86);
	         else if ((active1 & 0x800000L) != 0L)
	            return jjStopAtPos(1, 87);
	         else if ((active1 & 0x10000000L) != 0L)
	            return jjStopAtPos(1, 92);
	         else if ((active1 & 0x20000000L) != 0L)
	            return jjStopAtPos(1, 93);
	         break;
	      case 62:
	         if ((active1 & 0x10000L) != 0L)
	         {
	            jjmatchedKind = 80;
	            jjmatchedPos = 1;
	         }
	         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x8000000L);
	      case 65:
	         return jjMoveStringLiteralDfa2_0(active0, 0x1280000L, active1, 0L);
	      case 68:
	         if ((active0 & 0x20000L) != 0L)
	            return jjStartNfaWithStates_0(1, 17, 16);
	         break;
	      case 69:
	         return jjMoveStringLiteralDfa2_0(active0, 0x108000L, active1, 0L);
	      case 73:
	         return jjMoveStringLiteralDfa2_0(active0, 0x10000L, active1, 0L);
	      case 84:
	         return jjMoveStringLiteralDfa2_0(active0, 0x400040000L, active1, 0L);
	      case 85:
	         return jjMoveStringLiteralDfa2_0(active0, 0x800000L, active1, 0L);
	      case 87:
	         return jjMoveStringLiteralDfa2_0(active0, 0x40000000L, active1, 0L);
	      case 97:
	         return jjMoveStringLiteralDfa2_0(active0, 0x4100000000L, active1, 0L);
	      case 98:
	         return jjMoveStringLiteralDfa2_0(active0, 0x200000000L, active1, 0L);
	      case 101:
	         return jjMoveStringLiteralDfa2_0(active0, 0x8082400000L, active1, 0L);
	      case 102:
	         if ((active0 & 0x400L) != 0L)
	            return jjStartNfaWithStates_0(1, 10, 16);
	         break;
	      case 104:
	         return jjMoveStringLiteralDfa2_0(active0, 0x5000L, active1, 0L);
	      case 108:
	         return jjMoveStringLiteralDfa2_0(active0, 0x200L, active1, 0L);
	      case 109:
	         return jjMoveStringLiteralDfa2_0(active0, 0x10000000L, active1, 0L);
	      case 110:
	         return jjMoveStringLiteralDfa2_0(active0, 0x10000000800L, active1, 0L);
	      case 111:
	         return jjMoveStringLiteralDfa2_0(active0, 0x1000000120L, active1, 0L);
	      case 112:
	         return jjMoveStringLiteralDfa2_0(active0, 0x8000000L, active1, 0L);
	      case 114:
	         return jjMoveStringLiteralDfa2_0(active0, 0x4000040L, active1, 0L);
	      case 116:
	         return jjMoveStringLiteralDfa2_0(active0, 0x20002000L, active1, 0L);
	      case 119:
	         return jjMoveStringLiteralDfa2_0(active0, 0x2000000000L, active1, 0L);
	      case 120:
	         return jjMoveStringLiteralDfa2_0(active0, 0x800000000L, active1, 0L);
	      case 121:
	         return jjMoveStringLiteralDfa2_0(active0, 0x80L, active1, 0L);
	      case 124:
	         if ((active1 & 0x100L) != 0L)
	            return jjStopAtPos(1, 72);
	         break;
	      default :
	         break;
	   }
	   return jjStartNfa_0(0, active0, active1);
	}
	private int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
	{
	   if (((active0 &= old0) | (active1 &= old1)) == 0L)
	      return jjStartNfa_0(0, old0, old1);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(1, active0, active1);
	      return 2;
	   }
	   switch(curChar)
	   {
	      case 61:
	         if ((active1 & 0x4000000L) != 0L)
	            return jjStopAtPos(2, 90);
	         else if ((active1 & 0x8000000L) != 0L)
	            return jjStopAtPos(2, 91);
	         break;
	      case 65:
	         return jjMoveStringLiteralDfa3_0(active0, 0x40000000L, active1, 0L);
	      case 66:
	         return jjMoveStringLiteralDfa3_0(active0, 0x10000L, active1, 0L);
	      case 76:
	         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L, active1, 0L);
	      case 77:
	         return jjMoveStringLiteralDfa3_0(active0, 0x80000L, active1, 0L);
	      case 78:
	         return jjMoveStringLiteralDfa3_0(active0, 0x800000L, active1, 0L);
	      case 82:
	         return jjMoveStringLiteralDfa3_0(active0, 0x400008000L, active1, 0L);
	      case 83:
	         return jjMoveStringLiteralDfa3_0(active0, 0x100000L, active1, 0L);
	      case 84:
	         return jjMoveStringLiteralDfa3_0(active0, 0x240000L, active1, 0L);
	      case 97:
	         return jjMoveStringLiteralDfa3_0(active0, 0x10000000L, active1, 0L);
	      case 99:
	         return jjMoveStringLiteralDfa3_0(active0, 0x80000000L, active1, 0L);
	      case 101:
	         return jjMoveStringLiteralDfa3_0(active0, 0x4000040L, active1, 0L);
	      case 102:
	         return jjMoveStringLiteralDfa3_0(active0, 0x8000000000L, active1, 0L);
	      case 105:
	         return jjMoveStringLiteralDfa3_0(active0, 0x2000004000L, active1, 0L);
	      case 106:
	         return jjMoveStringLiteralDfa3_0(active0, 0x200000000L, active1, 0L);
	      case 110:
	         return jjMoveStringLiteralDfa3_0(active0, 0x100L, active1, 0L);
	      case 111:
	         return jjMoveStringLiteralDfa3_0(active0, 0x1020L, active1, 0L);
	      case 114:
	         if ((active0 & 0x1000000000L) != 0L)
	            return jjStartNfaWithStates_0(2, 36, 16);
	         return jjMoveStringLiteralDfa3_0(active0, 0x28002000L, active1, 0L);
	      case 115:
	         return jjMoveStringLiteralDfa3_0(active0, 0x14100000200L, active1, 0L);
	      case 116:
	         if ((active0 & 0x800L) != 0L)
	            return jjStartNfaWithStates_0(2, 11, 16);
	         return jjMoveStringLiteralDfa3_0(active0, 0x800400080L, active1, 0L);
	      case 119:
	         if ((active0 & 0x2000000L) != 0L)
	            return jjStartNfaWithStates_0(2, 25, 16);
	         break;
	      default :
	         break;
	   }
	   return jjStartNfa_0(1, active0, active1);
	}
	private int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1)
	{
	   if (((active0 &= old0) | (active1 &= old1)) == 0L)
	      return jjStartNfa_0(1, old0, old1);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(2, active0, 0L);
	      return 3;
	   }
	   switch(curChar)
	   {
	      case 65:
	         if ((active0 & 0x200000L) != 0L)
	            return jjStartNfaWithStates_0(3, 21, 16);
	         break;
	      case 67:
	         return jjMoveStringLiteralDfa4_0(active0, 0x900000L);
	      case 69:
	         if ((active0 & 0x80000L) != 0L)
	            return jjStartNfaWithStates_0(3, 19, 16);
	         break;
	      case 76:
	         return jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
	      case 80:
	         return jjMoveStringLiteralDfa4_0(active0, 0x40000000L);
	      case 82:
	         return jjMoveStringLiteralDfa4_0(active0, 0x50000L);
	      case 83:
	         return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
	      case 85:
	         return jjMoveStringLiteralDfa4_0(active0, 0x400000000L);
	      case 97:
	         return jjMoveStringLiteralDfa4_0(active0, 0x8000000040L);
	      case 101:
	         if ((active0 & 0x80L) != 0L)
	            return jjStartNfaWithStates_0(3, 7, 16);
	         else if ((active0 & 0x200L) != 0L)
	            return jjStartNfaWithStates_0(3, 9, 16);
	         else if ((active0 & 0x4000000L) != 0L)
	            return jjStartNfaWithStates_0(3, 26, 16);
	         else if ((active0 & 0x4000000000L) != 0L)
	            return jjStartNfaWithStates_0(3, 38, 16);
	         return jjMoveStringLiteralDfa4_0(active0, 0xa20000000L);
	      case 103:
	         return jjMoveStringLiteralDfa4_0(active0, 0x10000000L);
	      case 104:
	         return jjMoveStringLiteralDfa4_0(active0, 0x100000000L);
	      case 105:
	         return jjMoveStringLiteralDfa4_0(active0, 0x8002000L);
	      case 108:
	         return jjMoveStringLiteralDfa4_0(active0, 0x4020L);
	      case 114:
	         return jjMoveStringLiteralDfa4_0(active0, 0x1000L);
	      case 116:
	         return jjMoveStringLiteralDfa4_0(active0, 0x12080000100L);
	      case 117:
	         return jjMoveStringLiteralDfa4_0(active0, 0x400000L);
	      default :
	         break;
	   }
	   return jjStartNfa_0(2, active0, 0L);
	}
	private int jjMoveStringLiteralDfa4_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(2, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(3, active0, 0L);
	      return 4;
	   }
	   switch(curChar)
	   {
	      case 65:
	         return jjMoveStringLiteralDfa5_0(active0, 0x10000L);
	      case 66:
	         return jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
	      case 67:
	         return jjMoveStringLiteralDfa5_0(active0, 0x400000000L);
	      case 73:
	         return jjMoveStringLiteralDfa5_0(active0, 0x48000L);
	      case 82:
	         return jjMoveStringLiteralDfa5_0(active0, 0x100000L);
	      case 83:
	         return jjMoveStringLiteralDfa5_0(active0, 0x40000000L);
	      case 84:
	         return jjMoveStringLiteralDfa5_0(active0, 0x800000L);
	      case 97:
	         return jjMoveStringLiteralDfa5_0(active0, 0x10020000000L);
	      case 99:
	         return jjMoveStringLiteralDfa5_0(active0, 0x2200000000L);
	      case 101:
	         if ((active0 & 0x4000L) != 0L)
	            return jjStartNfaWithStates_0(4, 14, 16);
	         return jjMoveStringLiteralDfa5_0(active0, 0x10000020L);
	      case 105:
	         return jjMoveStringLiteralDfa5_0(active0, 0x100L);
	      case 107:
	         if ((active0 & 0x40L) != 0L)
	            return jjStartNfaWithStates_0(4, 6, 16);
	         break;
	      case 110:
	         return jjMoveStringLiteralDfa5_0(active0, 0x800002000L);
	      case 111:
	         return jjMoveStringLiteralDfa5_0(active0, 0x80000000L);
	      case 114:
	         return jjMoveStringLiteralDfa5_0(active0, 0x400000L);
	      case 116:
	         if ((active0 & 0x1000L) != 0L)
	            return jjStartNfaWithStates_0(4, 12, 16);
	         return jjMoveStringLiteralDfa5_0(active0, 0x108000000L);
	      case 117:
	         return jjMoveStringLiteralDfa5_0(active0, 0x8000000000L);
	      default :
	         break;
	   }
	   return jjStartNfa_0(3, active0, 0L);
	}
	private int jjMoveStringLiteralDfa5_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(3, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(4, active0, 0L);
	      return 5;
	   }
	   switch(curChar)
	   {
	      case 65:
	         return jjMoveStringLiteralDfa6_0(active0, 0x1000000L);
	      case 66:
	         return jjMoveStringLiteralDfa6_0(active0, 0x40000L);
	      case 73:
	         return jjMoveStringLiteralDfa6_0(active0, 0x900000L);
	      case 79:
	         return jjMoveStringLiteralDfa6_0(active0, 0x8000L);
	      case 82:
	         return jjMoveStringLiteralDfa6_0(active0, 0x10000L);
	      case 83:
	         return jjMoveStringLiteralDfa6_0(active0, 0x10000000L);
	      case 84:
	         if ((active0 & 0x400000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 34, 16);
	         break;
	      case 97:
	         return jjMoveStringLiteralDfa6_0(active0, 0x100000020L);
	      case 100:
	         return jjMoveStringLiteralDfa6_0(active0, 0x800000000L);
	      case 101:
	         if ((active0 & 0x8000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 27, 16);
	         return jjMoveStringLiteralDfa6_0(active0, 0x40000000L);
	      case 103:
	         if ((active0 & 0x2000L) != 0L)
	            return jjStartNfaWithStates_0(5, 13, 16);
	         break;
	      case 104:
	         if ((active0 & 0x2000000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 37, 16);
	         break;
	      case 108:
	         return jjMoveStringLiteralDfa6_0(active0, 0x8000000000L);
	      case 109:
	         if ((active0 & 0x20000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 29, 16);
	         break;
	      case 110:
	         if ((active0 & 0x400000L) != 0L)
	            return jjStartNfaWithStates_0(5, 22, 16);
	         return jjMoveStringLiteralDfa6_0(active0, 0x10000000100L);
	      case 114:
	         if ((active0 & 0x80000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 31, 16);
	         break;
	      case 116:
	         if ((active0 & 0x200000000L) != 0L)
	            return jjStartNfaWithStates_0(5, 33, 16);
	         break;
	      default :
	         break;
	   }
	   return jjStartNfa_0(4, active0, 0L);
	}
	private int jjMoveStringLiteralDfa6_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(4, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(5, active0, 0L);
	      return 6;
	   }
	   switch(curChar)
	   {
	      case 67:
	         return jjMoveStringLiteralDfa7_0(active0, 0x1000000L);
	      case 78:
	         if ((active0 & 0x8000L) != 0L)
	            return jjStartNfaWithStates_0(6, 15, 16);
	         break;
	      case 79:
	         return jjMoveStringLiteralDfa7_0(active0, 0x800000L);
	      case 80:
	         return jjMoveStringLiteralDfa7_0(active0, 0x100000L);
	      case 85:
	         return jjMoveStringLiteralDfa7_0(active0, 0x40000L);
	      case 89:
	         if ((active0 & 0x10000L) != 0L)
	            return jjStartNfaWithStates_0(6, 16, 16);
	         break;
	      case 98:
	         return jjMoveStringLiteralDfa7_0(active0, 0x100000000L);
	      case 99:
	         return jjMoveStringLiteralDfa7_0(active0, 0x10000000000L);
	      case 101:
	         return jjMoveStringLiteralDfa7_0(active0, 0x10000000L);
	      case 103:
	         return jjMoveStringLiteralDfa7_0(active0, 0x40000000L);
	      case 110:
	         if ((active0 & 0x20L) != 0L)
	            return jjStartNfaWithStates_0(6, 5, 16);
	         break;
	      case 115:
	         if ((active0 & 0x800000000L) != 0L)
	            return jjStartNfaWithStates_0(6, 35, 16);
	         break;
	      case 116:
	         if ((active0 & 0x8000000000L) != 0L)
	            return jjStartNfaWithStates_0(6, 39, 16);
	         break;
	      case 117:
	         return jjMoveStringLiteralDfa7_0(active0, 0x100L);
	      default :
	         break;
	   }
	   return jjStartNfa_0(5, active0, 0L);
	}
	private int jjMoveStringLiteralDfa7_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(5, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(6, active0, 0L);
	      return 7;
	   }
	   switch(curChar)
	   {
	      case 75:
	         if ((active0 & 0x1000000L) != 0L)
	            return jjStartNfaWithStates_0(7, 24, 16);
	         break;
	      case 78:
	         if ((active0 & 0x800000L) != 0L)
	            return jjStartNfaWithStates_0(7, 23, 16);
	         break;
	      case 84:
	         return jjMoveStringLiteralDfa8_0(active0, 0x140000L);
	      case 101:
	         if ((active0 & 0x100L) != 0L)
	            return jjStartNfaWithStates_0(7, 8, 16);
	         return jjMoveStringLiteralDfa8_0(active0, 0x10000000000L);
	      case 108:
	         return jjMoveStringLiteralDfa8_0(active0, 0x100000000L);
	      case 109:
	         return jjMoveStringLiteralDfa8_0(active0, 0x40000000L);
	      case 116:
	         if ((active0 & 0x10000000L) != 0L)
	            return jjStartNfaWithStates_0(7, 28, 16);
	         break;
	      default :
	         break;
	   }
	   return jjStartNfa_0(6, active0, 0L);
	}
	private int jjMoveStringLiteralDfa8_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(6, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(7, active0, 0L);
	      return 8;
	   }
	   switch(curChar)
	   {
	      case 69:
	         if ((active0 & 0x40000L) != 0L)
	            return jjStartNfaWithStates_0(8, 18, 16);
	         break;
	      case 73:
	         return jjMoveStringLiteralDfa9_0(active0, 0x100000L);
	      case 101:
	         if ((active0 & 0x100000000L) != 0L)
	            return jjStartNfaWithStates_0(8, 32, 16);
	         return jjMoveStringLiteralDfa9_0(active0, 0x40000000L);
	      case 111:
	         return jjMoveStringLiteralDfa9_0(active0, 0x10000000000L);
	      default :
	         break;
	   }
	   return jjStartNfa_0(7, active0, 0L);
	}
	private int jjMoveStringLiteralDfa9_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(7, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(8, active0, 0L);
	      return 9;
	   }
	   switch(curChar)
	   {
	      case 79:
	         return jjMoveStringLiteralDfa10_0(active0, 0x100000L);
	      case 102:
	         if ((active0 & 0x10000000000L) != 0L)
	            return jjStartNfaWithStates_0(9, 40, 16);
	         break;
	      case 110:
	         return jjMoveStringLiteralDfa10_0(active0, 0x40000000L);
	      default :
	         break;
	   }
	   return jjStartNfa_0(8, active0, 0L);
	}
	private int jjMoveStringLiteralDfa10_0(long old0, long active0)
	{
	   if (((active0 &= old0)) == 0L)
	      return jjStartNfa_0(8, old0, 0L);
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) {
	      jjStopStringLiteralDfa_0(9, active0, 0L);
	      return 10;
	   }
	   switch(curChar)
	   {
	      case 78:
	         if ((active0 & 0x100000L) != 0L)
	            return jjStartNfaWithStates_0(10, 20, 16);
	         break;
	      case 116:
	         if ((active0 & 0x40000000L) != 0L)
	            return jjStartNfaWithStates_0(10, 30, 16);
	         break;
	      default :
	         break;
	   }
	   return jjStartNfa_0(9, active0, 0L);
	}
	private int jjStartNfaWithStates_0(int pos, int kind, int state)
	{
	   jjmatchedKind = kind;
	   jjmatchedPos = pos;
	   try { curChar = input_stream.readChar(); }
	   catch(java.io.IOException e) { return pos + 1; }
	   return jjMoveNfa_0(state, pos + 1);
	}
	static final long[] jjbitVec0 = {
	   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
	};
	static final long[] jjbitVec2 = {
	   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
	};
	private int jjMoveNfa_0(int startState, int curPos)
	{
	   int startsAt = 0;
	   jjnewStateCnt = 17;
	   int i = 1;
	   jjstateSet[0] = startState;
	   int kind = 0x7fffffff;
	   for (;;)
	   {
	      if (++jjround == 0x7fffffff)
	         ReInitRounds();
	      if (curChar < 64)
	      {
	         long l = 1L << curChar;
	         do
	         {
	            switch(jjstateSet[--i])
	            {
	               case 0:
	                  if ((0x3ff000000000000L & l) != 0L)
	                  {
	                     if (kind > 41)
	                        kind = 41;
	                     jjCheckNAdd(1);
	                  }
	                  else if (curChar == 39)
	                     jjCheckNAddStates(0, 2);
	                  else if (curChar == 34)
	                     jjCheckNAddStates(3, 5);
	                  else if (curChar == 45)
	                     jjCheckNAdd(1);
	                  if (curChar == 48)
	                     jjstateSet[jjnewStateCnt++] = 3;
	                  break;
	               case 1:
	                  if ((0x3ff000000000000L & l) == 0L)
	                     break;
	                  if (kind > 41)
	                     kind = 41;
	                  jjCheckNAdd(1);
	                  break;
	               case 2:
	                  if (curChar == 48)
	                     jjstateSet[jjnewStateCnt++] = 3;
	                  break;
	               case 4:
	                  if ((0x3ff000000000000L & l) == 0L)
	                     break;
	                  if (kind > 41)
	                     kind = 41;
	                  jjstateSet[jjnewStateCnt++] = 4;
	                  break;
	               case 5:
	                  if (curChar == 34)
	                     jjCheckNAddStates(3, 5);
	                  break;
	               case 6:
	                  if ((0xfffffffbffffdbffL & l) != 0L)
	                     jjCheckNAddStates(3, 5);
	                  break;
	               case 8:
	                  if ((0x8400000000L & l) != 0L)
	                     jjCheckNAddStates(3, 5);
	                  break;
	               case 9:
	                  if (curChar == 34 && kind > 44)
	                     kind = 44;
	                  break;
	               case 10:
	                  if (curChar == 39)
	                     jjCheckNAddStates(0, 2);
	                  break;
	               case 11:
	                  if ((0xffffff7fffffd9ffL & l) != 0L)
	                     jjCheckNAddStates(0, 2);
	                  break;
	               case 13:
	                  if ((0x8400000000L & l) != 0L)
	                     jjCheckNAddStates(0, 2);
	                  break;
	               case 14:
	                  if (curChar == 39 && kind > 45)
	                     kind = 45;
	                  break;
	               case 16:
	                  if ((0x3ff000000000000L & l) == 0L)
	                     break;
	                  if (kind > 46)
	                     kind = 46;
	                  jjstateSet[jjnewStateCnt++] = 16;
	                  break;
	               default : break;
	            }
	         } while(i != startsAt);
	      }
	      else if (curChar < 128)
	      {
	         long l = 1L << (curChar & 077);
	         do
	         {
	            switch(jjstateSet[--i])
	            {
	               case 0:
	               case 16:
	                  if ((0x7fffffe87ffffffL & l) == 0L)
	                     break;
	                  if (kind > 46)
	                     kind = 46;
	                  jjCheckNAdd(16);
	                  break;
	               case 3:
	                  if ((0x100000001000000L & l) != 0L)
	                     jjCheckNAdd(4);
	                  break;
	               case 4:
	                  if ((0x7e0000007eL & l) == 0L)
	                     break;
	                  if (kind > 41)
	                     kind = 41;
	                  jjCheckNAdd(4);
	                  break;
	               case 6:
	                  if ((0xffffffffefffffffL & l) != 0L)
	                     jjCheckNAddStates(3, 5);
	                  break;
	               case 7:
	                  if (curChar == 92)
	                     jjstateSet[jjnewStateCnt++] = 8;
	                  break;
	               case 8:
	                  if ((0x14404410000000L & l) != 0L)
	                     jjCheckNAddStates(3, 5);
	                  break;
	               case 11:
	                  if ((0xffffffffefffffffL & l) != 0L)
	                     jjCheckNAddStates(0, 2);
	                  break;
	               case 12:
	                  if (curChar == 92)
	                     jjstateSet[jjnewStateCnt++] = 13;
	                  break;
	               case 13:
	                  if ((0x14404410000000L & l) != 0L)
	                     jjCheckNAddStates(0, 2);
	                  break;
	               default : break;
	            }
	         } while(i != startsAt);
	      }
	      else
	      {
	         int hiByte = (int)(curChar >> 8);
	         int i1 = hiByte >> 6;
	         long l1 = 1L << (hiByte & 077);
	         int i2 = (curChar & 0xff) >> 6;
	         long l2 = 1L << (curChar & 077);
	         do
	         {
	            switch(jjstateSet[--i])
	            {
	               case 6:
	                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
	                     jjAddStates(3, 5);
	                  break;
	               case 11:
	                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
	                     jjAddStates(0, 2);
	                  break;
	               default : break;
	            }
	         } while(i != startsAt);
	      }
	      if (kind != 0x7fffffff)
	      {
	         jjmatchedKind = kind;
	         jjmatchedPos = curPos;
	         kind = 0x7fffffff;
	      }
	      ++curPos;
	      if ((i = jjnewStateCnt) == (startsAt = 17 - (jjnewStateCnt = startsAt)))
	         return curPos;
	      try { curChar = input_stream.readChar(); }
	      catch(java.io.IOException e) { return curPos; }
	   }
	}
	static final int[] jjnextStates = {
	   11, 12, 14, 6, 7, 9, 
	};
	private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
	{
	   switch(hiByte)
	   {
	      case 0:
	         return ((jjbitVec2[i2] & l2) != 0L);
	      default :
	         if ((jjbitVec0[i1] & l1) != 0L)
	            return true;
	         return false;
	   }
	}

	/** Token literal values. */
	public static final String[] jjstrLiteralImages = {
	"", null, null, null, null, "\142\157\157\154\145\141\156", 
	"\142\162\145\141\153", "\142\171\164\145", "\143\157\156\164\151\156\165\145", "\145\154\163\145", 
	"\151\146", "\151\156\164", "\163\150\157\162\164", "\123\164\162\151\156\147", 
	"\167\150\151\154\145", "\126\105\122\123\111\117\116", "\114\111\102\122\101\122\131", "\111\104", 
	"\101\124\124\122\111\102\125\124\105", "\116\101\115\105", "\104\105\123\103\122\111\120\124\111\117\116", 
	"\104\101\124\101", "\162\145\164\165\162\156", "\106\125\116\103\124\111\117\116", 
	"\103\101\114\114\102\101\103\113", "\156\145\167", "\146\162\145\145", "\123\160\162\151\164\145", 
	"\111\155\141\147\145\123\145\164", "\123\164\162\145\141\155", "\125\127\101\120\123\145\147\155\145\156\164", 
	"\126\145\143\164\157\162", "\110\141\163\150\164\141\142\154\145", "\117\142\152\145\143\164", 
	"\123\124\122\125\103\124", "\145\170\164\145\156\144\163", "\146\157\162", "\163\167\151\164\143\150", 
	"\143\141\163\145", "\144\145\146\141\165\154\164", "\151\156\163\164\141\156\143\145\157\146", 
	null, null, null, null, null, null, null, null, "\50", "\51", "\173", "\175", "\73", 
	"\54", "\133", "\135", "\56", "\72", "\75", "\76", "\74", "\75\75", "\74\75", 
	"\76\75", "\41\75", "\53", "\55", "\52", "\57", "\45", "\46\46", "\174\174", "\46", 
	"\174", "\77", "\41", "\176", "\136", "\74\74", "\76\76", "\53\75", "\55\75", 
	"\52\75", "\57\75", "\45\75", "\46\75", "\174\75", "\53\53", "\55\55", "\74\74\75", 
	"\76\76\75", "\176\75", "\136\75", };

	/** Lexer state names. */
	public static final String[] lexStateNames = {
	   "DEFAULT",
	};
	static final long[] jjtoToken = {
	   0xfffe73ffffffffe1L, 0x3fffffffL, 
	};
	static final long[] jjtoSkip = {
	   0x1eL, 0x0L, 
	};
	protected SimpleCharStream input_stream;
	private final int[] jjrounds = new int[17];
	private final int[] jjstateSet = new int[34];
	protected char curChar;
	/** Constructor. */
	public GenericTokenManager(SimpleCharStream stream){
	   if (SimpleCharStream.staticFlag)
	      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
	   input_stream = stream;
	}

	/** Constructor. */
	public GenericTokenManager(SimpleCharStream stream, int lexState){
	   this(stream);
	   SwitchTo(lexState);
	}

	/** Reinitialise parser. */
	public void ReInit(SimpleCharStream stream)
	{
	   jjmatchedPos = jjnewStateCnt = 0;
	   curLexState = defaultLexState;
	   input_stream = stream;
	   ReInitRounds();
	}
	private void ReInitRounds()
	{
	   int i;
	   jjround = 0x80000001;
	   for (i = 17; i-- > 0;)
	      jjrounds[i] = 0x80000000;
	}

	/** Reinitialise parser. */
	public void ReInit(SimpleCharStream stream, int lexState)
	{
	   ReInit(stream);
	   SwitchTo(lexState);
	}

	/** Switch to specified lex state. */
	public void SwitchTo(int lexState)
	{
	   if (lexState >= 1 || lexState < 0)
	      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
	   else
	      curLexState = lexState;
	}

	protected Token jjFillToken()
	{
	   final Token t;
	   final String curTokenImage;
	   final int beginLine;
	   final int endLine;
	   final int beginColumn;
	   final int endColumn;
	   String im = jjstrLiteralImages[jjmatchedKind];
	   curTokenImage = (im == null) ? input_stream.GetImage() : im;
	   beginLine = input_stream.getBeginLine();
	   beginColumn = input_stream.getBeginColumn();
	   endLine = input_stream.getEndLine();
	   endColumn = input_stream.getEndColumn();
	   t = Token.newToken(jjmatchedKind, curTokenImage);

	   t.beginLine = beginLine;
	   t.endLine = endLine;
	   t.beginColumn = beginColumn;
	   t.endColumn = endColumn;

	   return t;
	}

	int curLexState = 0;
	int defaultLexState = 0;
	int jjnewStateCnt;
	int jjround;
	int jjmatchedPos;
	int jjmatchedKind;

	/** Get the next Token. */
	public Token getNextToken() 
	{
	  Token matchedToken;
	  int curPos = 0;

	  EOFLoop :
	  for (;;)
	  {
	   try
	   {
	      curChar = input_stream.BeginToken();
	   }
	   catch(java.io.IOException e)
	   {
	      jjmatchedKind = 0;
	      matchedToken = jjFillToken();
	      return matchedToken;
	   }

	   try { input_stream.backup(0);
	      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
	         curChar = input_stream.BeginToken();
	   }
	   catch (java.io.IOException e1) { continue EOFLoop; }
	   jjmatchedKind = 0x7fffffff;
	   jjmatchedPos = 0;
	   curPos = jjMoveStringLiteralDfa0_0();
	   if (jjmatchedKind != 0x7fffffff)
	   {
	      if (jjmatchedPos + 1 < curPos)
	         input_stream.backup(curPos - jjmatchedPos - 1);
	      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
	      {
	         matchedToken = jjFillToken();
	         return matchedToken;
	      }
	      else
	      {
	         continue EOFLoop;
	      }
	   }
	   int error_line = input_stream.getEndLine();
	   int error_column = input_stream.getEndColumn();
	   String error_after = null;
	   boolean EOFSeen = false;
	   try { input_stream.readChar(); input_stream.backup(1); }
	   catch (java.io.IOException e1) {
	      EOFSeen = true;
	      error_after = curPos <= 1 ? "" : input_stream.GetImage();
	      if (curChar == '\n' || curChar == '\r') {
	         error_line++;
	         error_column = 0;
	      }
	      else
	         error_column++;
	   }
	   if (!EOFSeen) {
	      input_stream.backup(1);
	      error_after = curPos <= 1 ? "" : input_stream.GetImage();
	   }
	   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
	  }
	}

	private void jjCheckNAdd(int state)
	{
	   if (jjrounds[state] != jjround)
	   {
	      jjstateSet[jjnewStateCnt++] = state;
	      jjrounds[state] = jjround;
	   }
	}
	private void jjAddStates(int start, int end)
	{
	   do {
	      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
	   } while (start++ != end);
	}
	private void jjCheckNAddTwoStates(int state1, int state2)
	{
	   jjCheckNAdd(state1);
	   jjCheckNAdd(state2);
	}

	private void jjCheckNAddStates(int start, int end)
	{
	   do {
	      jjCheckNAdd(jjnextStates[start]);
	   } while (start++ != end);
	}

}
