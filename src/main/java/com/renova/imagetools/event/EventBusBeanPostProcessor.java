package com.renova.imagetools.event;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public class EventBusBeanPostProcessor implements BeanPostProcessor {

	@Autowired
	private EventBus eventBus;

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean.getClass().isAnnotationPresent(EventListener.class)) {
			registerToEventBus(bean);
		}

		return bean;
	}

	private void registerToEventBus(Object bean) {
		this.eventBus.register(bean);
	}
}
