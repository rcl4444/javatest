package cn.zhijian.passport.config;

import javax.annotation.Nullable;

import org.simplejavamail.mailer.config.ServerConfig;
import org.simplejavamail.mailer.config.TransportStrategy;

import cn.zhijian.passport.api.EmailAddress;
import lombok.Data;

@Data
public class SmtpConfig {

	@Nullable
	String factoryClass;
	String host;
	Integer port;
	String username;
	String password;
	EmailAddress sender;

	/**
	 * <ul>
	 * <li>plain
	 * <li>ssl
	 * <li>tls
	 * </ul>
	 */
	String transport = "plain";

	public ServerConfig serverConfig() {
		return new ServerConfig(host, port, username, password);
	}

	public TransportStrategy transportStrategy() {
		switch (transport.toLowerCase()) {
		case "ssl":
			return TransportStrategy.SMTP_SSL;
		case "tls":
			return TransportStrategy.SMTP_TLS;
		default:
			return TransportStrategy.SMTP_PLAIN;
		}
	}

}
