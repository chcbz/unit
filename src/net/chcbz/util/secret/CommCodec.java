package net.chcbz.util.secret;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import net.chcbz.util.Helper;


public class CommCodec
{
	protected static final String 	MY64_TOKENS 			= "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
	protected static final String		BS64_TOKENS				= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	public static final String Tel64Encode(String mobile)
	{
		StringBuffer sb = new StringBuffer();
		long l = Long.parseLong(mobile);
		while(true)
		{
			long s = l/64;
			long y = l%64;
			sb.append(BS64_TOKENS.charAt((int)y));
			l = s;
			if(l==0) break;
		}
		String s = sb.toString();
		sb = new StringBuffer("AA");
		for(int i=s.length()-1;i>=0;i--) sb.append(s.charAt(i));
		return sb.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
//		System.out.println(new String(CommCodec.Inflate(CommCodec.Deflate("350226021357818".getBytes(),1024),1024)));
		System.out.println(CommCodec.MyEncode("feiliu_fkdkafd".getBytes()));
		System.out.println(new String(CommCodec.MyDecode("2Ws3nWfZfHVrTTKKi"), "utf-8"));
	}

	public static boolean checkImeiValid(String imei)
	{
		if(imei==null || (imei.length()!=15 && imei.length()!=17)) return false;
		int check = calcImeiCheck(imei.substring(0,14));
		if(check<0) return false;
		if(check!=imei.charAt(14)-'0') return false;
		return true;
	}
	
	public static String genValidImei(String imei)
	{
		if(checkImeiValid(imei)) return imei; 
		return imei.substring(0,14) + calcImeiCheck(imei.substring(0,14));
	}
	
	public static int calcImeiCheck(String imei)
	{
		int res = -1;
		if(imei.length()==14 && Helper.strIsProductionOf(imei,"0123456789"))
		{
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<imei.length();i++)
			{
				if(i%2==0) sb.append(imei.charAt(i));
				else sb.append(String.valueOf(Integer.parseInt(String.valueOf(imei.charAt(i)))*2));
			}
			int sum = 0;
			for(int i=0;i<sb.length();i++) sum += sb.charAt(i)-'0';
			res = (10-sum%10)%10;
		}
		return res;
	}
	
	public static final String MySafeTransEncode(String val,String share_secrect) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutput dop = new DataOutputStream(baos);
		long current_ts = System.currentTimeMillis();
		char[] msg = share_secrect.toCharArray().clone();
		CommDigest.digest(msg,current_ts);
		CommDigest.digest(msg,val);
		dop.writeUTF(new String(msg));
		dop.writeUTF(val);
		dop.writeLong(current_ts);
		return My64Encode(baos.toByteArray());
	}
	public static final String MySafeTransDecode(String str,String share_secrect,int timeout_secs) throws Exception
	{
		byte[] bts = My64Decode(str);
		if(bts==null) return null;
		DataInput dip = new DataInputStream(new ByteArrayInputStream(bts));
		String read_msg = dip.readUTF();
		String read_val = dip.readUTF();
		long read_ts = dip.readLong();
		char[] msg = share_secrect.toCharArray().clone();
		CommDigest.digest(msg,read_ts);
		CommDigest.digest(msg,read_val);
		if(new String(msg).equals(read_msg)==false) return null;
		long current_ts = System.currentTimeMillis();
		if(current_ts-read_ts > timeout_secs*1000) return null;
		return read_val;
	}
	
	public static final String My64Encode(byte[] bts)
	{
		StringBuffer sb = new StringBuffer();
		int len = bts.length;
		int bt0,bt1,bt2,bt3;
		for(int i=0;i<len;i+=3)
		{
			bt0 = bts[i]&0x3f;
			bt1 = (bts[i]&0xc0)>>6;
			if(i+1>=len)
			{
				sb.append(MY64_TOKENS.charAt(bt0));
				sb.append(MY64_TOKENS.charAt(bt1));
			}
			else
			{
				bt1 |= ((bts[i+1]&0x0f)<<2);
				bt2 = ((bts[i+1]&0xf0)>>4);
				
				if(i+2>=len)
				{
					sb.append(MY64_TOKENS.charAt(bt0));
					sb.append(MY64_TOKENS.charAt(bt1));
					sb.append(MY64_TOKENS.charAt(bt2));
				}
				else
				{
					bt2 |= ((bts[i+2]&0x03)<<4);
					bt3 = ((bts[i+2]&0xfc)>>2);
					sb.append(MY64_TOKENS.charAt(bt0));
					sb.append(MY64_TOKENS.charAt(bt1));
					sb.append(MY64_TOKENS.charAt(bt2));
					sb.append(MY64_TOKENS.charAt(bt3));
				}
			}
		}
		return sb.toString();
	}
	public static final byte[] My64Decode(String str) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = str.length();
		if(len%4==1) return null;
		for(int i=0;i<len;i+=4)
		{
			if(i+2==len)
			{
				int ch0 = MY64_TOKENS.indexOf(str.charAt(i));
				int ch1 = MY64_TOKENS.indexOf(str.charAt(i+1));
				if(ch0<0 || ch1<0) return null;
				baos.write(ch0 | ((ch1&0x03)<<6));
			}
			else if(i+3==len)
			{
				int ch0 = MY64_TOKENS.indexOf(str.charAt(i));
				int ch1 = MY64_TOKENS.indexOf(str.charAt(i+1));
				int ch2 = MY64_TOKENS.indexOf(str.charAt(i+2));
				if(ch0<0 || ch1<0 || ch2<0) return null;
				baos.write(ch0 | ((ch1&0x03)<<6));
				baos.write(((ch1&0x3c)>>2) | ((ch2&0x0f)<<4));
			}
			else
			{
				int ch0 = MY64_TOKENS.indexOf(str.charAt(i));
				int ch1 = MY64_TOKENS.indexOf(str.charAt(i+1));
				int ch2 = MY64_TOKENS.indexOf(str.charAt(i+2));
				int ch3 = MY64_TOKENS.indexOf(str.charAt(i+3));
				baos.write(ch0 | ((ch1&0x03)<<6));
				baos.write(((ch1&0x3c)>>2) | ((ch2&0x0f)<<4));
				baos.write(((ch2&0x30)>>4) | ((ch3&0x3f)<<2));
			}
		}
		return baos.toByteArray();
	}
		

	
	public static final int writeVIntEx(DataOutput dop,int v) throws Exception
	{
		byte sign_bit = 0;
		if(v<0)
		{
			sign_bit = (byte)0x80;
			v = -v;
		}
		if(v<0x40) 
		{//11 000000
			dop.writeByte( v&0x3f|sign_bit );
			return 1;
		}
		else if(v<0x2000)
		{// 10000000 000000
			dop.writeByte(v&0x3f|0x40|sign_bit);
			dop.writeByte((v>>6)&0x3f);
			return 2;
		}
		else if(v<0x100000)
		{// 10000000 0000000 000000
			dop.writeByte(v&0x3f|0x40|sign_bit);
			dop.writeByte((v>>6)&0x7f|0x80);
			dop.writeByte((v>>13)&0x7f);
			return 3;
		}
		else if(v<0x8000000)
		{// 1000000000000000000000000000
			dop.writeByte(v&0x3f|0x40|sign_bit);
			dop.writeByte((v>>6)&0x7f|0x80);
			dop.writeByte((v>>13)&0x7f|0x80);
			dop.writeByte((v>>20)&0x7f);
			return 4;
		}
		else
		{
			dop.writeByte(v&0x3f|0x40|sign_bit);
			dop.writeByte((v>>6)&0x7f|0x80);
			dop.writeByte((v>>13)&0x7f|0x80);
			dop.writeByte((v>>20)&0x7f|0x80);
			dop.writeByte((v>>27)&0x7f);
			return 5;
		}	
	}
	public static final int readVIntEx(DataInputStream dis) throws Exception
	{
		int bt = dis.readByte();
		boolean sign_bit = false;
		if((bt&0x80)!=0) sign_bit = true; 
		int res = bt&0x3f;
		if((bt&0x40)==0) return (sign_bit?-res:res);
		bt = dis.readByte();
		res += ((bt&0x7f)<<6);
		if((bt&0x80)==0) return (sign_bit?-res:res);
		bt = dis.readByte();
		res += (bt&0x7f)<<13;
		if((bt&0x80)==0) return (sign_bit?-res:res);
		bt = dis.readByte();
		res += (bt&0x7f)<<20;
		if((bt&0x80)==0) return (sign_bit?-res:res);
		bt = dis.readByte();
		res += (bt&0x7f)<<27;
		return (sign_bit?-res:res);
	}
	
	public static final int writeVInt(DataOutput dop,int v) throws Exception
	{
		if(v<0) Helper.E("error writing <0 int");
		int len = 0;
		if(v<0x80) 
		{
			dop.writeByte( v&0x7f );
			len = 1;
		}
		else if(v<0x4000)
		{
			dop.writeByte(v&0x7f|0x80);
			dop.writeByte((v>>7)&0x7f);
			len = 2;
		}
		else if(v<0x200000)
		{
			dop.writeByte(v&0x7f|0x80);
			dop.writeByte((v>>7)&0x7f|0x80);
			dop.writeByte((v>>14)&0x7f);
			len = 3;
		}
		else
		{
			dop.writeByte(v&0x7f|0x80);
			dop.writeByte((v>>7)&0x7f|0x80);
			dop.writeByte((v>>14)&0x7f|0x80);
			dop.writeByte((v>>21)&0x7f);
			len = 4;
		}
		return len;
	}
	public static int writeVStr(DataOutput dp,String s) throws Exception
	{
		int len = writeVInt(dp,s.length());
		dp.writeChars(s);
		len += s.length()*2;
		return len;
	}
	public static final int readVInt(DataInputStream dis) throws Exception
	{
		int bt = dis.readByte();
		int res = bt&0x7f;
		if((bt&0x80)==0) return res;
		bt = dis.readByte();
		res += ((bt&0x7f)<<7);
		if((bt&0x80)==0) return res;
		bt = dis.readByte();
		res += (bt&0x7f)<<14;
		if((bt&0x80)==0) return res;
		bt = dis.readByte();
		res += (bt&0x7f)<<21;
		return res;
	}
	public static final String readVStr(DataInputStream dis) throws Exception
	{
		int len = readVInt(dis);		
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<len;i++)
		{
			char ch = dis.readChar();
			 sb.append(ch);
		}
		return sb.toString();
	}

	
	public static final String MyDigest(String input)
	{
		return null;
	}
	
	public static final List<Integer> decodeIntList(String src,String delm) throws Exception
	{
		List<Integer> res = new ArrayList<Integer>();
		StringTokenizer st = new StringTokenizer(src,delm);
		while(st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if(Helper.strIsInt(tok,false)==false) continue;
			res.add(Integer.parseInt(tok));
		}
		return res;
	}
	
	public static final int[] decodeIntArray(String src) throws Exception
	{
		StringTokenizer st = new StringTokenizer(src,",'\"������");
		List<Integer> list = new ArrayList<Integer>();
		while(st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if(Helper.strIsInt(tok,false)==false) continue;
			list.add(Integer.parseInt(tok));
		}
		int[] res = new int[list.size()];
		for(int i=0;i<list.size();i++) res[i] = list.get(i);
		return res;
	}
	public static final long[] decodeLongArray(String src) throws Exception
	{
		StringTokenizer st = new StringTokenizer(src,",'\"������");
		List<Long> list = new ArrayList<Long>();
		while(st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if(Helper.strIsInt(tok,false)==false) continue;
			list.add(Long.parseLong(tok));
		}
		long[] res = new long[list.size()];
		for(int i=0;i<list.size();i++) res[i] = list.get(i);
		return res;
	}
	public static final String[] decodeStringArray(String src) throws Exception
	{
		StringTokenizer st = new StringTokenizer(src,",");
		List<String> list = new ArrayList<String>();
		while(st.hasMoreTokens()) list.add(st.nextToken());
		String[] res = new String[list.size()];
		for(int i=0;i<list.size();i++) res[i] = list.get(i);
		return res;
	}
	public static final String encodeIntArray(int[] src) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length;i++)
		{
			if(sb.length()>0) sb.append(',');
			sb.append(src[i]);
		}
		return sb.toString();
	}
	public static final String encodeLongArray(long[] src) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length;i++)
		{
			if(sb.length()>0) sb.append(',');
			sb.append(src[i]);
		}
		return sb.toString();
	}
	
	
	protected static String[] 	m_encode_maps = null;
	protected static int[][] 	m_index_maps = null;
	protected static final String 	m_offset_map = "nTusYL3MXKgvitzV"; 
	protected static final String[] getEncodeMaps()
	{
		if(m_encode_maps==null)
		{
			m_encode_maps = new String[]
		   	{
		   		"fY8T1z9xMvIO37NU",
		      	"7rsVXn4HDoNjqlwU",
		       	"DoHfxKpmYROG0t6C",
		       	"NEWfBZL1sHuliqAh",
		       	"MjWnpEl6aR1hqZUz",
		       	"naks3T5M08cFWjPD",
		       	"Pjp2efurWw0DdG8C",
		       	"VfQBOPUtkhHz5SYm",
		       	"SxWDOpkNlUvtynFR",
		       	"36uYwTWarHEgpnfy",
		       	"YedWv56rCbPfsRHZ",
		       	"taoiKcyZQeVp9nWH",
		       	"DgE0KH8Xqmc2hxQi",
		       	"ds27hnjWZyH5pCvF",
		       	"BSniRafpeCWAc1MD",
		       	"C1jKTD9oy8XpLauA",
		    };
		}
		return m_encode_maps;
	}
	protected static final int[][] getIndexMaps()
	{
		if(m_index_maps==null)
		{
			m_index_maps = new int[][] 
			{
			    new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
			    new int[]{6,5,4,3,2,1,0,15,14,13,12,11,10,9,8,7},
			    new int[]{5,4,3,2,1,0,8,7,6,15,14,13,12,11,10,9},
			    new int[]{4,3,2,1,0,11,10,9,8,7,6,5,15,14,13,12},
			    new int[]{3,2,1,0,7,6,5,4,15,14,13,12,11,10,9,8},
			    new int[]{2,1,0,10,9,8,7,6,5,4,3,15,14,13,12,11},
			    new int[]{1,0,5,4,3,2,15,14,13,12,11,10,9,8,7,6},
			    new int[]{15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0},
			    new int[]{10,9,8,7,6,5,4,3,2,1,0,11,12,13,14,15},
			    new int[]{9,8,7,6,5,4,3,2,1,0,10,11,12,13,14,15},
			    new int[]{8,7,6,5,4,3,2,1,0,9,10,11,12,13,14,15},
			    new int[]{7,6,5,4,3,2,1,0,15,14,13,12,11,10,9,8},
			    new int[]{6,5,4,3,2,1,0,15,14,13,7,8,9,10,11,12},
			    new int[]{5,4,3,2,1,0,11,12,13,14,15,6,7,8,9,10},
			    new int[]{11,12,13,14,15,7,8,9,10,0,1,2,3,4,5,6},
			    new int[]{12,13,14,15,6,7,8,9,10,11,0,1,2,3,4,5}
			};
		}
		return m_index_maps;
	}
	public static final String MyEncode(byte[] bts) throws Exception
	{
		Random rand = new Random(System.currentTimeMillis());
		int offset = rand.nextInt(m_offset_map.length());
		int[] index_map = getIndexMaps()[offset];
		String[] encode_maps = getEncodeMaps();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<bts.length;i++)
		{
			String encode_map = encode_maps[index_map[i%16]];//���bt���õ�map
			byte bt = bts[i];
			int h_offset = (bt>>4)&0x0f;
			int l_offset = bt&0x0f;
			sb.append(encode_map.charAt(h_offset));
			sb.append(encode_map.charAt(l_offset));
		}
		sb.append(m_offset_map.charAt(offset));
		return sb.toString();
	}
	public static final byte[] MyDecode(String str) throws Exception
	{
		int len = str.length();
		if(len%2!=1) Helper.E("invalid decode str odd len");
		int offset = m_offset_map.indexOf(str.charAt(len-1));
		if(offset<0) Helper.E("invalid offset ");
		int[] index_map = getIndexMaps()[offset];
		String[] decode_maps = getEncodeMaps();
		int bts_len = (len-1)/2;
		byte[] bts = new byte[bts_len];
		for(int i=0;i<bts_len;i++)
		{
			String decode_map = decode_maps[index_map[i%16]];//���bt���õ�map
			int high = decode_map.indexOf(str.charAt(i*2));
			if(high<0 || high>=16) Helper.E("invalid high " + high);
			int low = decode_map.indexOf(str.charAt(i*2+1));
			if(low<0 || low>=16) Helper.E("invalid low " + low);
			bts[i] = (byte)((high << 4) + low);
		}
		return bts;
	}
	protected static final String[] genEncodeMaps(String tokens,int len,int count)
	{
		String[] res = new String[count];
		Random rand = new Random(System.currentTimeMillis());
		for(int i=0;i<count;i++)
		{
			String map = genEncodeMap(tokens,len,rand);
			for(int j=0;j<i;j++) if(res[j].equals(map)) {i--;continue;}
			res[i] = map;
		}
		return res;
	}
	protected static final String genEncodeMap(String tokens,int len,Random rand)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<len;i++)
		{
			char ch = tokens.charAt(rand.nextInt(tokens.length()));
			if(sb.indexOf(String.valueOf(ch))>=0) {i--;continue;}
			sb.append(ch);
		}
		return sb.toString();
	}
	protected static final void writeEncodeMaps(String file) throws Exception
	{
		FileOutputStream fos = new FileOutputStream("ff.txt");
		String key = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String[] maps = genEncodeMaps(key,16,17);
		for(int i=0;i<maps.length;i++)
		{
			fos.write(("\"" + maps[i] + "\",\r\n").getBytes());
			
		}
		fos.close();
	}
	
	
	
	/*	private static char encode(char c)
	{
		return c;
	}

	private static String encode(String s)
	{
		int len = s.length();
		StringBuffer sb = new StringBuffer(len);
		for(int i=0;i<len;i++) sb.append(encode(s.charAt(i)));
		return sb.toString();
	}*/

	
	protected static char[] m_j2f = getj2f();
	protected static char[] m_f2j = getf2j();
	protected static byte[] m_gbkcpt = getgbkcpt();
	protected final static int m_fjlen  = 20902;
	protected final static int offset_l = 0x4e00;
	protected final static int offset_r = 0x9fa5;
	
	protected final static byte[] getgbkcpt()
	{
		if(m_gbkcpt==null)
		{
			InputStream is = null;
            try
			{
            	m_gbkcpt = new byte[m_fjlen];
				is = CommCodec.class.getResourceAsStream("gbkcpt.txt");
				Helper.Assert(is!=null,"can't get gbkcpt.txt");
				for(int i=0;i<m_fjlen;i++) m_gbkcpt[i] = (byte)is.read();
			}
            catch(Exception e)
			{
				Helper.RE(e.getMessage());
			}
            finally
            {
                if(is!=null)
                {
                	try{is.close();}
                	catch(Exception ee){}
                }
            }
		}
		return m_gbkcpt;
	}	
	
	//0: ��GBK�ַ��ַ�
	//1: GBK1:���
	//2: GBK2:����GB2312����
	//3: GBK3:��չ1
	//4: GBK4:��չ2
	//5: GBK5:���
	//6: ASCII
	public static final int getGbkCharCat(char ch)
	{
		if(ch>=offset_l && ch<=offset_r) return m_gbkcpt[ch-offset_l];
		return judgeGbkCharCat(ch);
	}
	public static final int judgeGbkCharCat(char ch)
	{
		if(ch<0x7f) return 6;
		byte[] bts = null; 
		try{bts=String.valueOf(ch).getBytes("GBK");}catch(Exception e){return 0;}
		if(bts.length!=2) return 0;
		int hb = (bts[0]>=0 ? bts[0] : bts[0] + 256);
		int lb = (bts[1]>=0 ? bts[1] : bts[1] + 256);
		if(hb>=0xa1 && hb<=0xa9 && lb>=0xa0 && lb<=0xfe) return 1;
		if(hb>=0xa8 && hb<=0xa9 && lb>=0x40 && lb<=0x9e) return 5;
		if(hb>=0xb0 && hb<=0xf7 && lb>=0xa1 && lb<=0xfe) return 2;
		if(hb>=0x81 && hb<=0xa0 && lb>=0x40 && lb<=0xfe) return 3;
		if(hb>=0xaa && hb<=0xfe && lb>=0x40 && lb<=0xa0) return 4;
		return 0;
	}
	public static final double getGbkStrScore(String src)
	{
		int res = 0;
		int len = src.length();
		if(len==0) return 1;
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			if(getGbkCharCat(ch)>0) res++; 
		}
		return ((double)res)/((double)len);
	}

	protected final static char[] getj2f()
	{
		if(m_j2f==null)
		{
			InputStream is = null;
            try
			{
				m_j2f = new char[m_fjlen];
				is = CommCodec.class.getResourceAsStream("j2f.txt");
				Helper.Assert(is!=null,"can't get j2f.txt");
				Reader rd  = new InputStreamReader(is,"UNICODE");
				for(int i=0;i<m_fjlen;i++) m_j2f[i] = (char)rd.read();
			}
            catch(Exception e)
			{
				Helper.RE(e.getMessage());
			}
            finally
            {
                if(is!=null)
                {
                	try{is.close();}
                	catch(Exception ee){}
                }
            }
		}
		return m_j2f;
	}
	protected static char[] getf2j()
	{
		if(m_f2j==null)
		{
            InputStream is = null;
            try
			{
				m_f2j = new char[m_fjlen];
				is = CommCodec.class.getResourceAsStream("f2j.txt");
				Helper.Assert(is!=null);
				Reader rd = new InputStreamReader(is,"UNICODE");
				for(int i=0;i<m_fjlen;i++) m_f2j[i] = (char)rd.read();
			}
            catch(Exception e)
            {
				Helper.RE(e.getMessage());
			}
            finally
            {
            	if(is != null)
            	{
            		try { is.close(); }
            		catch (Exception ee) { } 
            	}
            }
		}
		return m_f2j;
	}

	public static char j2f(char ch)
	{	
//		if(ch=='\u4e7e')//Ǭ
//			return ch;
		if(ch>=offset_l && ch<=0x9fa5) ch = getj2f()[ch-offset_l];
		return ch;
	}
	public static char f2j(char ch)
	{
//		if(ch=='\u4e7e')//Ǭ
//			return ch;
		if(ch>=offset_l && ch<=0x9fa5) ch = getf2j()[ch-offset_l];
		return ch;
	}
	public static String J2FEncode(String src) throws Exception
	{
		StringBuffer des = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++) des.append(j2f(src.charAt(i)));
		return des.toString();
	}
	public static String F2JEncode(String src) throws Exception
	{
		StringBuffer des = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++) des.append(f2j(src.charAt(i)));
		return des.toString();
	}
	
	

	
	
	public static String RandomStrEncode(String str) throws Exception
	{
		return Base64Encode(RandomBtsEncode(str.getBytes("UTF-8")));
	}
	public static String RandomStrDecode(String str) throws Exception
	{
		return new String(RandomBtsDecode(Base64Decode(str)),"UTF-8");
	}
	
	public static byte[] RandomBtsEncode(byte[] in)
	{
		Random r = new Random(System.currentTimeMillis());
		byte[] t = new byte[1];
		r.nextBytes(t);
		
		byte[] res = new byte[in.length+1];
		for(int i=0;i<in.length;i++) 
			res[i] = (byte)(in[i] ^ t[0]); 
		res[in.length] = t[0];
		return res;
	}
	public static byte[] RandomBtsDecode(byte[] in)
	{
		byte t = in[in.length-1];
		byte[] res = new byte[in.length-1];
		for(int i=0;i<res.length;i++)
			res[i] = (byte)(in[i] ^ t);
		return res;
	}
	
	public static String StringSetEncode(Set<String> set,char delm)
	{
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = set.iterator();
		int i=0;
		while(it.hasNext())
		{
			if(i++>0) sb.append(delm);
			sb.append((String)it.next());
		}
		return sb.toString();		
	}
	
	public static String StringListEncode(List<String> list,char delm)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<list.size();i++)
		{
			if(i>0) sb.append(delm);
			sb.append(list.get(i));
		}
		return sb.toString();		
	}
	public static List<String> StringListDecode(String src,char delm)
	{
		String[] res =  src.split(delm + "");
		List<String> list = new ArrayList<String>();
		for(int i=0;i<res.length;i++) list.add(res[i]);
		return list;
	}
	
	public static String StringArrayEncode(String[] src,char delm)
	{
		StringBuffer sb = new StringBuffer();
		if(src==null) return sb.toString();
		for(int i=0;i<src.length;i++)
		{
			if(i>0) sb.append(delm);
			sb.append(src[i]);
		}
		return sb.toString();
	}
	public static String StringArrayEncodeNotNull(String[] src,char delm)
	{
		StringBuffer sb = new StringBuffer();
		if(src==null) return sb.toString();
		for(int i=0;i<src.length;i++)
		{
			if(src[i] != null && src[i].length() > 0){
				if(i>0) sb.append(delm);
				sb.append(src[i]);
			}
		}
		return sb.toString();
	}
	public static String[] StringArrayDecode(String s,char delm)
	{
		return s.split(delm + "");
	}
	
	public static String HexStrEncode(byte[] bts) throws Exception
	{
		Helper.AssertNotNull(bts);
		String toks = "0123456789abcdef";
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<bts.length;i++)
		{
			byte bt = bts[i];
			sb.append(toks.charAt((bt>>4)&0x0f));
			sb.append(toks.charAt(bt&0x0f));
		}
		return sb.toString();
	}
	public static byte[] HexStrDecode(String str) throws Exception
	{
		Helper.AssertNotNull(str);
		Helper.Assert(str.length()%2==0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int i=0;i<str.length();i+=2)
			baos.write(Integer.parseInt(str.substring(i,i+2),16));
		return baos.toByteArray();
	}
	
	
	
	// Mapping table from 6-bit nibbles to Base64 characters.
	private static char[]	map1	= new char[64];
	static
	{
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++)
			map1[i++] = c;
		for (char c = 'a'; c <= 'z'; c++)
			map1[i++] = c;
		for (char c = '0'; c <= '9'; c++)
			map1[i++] = c;
		map1[i++] = '+';
		map1[i++] = '/';
	}
	// Mapping table from Base64 characters to 6-bit nibbles.
	private static byte[]	map2	= new byte[128];
	static
	{
		for (int i = 0; i < map2.length; i++)
			map2[i] = -1;
		for (int i = 0; i < 64; i++)
			map2[map1[i]] = (byte) i;
	}

	public static String Base64Encode(byte[] in) 
	{
		int iLen = in.length;
		int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
		int oLen = ((iLen + 2) / 3) * 4; // output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen)
		{
			int i0 = in[ip++] & 0xff;
			int i1 = ip < iLen ? in[ip++] & 0xff : 0;
			int i2 = ip < iLen ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = map1[o0];
			out[op++] = map1[o1];
			out[op] = op < oDataLen ? map1[o2] : '=';
			op++;
			out[op] = op < oDataLen ? map1[o3] : '=';
			op++;
		}
		return new String(out);
	}

	public static byte[] Base64Decode(String s) 
	{
		char[] in = s.toCharArray();
		int iLen = in.length;
		if (iLen % 4 != 0) throw new IllegalArgumentException(
				"Length of Base64 encoded input string is not a multiple of 4.");
		while (iLen > 0 && in[iLen - 1] == '=')
			iLen--;
		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen)
		{
			int i0 = in[ip++];
			int i1 = in[ip++];
			int i2 = ip < iLen ? in[ip++] : 'A';
			int i3 = ip < iLen ? in[ip++] : 'A';
			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) throw new IllegalArgumentException(
					"Illegal character in Base64 encoded data.");
			int b0 = map2[i0];
			int b1 = map2[i1];
			int b2 = map2[i2];
			int b3 = map2[i3];
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) throw new IllegalArgumentException(
					"Illegal character in Base64 encoded data.");
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen) out[op++] = (byte) o1;
			if (op < oLen) out[op++] = (byte) o2;
		}
		return out;
	}

	
	
	
	
	public static String UnicodeEscapeEncode(String src)
	{
		if(src==null) return "";
		String toks = "0123456789abcdef";
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length();i++)
		{
			char c = src.charAt(i);
			if(c=='\\')
			{
				sb.append("\\\\");
			}
			else if(c>128)
			{
				sb.append("\\u");
				int j;
				j = (c&0xf000) >> 12;
				sb.append(toks.charAt(j));
				j = (c&0x0f00) >> 8;
				sb.append(toks.charAt(j));
				j = (c&0x00f0) >> 4;
				sb.append(toks.charAt(j));
				j = (c&0x000f);
				sb.append(toks.charAt(j));
			}
			else sb.append(c);
		}
		return sb.toString();
	}
	public static String UnicodeEscapeDecode(String src) throws Exception
	{
		if(src==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length();i++)
		{
			char c = src.charAt(i);
			if(c=='\\')
			{
				if(i+1>=src.length()) Helper.E("invalid encode1");
				c = src.charAt(i+1);
				if(c=='\\') 
				{
					sb.append(c);
					i+=1;
					continue;
				}
				else if(c=='u')
				{
					if(i+5>=src.length()) Helper.E("invalid encode2");
					String digs = src.substring(i+2,i+6);
					c = (char)Integer.parseInt(digs,16);
					sb.append(c);
					i+=5;
					continue;
				}
				else Helper.E("invalid encode3");
			}
			else sb.append(c);
		}
		return sb.toString();
	}
	public static final String XMLEntityEncode(String src) throws Exception
	{//&#20026;&#20160;&#20040;&#25105;&#20889;&#20986;&#26469;&#30340;&#37117;&#26159;&#20081;&#30721;&#21602;
		StringBuffer sb = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			int ch = (int)src.charAt(i);
			sb.append("&#").append(ch).append(';');
		}
		return sb.toString();
	}
	public static final String XMLEntityDecode(String src) throws Exception
	{
		int off = src.indexOf('&'); 
		if(off<0) return src;
		
		StringBuffer sb = new StringBuffer();
		sb.append(src.substring(0,off));
		int len = src.length();
		for(int i=off;i<len;i++)
		{
			char ch = src.charAt(i);
			if(ch=='&')
			{
				int k = src.indexOf(';',i);
				if(k>=0)
				{
					String content = src.substring(i,k+1);//&#123412;
					if(content.startsWith("&#") && content.endsWith(";"))
					{
						content = content.substring(2,content.length()-1);
						if(content.startsWith("x"))
						{
							content = content.substring(1);
							try
							{
								int val = Integer.parseInt(content,16);
								sb.append((char)val);
								i = k;
								continue;
							}
							catch(Exception e){}
						}
						else
						{
							try
							{
								int val = Integer.parseInt(content);
								sb.append((char)val);
								i = k;
								continue;
							}
							catch(Exception e){}
						}
					}
				}
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	public static final String MD5(String[] input) throws Exception
	{
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for(int i=0;i<input.length;i++) md5.update(input[i].getBytes("UTF-8"));
		byte[] res = md5.digest();
		return CommCodec.Base64Encode(res);
	}
	
	public static final String SafeMD5(String[] input) throws Exception
	{
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for(int i=0;i<input.length;i++) md5.update(input[i].getBytes("UTF-8"));
		byte[] res = md5.digest();
		return CommCodec.My64Encode(res);
	}
	
	public final static String MD5(String s)
	{ 
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f'}; 
		try
		{
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest(); 
			int j = md.length;
			char str[] = new char[j * 2]; 
			int k = 0;
			for (int i = 0; i < j; i++) 
			{
				byte byte0 = md[i]; 
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str); 
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	public static final byte[] Inflate(byte[] src,int buflen) throws Exception
	{//��ѹ
		java.util.zip.Inflater decompressor = new java.util.zip.Inflater();
		decompressor.setInput(src);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[buflen];
		while(decompressor.finished()==false)
		{
			int len = decompressor.inflate(buf);
			baos.write(buf,0,len);
		}
		return baos.toByteArray();	
	}
	
	public static final byte[] Deflate(byte[] src,int buflen) throws Exception
	{//ѹ��
		java.util.zip.Deflater compressor = new java.util.zip.Deflater();
		compressor.setInput(src);
		compressor.finish();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[buflen];				
		while(compressor.finished()==false)
		{
			int len = compressor.deflate(buf);
			baos.write(buf,0,len);
		}
		return baos.toByteArray();	
	}
}