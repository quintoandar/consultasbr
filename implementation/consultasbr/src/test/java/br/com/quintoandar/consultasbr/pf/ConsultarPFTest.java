package br.com.quintoandar.consultasbr.pf;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConsultarPFTest {
	private static ConsultarPF consultarPF;
	
	@Before
	public void setBefore(){
		if(consultarPF == null){
			consultarPF = new ConsultarPF();
		}
	}

	private String solveCaptcha(String name, byte[] captchaImage) {
		JLabel jLabel = new JLabel(new ImageIcon(captchaImage));
		jLabel.setText("Testing for: " + name);
		return JOptionPane.showInputDialog(jLabel);
	}

	@Test
	public void testConsultaFernandinhoBeiraMar() {
		RespostaCaptcha captcha = consultarPF.requestCaptcha();

		String respCaptcha = solveCaptcha("Luiz Fernando da Costa", captcha.getCaptchaImage());

		ConsultaAntecedentes con = captcha.consulta(respCaptcha, "Luiz Fernando da Costa");// fernandinho beira mar

		ResultadoAntecedentes ra = consultarPF.consultarAntecedentes(con);
		Assert.assertEquals(StatusAntecedentes.VerificarComPF, ra.getStatus());
		Assert.assertNotNull(ra.getProtocolo());
		Assert.assertNotNull(ra.getPdf());
	}

	@Test
	public void testConsultaLula() {
		RespostaCaptcha captcha = consultarPF.requestCaptcha();

		String respCaptcha = solveCaptcha("Luis Inácio Lula da Silva", captcha.getCaptchaImage());

		ConsultaAntecedentes con = captcha.consulta(respCaptcha, "Luis Inácio Lula da Silva");

		ResultadoAntecedentes ra = consultarPF.consultarAntecedentes(con);
		Assert.assertEquals(StatusAntecedentes.SemAntecedentes, ra.getStatus());
		Assert.assertNull(ra.getProtocolo());
		Assert.assertNotNull(ra.getPdf());
	}

	@Test
	public void testWrongCaptcha() {
		RespostaCaptcha captcha = consultarPF.requestCaptcha();

//		String respCaptcha = solveCaptcha("Luis Inácio Lula da Silva", captcha.getCaptchaImage());
		String respCaptcha = "XXXXXX";

		ConsultaAntecedentes con = captcha.consulta(respCaptcha, "Luis Inácio Lula da Silva");

		ResultadoAntecedentes ra = consultarPF.consultarAntecedentes(con);
		Assert.assertEquals(StatusAntecedentes.CaptchaInvalido, ra.getStatus());
		Assert.assertNull(ra.getProtocolo());
		Assert.assertNull(ra.getPdf());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNoSessionId() {
		RespostaCaptcha captcha = new RespostaCaptcha(null);

		String respCaptcha = null;
		ConsultaAntecedentes con = captcha.consulta(respCaptcha, "Luis Inácio Lula da Silva");
		ResultadoAntecedentes ra = consultarPF.consultarAntecedentes(con);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNoRespostaCaptcha() {
		RespostaCaptcha captcha = consultarPF.requestCaptcha();

		String respCaptcha = null;
		ConsultaAntecedentes con = captcha.consulta(respCaptcha, "Luis Inácio Lula da Silva");
		ResultadoAntecedentes ra = consultarPF.consultarAntecedentes(con);
	}

}
