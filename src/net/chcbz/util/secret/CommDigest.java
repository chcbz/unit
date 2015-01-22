package net.chcbz.util.secret;

public final class CommDigest 
{
	public static final char[] elem = new char[]
	{
		'S','3','c','5','J','m','n','V','7','s','4',
		'1','b','G','d','e','U','f','g','h','i','j',
		'k','O','l','2','o','p','q','M','r','t','u',
		'F','v','H','w','x','W','z','0','Q','A','a',
		'C','6','E','Z','I','K','L','N','Y','P','R',
		'T','y','B','8','X','D','9'
	};
	protected static final int ele_len = elem.length;

	public static void main(String[] args) throws Exception
	{
		char[] dd = "dsfsdf".toCharArray();
		char[] dd2 = dd.clone();
		System.out.println(new String(dd));
		System.out.println(new String(dd2));
		dd[1] = '3';
		dd2[3]= '7';
		System.out.println(new String(dd));
		System.out.println(new String(dd2));
	}
	
	public static final void digest(char[] msg,int v)
	{
		if(v<0) v=-v;
		int len = msg.length;
		for(int i=0;i<len;i++)
		{
			int ch1 = (int)msg[i];
			int ch2 = msg[(ch1+i)%msg.length];
			int cs = ((ch1+7-i)*v+ch2)+v-21+i;
			if(cs<0) cs = -cs;
			msg[i] = elem[cs%ele_len];
		}
	}
	public static final void digest(char[] msg,char v)
	{
		int len = msg.length;
		for(int i=0;i<len;i++)
		{
			int ch1 = (int)msg[i];
			int ch2 = msg[(ch1+i)%msg.length];
			int cs = ((ch1+9-i)*v+ch2)+v-29+i;
			if(cs<0) cs = -cs;
			msg[i] = elem[cs%ele_len];
		}
	}
	public static final void digest(char[] msg,String v)
	{
		int len = msg.length;
		for(int i=0;i<len;i++)
		{
			int v2 = v.charAt(i%v.length())+i;
			int ch1 = (int)msg[i];
			int ch2 = msg[(ch1+i)%msg.length];
			int cs = ((ch1+6-i)*v2+ch2)+v2-11+i;
			if(cs<0) cs = -cs;
			msg[i] = elem[cs%ele_len];
		}
	}
	public static final void digest(char[] msg,long v)
	{
		int len = msg.length;
		for(int i=0;i<len;i++)
		{
			int v2 = (int)v;
			int ch1 = (int)msg[i];
			int ch2 = msg[(ch1+i)%msg.length]+1;
			int cs = ((ch1+4-i)*v2+ch2)+v2-7+i;
			if(cs<0) cs = -cs;
			msg[i] = elem[cs%ele_len];
		}
	}
}
