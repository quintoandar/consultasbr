package br.com.quintoandar.consultasbr.pf;
import java.io.Serializable;

public class RespostaCaptcha implements Serializable {

	private static final long serialVersionUID = -6637625240418627035L;

	private String sessionId;

	private byte[] captchaImage;

	public RespostaCaptcha(String sessionId, byte[] captachImage) {
		super();
		this.sessionId = sessionId;
		this.captchaImage = captachImage;
	}
	
	public RespostaCaptcha(String sessionId){
		this(sessionId,null);
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public byte[] getCaptchaImage() {
		return captchaImage;
	}

	public void setCaptchaImage(byte[] captchaImage) {
		this.captchaImage = captchaImage;
	}

	public ConsultaAntecedentes consulta(String respCaptcha, String nome) {
		ConsultaAntecedentes ca  = new ConsultaAntecedentes(sessionId, nome, null, respCaptcha);
		return ca;
	}

}
