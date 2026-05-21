package com.clmcat.basics.commons.versioncmp;


import java.util.Arrays;

public class Version implements Comparable<Version> {
	private String value; // x.xx.xx.xxx
	private int[] versionItems;

	public Version(String value) {
		this.value = value;
		String[] versionArrs = value.split("\\.");
		versionItems = new int[versionArrs.length];
		for (int i = 0; i < versionArrs.length; i++) {
			versionItems[i] = Integer.parseInt(versionArrs[i]);
		}
	}
	public Version(int[] versionItems) {
		this.versionItems = versionItems;
		if (versionItems.length > 0) {
			StringBuilder builder = new StringBuilder(32);
			for (int versionItem : versionItems) {
				builder.append(versionItem).append(".");
			}
			value = builder.substring(0, builder.length() - 1);
		}
		
	}

	@Override
	public int compareTo(Version o) {
		int len = Math.min(versionItems.length, o.versionItems.length);
		for (int i = 0; i < len; i++) {
			if (versionItems[i] > o.versionItems[i]) {
				return 1;
			} else if (versionItems[i] < o.versionItems[i]) {
				return -1;
			}
		}
		int x = versionItems.length - o.versionItems.length;
		if (x == 0) {
			return 0;
		} else if (x > 0) {
			return 1;
		} else {
			return -1;
		}
	}

	public Version add(Version o) {
		return add(o, 0);
	}
	
	public Version add(Version o, String maxVersion) {
		return add(o, Version.of(maxVersion).versionItems);
	}
	/**
	 * 
	 * @param o 追加版本
	 * @param maxVersionItemNum 最大版本数
	 * @return
	 */
	public Version add(Version o, int maxVersionItemNum) {
		int len = Math.max(versionItems.length, o.versionItems.length);
		int[] maxVersionItemNums = new int[len];
		for (int i = 0; i < maxVersionItemNums.length; i++) {
			
		}
		Arrays.fill(maxVersionItemNums, maxVersionItemNum);
		return add(o, maxVersionItemNums); 
	}
	/**
	 * 
	 * @param o 追加版本
	 * @param maxVersionItemNums 最大版本设置
	 * @return
	 */
	public Version add(Version o, int[] maxVersionItemNums) {
		int len = Math.max(versionItems.length, o.versionItems.length);
		int[] newVersionItems = new int[len];
		int[] maxVersionItemNumsTmp = maxVersionItemNums;
		if (maxVersionItemNums.length < len) {
			maxVersionItemNumsTmp = new int[len];
			System.arraycopy(maxVersionItemNums, 0, maxVersionItemNumsTmp, 0, len);
		}
		
		for (int i = 0; i < versionItems.length; i++) {
			newVersionItems[i] = versionItems[i];
		}
		int item = 0;
		for (int i = o.versionItems.length - 1; i >= 0 ; i--) {
			newVersionItems[i] += item;
			newVersionItems[i] += o.versionItems[i];
			int maxVersionItemNum = maxVersionItemNumsTmp[i];
			if (newVersionItems[i] < 0) {
				if (maxVersionItemNum > 0) {
					int mod  = newVersionItems[i] % (maxVersionItemNum + 1);
					item = newVersionItems[i] / (maxVersionItemNum + 1) - (mod != 0 ? 1 : 0);
					newVersionItems[i] = mod ;
					if (mod < 0) {
						newVersionItems[i] = mod + (maxVersionItemNum + 1);
					}
				} else {
					newVersionItems[i] = 0;
				}
				
			} else if (maxVersionItemNum > 0 && newVersionItems[i] > maxVersionItemNum) {
				item = newVersionItems[i] / (maxVersionItemNum + 1);
				newVersionItems[i] = newVersionItems[i] % (maxVersionItemNum + 1);
				
			} else {
				item = 0;
			}
		}
		if (item > 0) {
			for (int i = 0; i < newVersionItems.length; i++) {
				newVersionItems[i] = maxVersionItemNumsTmp[i];
			}
		} else if (item < 0) {
			for (int i = 0; i < newVersionItems.length; i++) {
				newVersionItems[i] = 0;
			}
		}
		
		return new Version(newVersionItems);
	}
	
	
	public Version add(String version, String maxVersion) {
		return add(Version.of(version), maxVersion);
	}
	
	public Version add(String version, int maxVersionItemNum) {
		return add(Version.of(version), maxVersionItemNum);
	}
	
	public Version add(String version) {
		return add(Version.of(version));
	}
	
	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			return ((Version) obj).value.equals(value);
		}
		return super.equals(obj);
	}
	
	public static Version of(String versionValue) {
		return new Version(versionValue);
	}
	
	public static void main(String[] args) {
		//System.out.println(Version.of("991.2.32.1").compareTo(Version.of("1.2.32")));
		//System.out.println(Version.of("1.3.5").add("0.0.1", 5));
		Version version = Version.of("5.0.0");
		for (int i = 0; i < 100; i++) {
			version = version.add("0.0.1", "9.8.9");
			System.out.println(version);
		}
//		System.out.println((-11 + 5) / (5 + 1) - 1);
//		System.out.println(0 % 6);  // 0 0
//		System.out.println(-1 % 6); // -1 5
//		System.out.println(-2 % 6); // -2 4
//		System.out.println(-3 % 6); // -3 3
//		System.out.println(-4 % 6); // -4 2
//		System.out.println(-5 % 6); // -5 1
//		System.out.println(-6 % 6); //  0 0
//		System.out.println();
	}
}
