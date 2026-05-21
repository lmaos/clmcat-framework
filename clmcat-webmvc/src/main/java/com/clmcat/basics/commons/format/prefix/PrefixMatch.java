package com.clmcat.basics.commons.format.prefix;
/**
 * 快速前缀匹配
 * 
 * @author zhangxingyu
 *
 */
public class PrefixMatch {

	
	private PrefixNode head = new PrefixNode();
	
	public Object putPrefix(String prefixText, Object value) {
		PrefixNode next = head;
		for (int i = 0; i < prefixText.length(); i++) {
			char c = prefixText.charAt(i);
			next = next.createOrGet(c);
		}
		next.setPrefixText(prefixText);
		return next.setValue(value);
	}
	
	/**
	 * 前缀匹配获取KEY数据
	 * @param prefix
	 * @return
	 */
	public PrefixResult get(String text) {
		PrefixNode next = head;
		PrefixNode result = null;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			PrefixNode node = next.next(c);
			if (node == null) {
				break;
			} else if (node.getPrefixText() != null){
				result = node;
			}
			next = node;
		}
		return result == null ? null : new PrefixResult(result.getPrefixText(), result.getValue());
	}
	
	public static void main(String[] args) {
		PrefixMatch match = new PrefixMatch();
		match.putPrefix("hello", 12345);
		match.putPrefix("hello-jj", 9999);
		match.putPrefix("hello-jj-kk", 88888);
		
		System.out.println(match.get("hello-sds"));
		System.out.println(match.get("hello-jj-cv"));
		System.out.println(match.get("hello-jj-kkasd"));
		
		
	}
	
}
