package com.wp.casino.messageserver;

//import com.wp.casino.messageserver.listen.NettyServerListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MessageServerApplication {

//	@Bean
//	public ServletListenerRegistrationBean servletListenerRegistrationBean() {
//		ServletListenerRegistrationBean servletListenerRegistrationBean =
//				new ServletListenerRegistrationBean();
//		servletListenerRegistrationBean.setListener(new NettyServerListener());
//		return servletListenerRegistrationBean;
//	}

	public static void main(String[] args) {
		SpringApplication.run(MessageServerApplication.class, args);
	}

}
