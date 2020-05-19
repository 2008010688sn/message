package com.wp.casino.messageclient;

import com.wp.casino.messageclient.listen.NettyClientListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MessageClientApplication {

	@Bean
	public ServletListenerRegistrationBean servletListenerRegistrationBean() {
		ServletListenerRegistrationBean servletListenerRegistrationBean =
				new ServletListenerRegistrationBean();
		servletListenerRegistrationBean.setListener(new NettyClientListener());
		return servletListenerRegistrationBean;
	}
	public static void main(String[] args) {
		SpringApplication.run(MessageClientApplication.class, args);
	}

}
