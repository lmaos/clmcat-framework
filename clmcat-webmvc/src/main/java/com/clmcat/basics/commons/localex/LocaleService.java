package com.clmcat.basics.commons.localex;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.clmcat.basics.commons.lang.StringUtils;

public class LocaleService {
	
	// 别名
	public static Map<String, Locale> DEFAULT_LOCALE_ALIAS = new HashMap<>(64);
	public static void setDefaultLocaleAlias(Locale src, Locale alias) {
		String key = getAliasLocaleKey(alias);
		DEFAULT_LOCALE_ALIAS.put(key, src);
	}
	
	public static final LocaleService EMPTY = new LocaleService();

	// 根元素
	private LocaleService root = this;
	// 可选择子的元素, 用来兼容子选择.
	private Map<String, LocaleService> selects = new HashMap<>();

	private LocaleMapParser localeMapParser;
	private LinkedHashMap<String, LocaleMapParser> appends = new LinkedHashMap<>();
	private LocaleMap localeMap;
	private Locale defaultLocale;
	private boolean ignoreCase = true;
	
	
	private LocaleValueFormat localeValueFormat = new GlLocaleValueFormat();
	// 别名
	private Map<String, Locale> localeAlias = new HashMap<>(64);
	{
		localeAlias.putAll(DEFAULT_LOCALE_ALIAS);
	}
	
	
	protected LocaleService() {
		this.localeMap = new LocaleMap();
	}
	
	public LocaleService(String path, LocaleFileLoader localeFileLoader, boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		localeMapParser = new LocalFileParser(path, localeFileLoader);
		reset();
	}

	public LocaleService(LocaleMapParser localeMapParser) {
		this.localeMapParser = localeMapParser;
		reset();
	}

	public LocaleService setLocaleValueFormat(LocaleValueFormat localeValueFormat) {
		this.localeValueFormat = localeValueFormat;
		return this;
	}

	public void reset() {
		synchronized (this) {

			LocaleMap localeMap = new LocaleMap(ignoreCase);
			if (localeMapParser != null) {
				localeMap.putAll(localeMapParser.parserLocaleMap(this));
			}
			appends.forEach((k, v) -> {
				localeMap.putAll(v.parserLocaleMap(this));
			});
			this.localeMap = localeMap;
			
			if (selects.size() > 0) {
				selects.forEach((k,v)->{
					v.reset();
				});
			}
		}
	}

	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	
	public Locale getDefaultLocale() {
		return defaultLocale;
	}
	/**
	 * 查询大区的数据, 如果找不到则会使用默认大区进行填充.
	 * @param locale
	 * @param key
	 * @param args
	 * @return
	 */
	public String get(Locale locale, String key, Object... args) {
		if (localeMap == null) {
			return null;
		}
		Locale alias = getLocaleAlias(locale); // 获取别名
		String message = localeMap.get(alias, key);

		if (message == null && alias != locale) { // 别名查不到,则用原始名获取.
			message = localeMap.get(locale, key);
		}
		if (message == null && defaultLocale != null) {
			message = localeMap.get(defaultLocale, key);
		}
		if (message != null && message.length() > 0) {
			message = localeValueFormat.format(message, args);
		}
		return message;
	}
	
	/**
	 * 获取这个大区的数据, 如果找不到则使用默认值. 这个方法不会使用默认大区进行填充
	 * @param locale 大区
	 * @param key 
	 * @param def 默认值
	 * @param args 参数
	 * @return
	 */
	public String getOrDefault(Locale locale, String key, String def, Object... args) {
		if (localeMap == null) {
			return def;
		}
		Locale alias = getLocaleAlias(locale); // 获取别名
		String message = localeMap.get(alias, key);

		if (message == null && alias != locale) { // 别名查不到,则用原始名获取.
			message = localeMap.get(locale, key);
		}
		if (message == null && defaultLocale != null) {
			message = def;
		}
		if (message != null && message.length() > 0) {
			message = localeValueFormat.format(message, args);
		}
		return message;
	}
	/**
	 * 获取不到则使用传入的值, 这个方法会查询默认Locale, 默认大区不存在的时候才使用传入值
	 * @param locale
	 * @param key
	 * @param oth 默认值
	 * @param args
	 * @return
	 */
	public String getOrOther(Locale locale, String key, String oth, Object... args) {
		if (localeMap == null) {
			return null;
		}
		Locale alias = getLocaleAlias(locale); // 获取别名
		String message = localeMap.get(alias, key);

		if (message == null && alias != locale) { // 别名查不到,则用原始名获取.
			message = localeMap.get(locale, key);
		}
		if (message == null && defaultLocale != null) {
			message = localeMap.get(defaultLocale, key);
		}
		if (message == null) {
			message = oth;
		}
		if (message != null && message.length() > 0) {
			message = localeValueFormat.format(message, args);
		}
		return message;
	}
	

	/**
	 * 获得locale别名
	 * 
	 * @param locale
	 * @return
	 */
	public Locale getLocaleAlias(Locale locale) {
		if (localeAlias == null || localeAlias.size() == 0) {
			return locale;
		}
		String key = getAliasLocaleKey(locale);

		return localeAlias.getOrDefault(key, locale);
	}

	/**
	 * 设置locale别名
	 * 
	 * @param src
	 * @param alias
	 * @return
	 */
	public LocaleService setLocaleAlias(Locale src, Locale alias) {
		String key = getAliasLocaleKey(alias);
		localeAlias.put(key, src);
		return this;
	}

	private static String getAliasLocaleKey(Locale locale) {
		String key = locale.getLanguage();
		if (StringUtils.isNotBlank(locale.getCountry())) {
			key = locale.getLanguage() + "-" + locale.getCountry();
		}
		return key;
	}

	public LocaleService append(String key, LocaleMapParser localeMapParser) {
		synchronized (this) {
			this.appends.put(key, localeMapParser);
			this.localeMap.putAll(localeMapParser.parserLocaleMap(this));
		}
		return this;
	}
	public boolean isIgnoreCase() {
		return ignoreCase;
	}
	
	/**
	 * 得到多语言的value @(key)
	 * 
	 * 被这个 @() 包裹的会作为 key 查询多语言, 如果没有这个包围,则作为值直接返回.
	 * 
	 * 完整可以 @(key=def),  def是如果key不存在的时候返回的默认值.
	 * 
	 * 作用与 getLocaleKey 完全相反.
	 * 
	 * @param locale
	 * @param value
	 * @param args
	 * @return
	 */
	public String getLocaleValue(Locale locale, String value, Object ...args) {
		if (value != null) {
			if (value.startsWith("@(") && value.endsWith(")")) {
				String key = value.substring(2, value.length() - 1);
				String def = null;
				int index = key.indexOf("=");
				if (index != -1) {
					def = key.substring(index + 1);
					key = key.substring(0, index);
				}
				String val = get(locale, key, args);
				if (val != null) {
					value = val;
				} else if (def != null) {
					value = def;
				}
			}
		}
		return value;
	}
	
	
	/**
	 * 得到多语言的value @(value), 与getLocaleValue相反.
	 * 
	 * 如果 这个值 被 @()包裹,则不在作 多语言 “KEY”, 它将是一个值.
	 * 
	 * @param locale
	 * @param value
	 * @param args
	 * @return
	 */
	public String getLocaleKey(Locale locale, String keyvalue, Object ...args) {
		String val = null; 
		if (keyvalue != null) {
			if (keyvalue.startsWith("@(") && keyvalue.endsWith(")")) {
				String res = keyvalue.substring(2, keyvalue.length() - 1);
				val = res;
			} else {
				val = get(locale, keyvalue, args);
			}
		}
		return val;
	}
	/**
	 * 选择一个LocaleService
	 * 
	 * @param selectName
	 * @param selectMode DEF 选择的不存在返回默认的, CREATE 选择不存在则创建 
	 * @return
	 */
	public LocaleService select(String selectName, SelectMode selectMode) {
		if (selectName == null || (selectName = selectName.trim()).isEmpty()) {
			return root;
		}
		LocaleService localeService = root.selects.get(selectName);
		if (localeService == null) {
			if (selectMode == SelectMode.CREATE) {
				localeService = new LocaleService();
				localeService.root = this.root;
				localeService.localeValueFormat = root.localeValueFormat;
				localeService.localeAlias = root.localeAlias;
				localeService.ignoreCase = root.ignoreCase;
				localeService.defaultLocale = root.defaultLocale;
				selects.put(selectName, localeService);
			} else if (selectMode == SelectMode.NONE) {
				//
			} else {
				localeService = root;
			}
		}
		return localeService;
	}
	/**
	 * 选择, 如果不存在则使用默认 : 等同于 select(selectName, SelectMode.DEF);.   <br>
	 * 
	 * 如果想要创建一个新的, 请调用  select(selectName, SelectMode.CREATE);
	 * 
	 * @param selectName
	 * @return
	 */
	public LocaleService select(String selectName) {
		return select(selectName, SelectMode.DEF);
	}
	
	
	public boolean isRoot() {
		return this.root == this;
	}
	
	public static enum SelectMode {
		/**
		 * 如果选择的不存在,则使用根值
		 */
		DEF,
		/**
		 * 如果选择的不存在, 则创建一个新的
		 */
		CREATE,
		/**
		 * 如果选择的不存在则返回 NULL
		 */
		NONE,
	}
}
