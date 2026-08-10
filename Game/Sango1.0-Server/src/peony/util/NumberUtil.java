package peony.util;

public abstract class NumberUtil {
	
	public static long parseLong(final String s) {
		if (s == null)
			throw new NumberFormatException("Null string");
		long num = 0;
		int sign = -1;
		final int len = s.length();
		final char ch = s.charAt(0);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException("Missing digits:  " + s);
			sign = 1;
		} else {
			final int d = ch - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			num = -d;
		}
		final long max = (sign == -1) ? -Long.MAX_VALUE : Long.MIN_VALUE;
		final long multmax = max / 10;
		int i = 1;
		while (i < len) {
			int d = s.charAt(i++) - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			if (num < multmax)
				throw new NumberFormatException("Over/underflow:  " + s);
			num *= 10;
			if (num < (max + d))
				throw new NumberFormatException("Over/underflow:  " + s);
			num -= d;
		}
		return sign * num;
	}
	
	
	public static int parseInt( final String s )
	{
	    if ( s == null )
	        throw new NumberFormatException( "Null string" );

	    int num  = 0;
	    int sign = -1;
	    final int len  = s.length( );
	    final char ch  = s.charAt( 0 );
	    if ( ch == '-' )
	    {
	        if ( len == 1 )
	            throw new NumberFormatException( "Missing digits:  " + s );
	        sign = 1;
	    }
	    else
	    {
	        final int d = ch - '0';
	        if ( d < 0 || d > 9 )
	            throw new NumberFormatException( "Malformed:  " + s );
	        num = -d;
	    }

	    final int max = (sign == -1) ?
	        -Integer.MAX_VALUE : Integer.MIN_VALUE;
	    final int multmax = max / 10;
	    int i = 1;
	    while ( i < len )
	    {
	        int d = s.charAt(i++) - '0';
	        if ( d < 0 || d > 9 )
	            throw new NumberFormatException( "Malformed:  " + s );
	        if ( num < multmax )
	            throw new NumberFormatException( "Over/underflow:  " + s );
	        num *= 10;
	        if ( num < (max+d) )
	            throw new NumberFormatException( "Over/underflow:  " + s );
	        num -= d;
	    }

	    return sign * num;
	}
	

	public static int parseValidInt(final String s){
	    int num  = 0;
	    int sign = -1;
	    final int len  = s.length( );
	    final char ch  = s.charAt( 0 );
	    if ( ch == '-' )
	        sign = 1;
	    else
	        num = '0' - ch;

	    int i = 1;
	    while ( i < len )
	        num = num*10 + '0' - s.charAt( i++ );

	    return sign * num;

	}
}
