package com.puffer.fabric;

import com.puffer.core.context.mybatis.PufferMapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * spring boot 启动类
 *
 * @author buyi
 * @date 2017年11月30日上午11:18:22
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTransactionManagement
@PufferMapperScan(value = "${mybatis.mapper-scan-path}")
public class Application {
	private final static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		logger.info("启动成功");
	}
}
