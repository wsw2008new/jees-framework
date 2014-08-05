package com.iisquare.jees.framework.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import com.iisquare.jees.framework.FrameworkConfiguration;
import com.iisquare.jees.framework.util.DPUtil;

public class RequestMappingHandlerMapping extends
		org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping {

	private FrameworkConfiguration frameworkConfiguration;
	private boolean useSuffixPatternMatch = true;
	private boolean useTrailingSlashMatch = true;
	private final List<String> fileExtensions = new ArrayList<String>();

	public FrameworkConfiguration getFrameworkConfiguration() {
		return frameworkConfiguration;
	}

	public void setFrameworkConfiguration(
			FrameworkConfiguration frameworkConfiguration) {
		this.frameworkConfiguration = frameworkConfiguration;
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method,
			Class<?> handlerType) {
		RequestMappingInfo info =  super.getMappingForMethod(method, handlerType);
		if(null == info) { // 未设置@RequestMapping时执行约定路由映射
			/* 提取相关URI路径参数 */
			String classFullName = method.getDeclaringClass().getName();
			String actionName = method.getName();
			Scope scope = method.getDeclaringClass().getAnnotation(Scope.class);
			/* 约定前提判定 */
			if(null == scope // 确保对象为多实例模式
					|| !ConfigurableBeanFactory.SCOPE_PROTOTYPE.equals(scope.value())) return null;
			if(!classFullName.startsWith(frameworkConfiguration.getModulePrefix())) return null;
			if(!classFullName.endsWith(frameworkConfiguration.getControllerSuffix())) return null;
			if(!actionName.endsWith(frameworkConfiguration.getActionSuffix())) return null;
			/* 提取Module名称 */
			String moduleName = classFullName.substring(0, classFullName.lastIndexOf("."));
			moduleName = moduleName.substring(frameworkConfiguration.getModulePrefix().length());
			/* 提取Controller名称 */
			String controllerName = classFullName.substring(classFullName.lastIndexOf(".") + 1);
			controllerName = controllerName.substring(0, controllerName.lastIndexOf(frameworkConfiguration.getControllerSuffix()));
			controllerName = DPUtil.lowerCaseFirst(controllerName);
			/* 提取Action名称 */
			actionName = actionName.substring(0, actionName.lastIndexOf(frameworkConfiguration.getActionSuffix()));
			/* 组合Pattern路径 */
			StringBuilder pb = new StringBuilder();
			if(0 < moduleName.length()) {
				pb.append("/")
						.append(moduleName.replaceAll("\\.", "/"));
			}
			pb.append("/")
					.append(controllerName)
					.append("/")
					.append(actionName);
			String[] patterns = {pb.toString()};
			/* 生成RequestMappingInfo对象 */
			RequestCondition<?> methodCondition = getCustomMethodCondition(method);
			info = new RequestMappingInfo(
					new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(),
							this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions),
					new RequestMethodsRequestCondition(),
					new ParamsRequestCondition(),
					new HeadersRequestCondition(),
					new ConsumesRequestCondition(),
					new ProducesRequestCondition(),
					methodCondition);
		}
		return info;
	}

}
