package com.clmcat.framework.webmvc.anns;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clmcat.framework.webmvc.ResponseEntityKey;
import com.clmcat.framework.webmvc.ResponseStatusAdapter;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangxingyu
 *
 * Api-controller。 标记控制器是作为API使用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@ResponseBody
@Inherited
@Documented
@RestController
public @interface ApiController {

	@AliasFor(annotation = Controller.class)
	String value() default "";

	/**
	 *  针对每个接口可以选择不一样的应答模式 KEY
	 *  如果重写了适配器, 则这个可能无效.
	 */
	Class<? extends ResponseEntityKey> entityKey() default ResponseEntityKey.class;

	/**
	 * 状态适配器。
	 */
	Class<? extends ResponseStatusAdapter> statusAdapter() default ResponseStatusAdapter.class;
	/**
	 * 结果样式适配器
	 */
	String resultAdapterName() default "";

	LogMode logMode() default LogMode.ERROR;

	public static enum LogMode {


		NONE,
		INFO,
		ERROR
		;
		public final static String REQUEST_LOG_MODE_KET = "requestLogMode";
	}
}
